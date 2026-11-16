# 13. Backend Architecture

Bitta deploy qilinadigan **Spring Boot 3** ilovasi (Java 21, virtual threads yoqilgan: `spring.threads.virtual.enabled=true`), ichida **7 biznes modul** — modular monolith. Build: Gradle (Kotlin DSL), bitta modul (`backend/`), paket darajasidagi modullar Spring Modulith bilan tekshiriladi.

## 1. Modullar

| Modul (paket `com.devtype.*`) | Mas'uliyat | Ommaviy API (`api` paketi) | Nashr qiladigan hodisalar |
| --- | --- | --- | --- |
| `identity` | Ro'yxat, login, JWT, refresh, OAuth2, 2FA, profil, sozlamalar, follow, mehmon tokeni | `UserDirectory`, `CurrentUser`, `UserView` | `UserRegistered`, `UserBlocked`, `UserAnonymized` |
| `content` | Tillar, mavzular, mashqlar, teglar, validatsiya, import, shikoyatlar | `LanguageCatalog`, `ExerciseCatalog`, `ExerciseView` | `ExercisePublished`, `LanguagePublished` |
| `training` | Sessiya hayot sikli, snippet tanlash, keylog replay, metrikalar, anti-cheat, mavzu progressi | `SessionQuery`, `TopicProgressQuery`, `WeaknessProfilePort` (SPI), `SessionOutcomeCollector` | `SessionCompleted`, `SessionRevoked`, `TopicLevelCompleted` |
| `analytics` | Agregatlar, belgi/bigram statistikasi, zaiflik profili, mahsulot metrikalari | `WeaknessProfileProvider`, `LanguageStatsQuery` | — |
| `gamification` | XP, level, rank, streak, rekordlar, achievement, challenge, reyting, musobaqa, bildirishnomalar | `GamificationSummary`, `SessionRewards` | `LevelUp`, `StreakUpdated`, `AchievementUnlocked`, `RecordBroken` |
| `coach` | Kunlik reja, maslahat, maqsad, haftalik hisobot, narrator | `CoachTipProvider`, `CoachPlanView` | `GoalAchieved` |
| `admin` | Admin endpoint'lari (boshqa modullar API'si orqali), audit jurnali | `AuditLogger` | — |

`shared` — modul emas, texnik kutubxona (Modulith'da `@ApplicationModule(type = OPEN)`): xato modeli, sahifalash, `Clock`, ID generator (UUID v7), `LocaleContext`, konstantalar.

Ruxsat etilgan bog'liqliklar (faqat `api` paketlari orqali):

```text
admin        → identity, content, training, analytics, gamification, coach
coach        → analytics, training, gamification, content, identity
gamification → training(api: hodisalar), analytics(api), content, identity
analytics    → training(api: hodisalar), content, identity
training     → content, identity
content      → identity
identity     → (hech kim)
```

Sikl yo'q (Modulith sikllarni rad etadi):

- `training` `gamification`/`coach` ni chaqirmaydi — sinxron mukofotlar va maslahat `SessionCompleted` hodisasining `BEFORE_COMMIT` tinglovchilari orqali yig'iladi (§4): tinglovchilar natijani hodisa obyektidagi `SessionOutcomeCollector` ga yozadi (`training.api` da e'lon qilingan).
- WEAKNESS rejimi uchun zaiflik profili kerak, lekin `training → analytics` bog'liqligi sikl hosil qilardi. Shuning uchun port **`training.api.WeaknessProfilePort`** da e'lon qilinadi va uni `analytics` implementatsiya qiladi (dependency inversion). `coach` va `admin` esa `analytics.api.WeaknessProfileProvider` dan to'g'ridan-to'g'ri foydalanadi.

`ModularityTests`: `ApplicationModules.of(DevTypeApplication.class).verify()` — CI'da majburiy.

## 2. Modul ichki tuzilmasi

```text
com.devtype.training/
  api/                  # boshqa modullar ko'radigan interfeyslar, DTO (record), hodisalar
  web/                  # @RestController, request/response record'lar, mapper
  application/          # use-case servislar; @Transactional chegarasi shu yerda
     StartSessionUseCase, NextExerciseUseCase, CompleteSessionUseCase, AbandonSessionUseCase,
     ClaimGuestSessionsUseCase, SessionHistoryQuery
  domain/               # entity, value object, domen servislari — Spring'siz, sof Java
     Session, SessionStatus, Mode, Keylog, KeylogDecoder, TargetPreparer, KeylogReplayer,
     MetricsCalculator, SuspicionRules, ExercisePicker, TopicProgressCalculator
  infrastructure/       # JPA repository implementatsiyalari, JDBC so'rovlar, tashqi adapterlar
     SessionJpaRepository, KeylogStore, CompleteTokenService
  internal/             # hodisa tinglovchilari, scheduler job'lari
```

- Bog'liqlik yo'nalishi: `web → application → domain ← infrastructure`. `domain` Spring, JPA annotatsiyalaridan xoli bo'lishi **KERAK** `training` va `gamification` da (hisob-kitob mantig'i); `content`, `identity`, `admin` kabi CRUD modullarda JPA entity domen obyekti sifatida ishlatiladi va alohida port interfeyslari yozilmaydi (**D-9**).
- ArchUnit qoidalari: `..domain..` `org.springframework..` ga bog'liq emas (istisno modullar ro'yxati bilan); `..web..` repository'ga to'g'ridan-to'g'ri murojaat qilmaydi; `main` kodida `*Mock*`, `*Fake*`, `*Stub*`, `*Dummy*` klasslari yo'q.

## 3. Texnik qatlamlar

| Masala | Yechim |
| --- | --- |
| Web | Spring MVC (virtual threads); `@RestController`; kirish DTO — `record` + Bean Validation |
| Persistence | Spring Data JPA (Hibernate 6); `open-in-view=false`; og'ir o'qishlar (reyting, analytics, tarix) — `JdbcClient` + native SQL, DTO projeksiya |
| N+1 oldini olish | `@EntityGraph`/`JOIN FETCH` yoki DTO so'rovlari; testlarda Hibernate statistikasi bilan so'rov soni tekshiriladi (`18`) |
| Tranzaksiya | Use-case darajasida `@Transactional`; o'qish — `readOnly=true`; izolyatsiya `READ COMMITTED`; qatorni qulflash kerak joyda `SELECT … FOR UPDATE` (`sessions`, `user_progress`) |
| Optimistic lock | `@Version` — `users`, `user_progress`; to'qnashuvda 1 marta qayta urinish, keyin `409 CONCURRENT_MODIFICATION` |
| Hodisalar | Spring Modulith Event Publication Registry (JDBC) — transactional outbox; tugallanmagan nashrlar qayta ishga tushganda va har 1 daqiqada qayta uriniladi; 7 kundan eski tugallanganlar tozalanadi |
| Kesh | Caffeine: `languages` (5 daq), `topics` (5 daq), `exercisePool:{lang}` (ranked pool, 5 daq), `leaderboardTop:{board}:{scope}` (60 s), `weaknessProfile:{user}:{lang}` (10 daq), `levelTable` (doimiy). Kontent o'zgarganda tegishli kesh hodisa bilan tozalanadi |
| Scheduler | `@Scheduled` + ShedLock (JDBC) — ro'yxat `12` §5 va `11` |
| Xatolar | `@RestControllerAdvice` → `ProblemDetail` (`14` §1.3); kutilmagan istisno → `500 INTERNAL_ERROR`, stack trace klientga chiqmaydi |
| Validatsiya | Format — DTO da (Bean Validation); biznes qoidalar — domen/use-case da, `DomainException(code, args)` |
| i18n | `LocaleResolver`: foydalanuvchi `locale` → `Accept-Language` → `en`; `MessageSource` (`messages_{en,uzl,uzc,ru}.properties`) |
| Email | Port `MailSender` → SMTP adapter; shablonlar Thymeleaf, 4 lokal; yuborish commit'dan keyin, asinxron, 3 marta qayta urinish |
| Fayl saqlash | Port `ObjectStorage` → S3-mos adapter (prod) / lokal papka (local); avatar va importlar |
| LLM | Port `LlmClient` (`coach`) → HTTP adapter; timeout 8 s; circuit breaker (Resilience4j) |
| Rate limit | Bucket4j (xotirada, Caffeine bilan); filtr darajasida; kalitlar `00` §4.9 |
| Kuzatuv | Micrometer + Prometheus; OpenTelemetry trace (OTLP); JSON loglar (`traceId`, `userId` — hash) |
| Konfiguratsiya | `application.yml` + profil (`local`, `test`, `staging`, `prod`); sirlar faqat muhit o'zgaruvchilaridan; biznes konstantalar `devtype.*` prefiksi ostida `@ConfigurationProperties` (validatsiya bilan) |
| API hujjati | springdoc-openapi → `/v3/api-docs`; CI'da `docs/openapi.yaml` (generatsiya qilingan) bilan diff — kutilmagan o'zgarish build'ni to'xtatadi |
| WebSocket (R3) | STOMP `/ws`; faqat poyga xonalari; holat xotirada (bitta instans) |

## 4. Asosiy oqim: sessiyani yakunlash

```text
POST /api/v1/sessions/{id}/complete
 └─ CompleteSessionUseCase.execute(cmd)                       @Transactional
     1. completeToken tekshiruvi (HMAC, sessionId, exp)        → 401 COMPLETE_TOKEN_INVALID
     2. session = repo.findForUpdate(id)                       → 404 SESSION_NOT_FOUND
        egalik: session.userId == currentUser yoki guestId mos → 404
     3. status != STARTED:
          COMPLETED/REJECTED → return session.resultJson (200)  [idempotent]
          ABANDONED          → 409 SESSION_NOT_ACTIVE
     4. now > startedAt + 24h → ABANDONED; 422 SESSION_EXPIRED
     5. keylog = KeylogDecoder.decode(base64)                  → REJECTED(KEYLOG_INVALID), 422
     6. exercises = session_exercises (+ content yoki generated_code); langConfig
     7. result = KeylogReplayer.replay(...)                    (07 §8)
     8. SuspicionRules.validateTiming(...)                     → REJECTED(TIMING_MISMATCH), 422
        BL-TM-3 rejim qoidalari; SESSION_TOO_SHORT             → ABANDONED, 422
     9. score = SuspicionRules.score(...)                      (07 §9.3)
    10. session.complete(result, score); ranked = eligibility(...)
    11. persist: sessions, session_exercises (natijalar), session_keylogs, session_char_stats
        TopicProgressCalculator → user_exercise_best, user_topic_progress   (BL-LS-1)
    12. publish SessionCompleted(payload)                      # outbox'ga shu tranzaksiyada
        ├─ BEFORE_COMMIT (sinxron, shu tranzaksiya):
        │    gamification.SessionRewardsListener → XP, level, streak, rekord, rank  → SessionRewards
        │    coach.TipListener → coachTip (faqat user_id bor bo'lsa va coachEnabled)
        └─ AFTER_COMMIT (@ApplicationModuleListener, asinxron, qayta uriniladigan):
             analytics.AggregatesListener
             gamification.LeaderboardListener, ChallengeListener, AchievementListener
             coach.PlanProgressListener
    13. response = assemble(result, rewards, progress, tip); session.resultJson = response
 └─ 200 OK
```

Qoidalar:

- Sinxron tinglovchi xato bersa — butun tranzaksiya qaytadi (`500`), klient qayta uradi (idempotent). Shuning uchun sinxron qism minimal va faqat bazaga bog'liq.
- Asinxron tinglovchilar **idempotent** (`processed_sessions`, `UNIQUE` cheklovlari, upsert); xatoda Modulith qayta uradi; 5 muvaffaqiyatsiz urinishdan keyin alert (`event_publication` da tugallanmagan yozuvlar metrikasi).
- Mehmon sessiyasi: 12-qadamda hodisa `userId=null` bilan nashr qilinadi; gamification/analytics/coach tinglovchilari uni o'tkazib yuboradi. Claim'da hodisa qayta nashr qilinadi (`BL-UM-3`).
- `SessionCompleted` payload: sessiya ID, user ID, til, rejim, metrikalar, snippetlar (`exercise_key`, daraja, topic, belgilar), belgi/bigram statistikasi, `claimed`, `ranked`.

## 5. Sessiyani ochish

```text
POST /api/v1/sessions → StartSessionUseCase                    @Transactional
  1. rate limit; til PUBLISHED va (user uchun) tanlangan/aktiv   → 409 LANGUAGE_NOT_AVAILABLE
  2. rejim relizda mavjudmi (feature flag devtype.modes.<MODE>)  → 409 MODE_NOT_AVAILABLE
     mehmon: faqat SPEED_TEST                                    → 403 GUEST_MODE_NOT_ALLOWED
  3. rejimga xos tekshiruv: BL-LS-2 (qulf), BL-TM-4 (profil), challenge/competition urinishlari
  4. snippetlar: ExercisePicker (BL-TM-1) yoki generator (BL-SC-1)
  5. ochiq STARTED sessiyalar > 3 → eng eskisi ABANDONED
  6. insert sessions (id = UUIDv7, engine_version = so'rovdagi, limit hisoblangan), session_exercises
  7. completeToken = HMAC-SHA256(secret, sessionId | startedAt | exp=startedAt+24h)
  8. 201: sessionId, startedAt, durationLimitS, exercises[{seq, code, indent, tokenPatterns, …}], completeToken
```

`engine_version` server qo'llab-quvvatlaydigan oraliqdan tashqarida → `426 ENGINE_VERSION_UNSUPPORTED` (klient sahifani yangilashni taklif qiladi).

## 6. Yangi dasturlash tilini qo'shish — kod o'zgarishisiz

1. `POST /admin/languages` — slug, nom, `grammar_id`, indent, `token_patterns`, `special_templates`, `forbidden_patterns` (holat `DRAFT`).
2. Mavzular va mashqlar import qilinadi (`content/<slug>/`), ko'rib chiqiladi, nashr qilinadi.
3. `POST /admin/languages/{id}/publish` — `BL-PL-2` tekshiruvi → `PUBLISHED`; `LanguagePublished` hodisasi keshlarni tozalaydi.
4. Til katalogda, practice'da, statistikada, reytingda paydo bo'ladi.

Kod o'zgarishi talab qilinadigan yagona holat: Shiki'da shu `grammar_id` yo'q — frontendga grammatika qo'shiladi (til baribir highlight'siz ishlaydi).

## 7. Xatolarga chidamlilik

| Nosozlik | Xulq |
| --- | --- |
| PostgreSQL vaqtincha yo'q | HikariCP timeout 5 s → `503 SERVICE_UNAVAILABLE` + `Retry-After: 5`; readiness `DOWN` |
| SMTP ishlamaydi | Xat navbatda (outbox hodisasi), qayta uriniladi; ro'yxatdan o'tish muvaffaqiyatli |
| LLM ishlamaydi | Circuit breaker ochiladi → `TemplateNarrator` |
| Obyekt xotirasi ishlamaydi | Avatar yuklash `503`; qolgan funksiyalar ishlaydi |
| HIBP API ishlamaydi | Parol tekshiruvi o'tkazib yuboriladi (WARN log) |
| Hodisa tinglovchisi xatosi | Qayta urinish; javobga ta'sir qilmaydi |
| Ilova qayta ishga tushdi | Tugallanmagan hodisalar qayta nashr qilinadi; `STARTED` sessiyalar saqlanadi |

## 8. Loyiha tuzilmasi (maqsad)

```text
backend/
  build.gradle.kts, settings.gradle.kts, gradle/libs.versions.toml
  src/main/java/com/devtype/
    DevTypeApplication.java
    shared/ identity/ content/ training/ analytics/ gamification/ coach/ admin/
  src/main/resources/
    application.yml, application-{local,test,staging,prod}.yml
    db/migration/…            messages_*.properties      mail/{locale}/*.html
  src/test/java/…             # 18-hujjat
  Dockerfile
```

Asosiy bog'liqliklar: `spring-boot-starter-{web,security,data-jpa,validation,actuator,oauth2-client,mail,websocket(R3)}`, `spring-modulith-starter-{core,jpa}`, `flyway-core`, `flyway-database-postgresql`, `postgresql`, `jjwt` (yoki `nimbus-jose-jwt`), `bucket4j-core`, `caffeine`, `shedlock-spring` + `shedlock-provider-jdbc-template`, `springdoc-openapi-starter-webmvc-ui`, `resilience4j-spring-boot3`, `micrometer-registry-prometheus`, `opentelemetry` starter, `bouncycastle` (Argon2), `dev.samstevens.totp` (yoki ekvivalent); test: `spring-boot-starter-test`, `testcontainers` (postgresql), `archunit-junit5`, `spring-modulith-starter-test`, `spring-security-test`.
