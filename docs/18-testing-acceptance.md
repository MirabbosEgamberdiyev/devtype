# 18. Testing Strategy va Acceptance Criteria

## 1. Tamoyillar

1. **Test yozilgani ≠ test o'tgani.** Talab faqat CI'da yashil o'tgan test bilan "bajarilgan" hisoblanadi.
2. **Har FR — kamida bitta avtomatlashtirilgan test.** Test nomida yoki annotatsiyasida AC ID ko'rsatiladi (`@Tag("AC-TE-4")`, `test('AC-TE-4: …')`); CI skripti barcha `AC-*` ID'lar uchun kamida bitta test borligini tekshiradi (`scripts/ac-coverage`).
3. Eng qimmat xato — noto'g'ri metrika yoki klient/server farqi; piramidaning asosi — engine va uning server juftligi.
4. Production kodida mock yo'q; testlarda fixture va fabrikalar ruxsat (`src/test`, `*.test.ts`, `e2e/`).
5. Destruktiv testlar faqat `local`/CI/`staging` da.

## 2. Test darajalari

| # | Daraja | Vosita | Nima tekshiriladi | Mezon |
| --- | --- | --- | --- | --- |
| 1 | Engine unit | Vitest | Reducer o'tishlari, metrikalar, indent, backspace, keylog codec | `engine/` 100% branch |
| 2 | Engine property | fast-check | Invariantlar (§3.1) | 10 000 generatsiya / xususiyat |
| 3 | Klient–server moslik | `engine-vectors/*.json` (Vitest + JUnit) | Bir xil kirish → bir xil holat va metrika | Farq 0; ≥ 120 vektor |
| 4 | Backend unit | JUnit 5, AssertJ | Domen: replay, shubha qoidalari, XP, level, streak, rekord, progress, zaiflik, coach, validatsiya, transliteratsiya | Domen paketlari ≥ 90% line |
| 5 | Arxitektura | Spring Modulith `verify()`, ArchUnit | Modul chegaralari, qatlam qoidalari, `main` da mock yo'q | Build to'xtaydi |
| 6 | Repository/integratsiya | Spring Boot Test + Testcontainers (PostgreSQL 16) | Migratsiyalar, so'rovlar, cheklovlar, tranzaksiya, hodisa oqimi, idempotentlik, so'rovlar soni | Har use-case |
| 7 | API | MockMvc/RestAssured + OpenAPI validator | Har endpoint: muvaffaqiyat, validatsiya, 401/403/404, egalik, xato kodlari, sahifalash | Barcha endpoint'lar |
| 8 | Xavfsizlik (avtomatik) | Spring Security Test, maxsus testlar | §3.3 ro'yxati | Hammasi yashil |
| 9 | Frontend komponent | Vitest + Testing Library | Komponent holatlari, formalar, guard'lar, hook'lar | Umumiy ≥ 75% |
| 10 | E2E | Playwright (Chromium, Firefox, WebKit) | §3.4 ssenariylari | Har PR'da Chromium; `main` da 3 brauzer |
| 11 | Vizual/responsive | Playwright screenshot | 8 asosiy ekran × 2 tema × 5 kenglik (320, 768, 1280, 1920, 3840) × 4 lokal (asosiy 3 ekran) | Farq < 0.1% |
| 12 | Accessibility | axe-core (Playwright) | WCAG AA | 0 `critical`/`serious` |
| 13 | Unumdorlik (frontend) | Playwright perf, Lighthouse CI, size-limit | `17` §2 | Byudjetlar |
| 14 | Yuklama | k6 | `17` §5 | SLO |
| 15 | Kontent | `content/` validatori + kompilyatorlar (Oracle Free, PostgreSQL, javac, …) | Har snippet sintaktik to'g'ri, `BL-EM-1` | 100% |
| 16 | i18n | `i18n:check`, ko'plik testlari, transliteratsiya testlari | `22` §9 | 0 yetishmovchilik |
| 17 | DAST/SAST | ZAP baseline, Semgrep, Dependency-Check, Trivy, gitleaks | `16` §10 | Yuqori topilma yo'q |
| 18 | Migratsiya | Testcontainers | Bo'sh bazadan va oldingi relizdan yangilanish | Yashil |

## 3. Majburiy test to'plamlari

### 3.1 Engine invariantlari (property-based)

Ixtiyoriy hodisalar ketma-ketligi uchun:

- `0 ≤ cursor ≤ target.length`; kursor hech qachon `auto` pozitsiyada to'xtamaydi.
- `C ≤ T`; `K✓ + K✗ = T` (Tab-indent holatidan tashqari — u ham 1 bosish).
- `0 ≤ accuracy ≤ 100`; `0 ≤ consistency ≤ 100`; `wpm ≥ 0`; `rawWpm ≥ wpm` (erkin rejimda tuzatilmagan xatolar bo'lsa qat'iy katta).
- Stop rejimda yakunda `incorrect` pozitsiya yo'q.
- `replay(exportKeylog(log)) == jonli holat` (encode → decode → replay tengligi).
- `activeMs` monoton; pauza davomida o'zgarmaydi.
- `finished` dan keyingi hodisalar holatni o'zgartirmaydi.
- Bir xil kirish → bir xil chiqish (determinizm).

### 3.2 Engine chekka holat testlari (misollar; hammasi vektor sifatida)

| Guruh | Holatlar |
| --- | --- |
| Indent | Enter'dan keyin avtomatik indentdan sakrash; ketma-ket bo'sh qatorlar; indent kamayishi; strict rejimda probel va Tab bilan; TAB uslubidagi til |
| Backspace | Qator boshidan oldingi qatorga; avtomatik indent ustidan; snippet boshida; stop rejimda; `wordDelete` so'z chegaralari va `\n` |
| Xatolar | 8 ketma-ket xato bloklanishi va Backspace bilan ochilishi; `corrected` holati accuracy'ga ta'siri; oxirgi belgi xato |
| Vaqt | `timeout` da tugallanmagan so'z; pauza va avtomatik pauza `t` si; `next` tanaffusi; `dt` chegaralari |
| Metrika | `m = 0`; < 3 soniya consistency; yaxlitlash `x.xx5`; Tab-indent `C` hisobi; ko'p snippetli `C` |
| Kiritish adapteri (frontend) | Dead key; IME (`isComposing`); AltGr; macOS Option; `Cmd`/`Ctrl` kombinatsiyalari; avtotakror; kirill; emoji (surrogat juftlik); paste/drop/cut bloklanishi; `key='Unidentified'` (touch) |
| Layout | ANSI/ISO/Dvorak/Colemak da bir xil `event.key` → bir xil natija |

### 3.3 Xavfsizlik testlari (avtomatik)

- Har himoyalangan endpoint: tokensiz → 401; noto'g'ri rol → 403/404; boshqa foydalanuvchi resursi → 404 (parametrlangan test barcha `{id}` li yo'llar bo'yicha — yangi endpoint avtomatik qamraladi, OpenAPI'dan o'qib).
- JWT: muddati o'tgan, noto'g'ri imzo, `alg=none`, HS256 ga almashtirilgan, noto'g'ri `aud`/`iss`.
- Refresh: rotation, reuse → oila bekor, bekor qilingandan keyin 401.
- Mass assignment: `PATCH /me` ga `role`, `emailVerified`, `totalXp` yuborilganda 400.
- SQL injection payloadlari qidiruv/saralash/filtr parametrlarida → 400 yoki xavfsiz natija; noma'lum `sort` → 400.
- XSS payloadlari `bio`, `displayName` da → saqlanadi, E2E da skript bajarilmaydi (escaped).
- Rate limit: chegaradan keyin 429 + `Retry-After`.
- Fayl: PNG deb nomlangan HTML/SVG → `INVALID_IMAGE`; 4097 px rasm; 2 MB dan katta; YAML alias bombasi.
- Keylog: gzip bomba (1 MB dan katta ochiladi) → 422; 50 001 hodisa → 422.
- CSRF: `/auth/refresh` boshqa `Origin` bilan → 403.
- Sarlavhalar: CSP, HSTS, nosniff, frame-ancestors — staging'da tekshiriladi.
- Open redirect: `next=//evil.com`, `next=https://evil.com` → e'tiborsiz.

### 3.4 E2E ssenariylari

| # | Ssenariy | AC |
| --- | --- | --- |
| E2E-1 | Mehmon landing demo → natija → ro'yxatdan o'tish → sessiya akkauntda | AC-UM-7 |
| E2E-2 | Email ro'yxat → tasdiqlash havolasi (Mailpit) → login | AC-UM-1, 2 |
| E2E-3 | Onboarding: til tanlash → placement → dashboard | AC-LS-4 |
| E2E-4 | Practice (Java): mashq → natija → "keyingisi" → progress saqlanadi (qayta login'dan keyin ham) | AC-TM-1, AC-LS-2 |
| E2E-5 | **SQL Practice:** SELECT/BEGINNER mashqi, katta harfli kalit so'zlar, river-indent sakrashi | AC-SQL-1…3 |
| E2E-6 | **PL/SQL Practice:** `:=`, `%TYPE`, `/` terminatorli mashq; token statistikasi natijada | AC-PLS-1…5 |
| E2E-7 | Speed Test 60 s: vaqt tugashi, snippet oqimi, natija, tarixda ko'rinadi | AC-TM-2 |
| E2E-8 | Accuracy: 97% dan pastga tushganda tugash | AC-TM-3 |
| E2E-9 | Special Characters + virtual klaviatura keyingi klavish/Shift ko'rsatishi | AC-SC-*, AC-VK-1 |
| E2E-10 | Paste/drop bloklanishi; kirill layout ogohlantirishi; Caps Lock indikatori | AC-TE-10, 11 |
| E2E-11 | Offline: tarmoq uziladi → sessiya tugaydi → tarmoq qaytadi → natija saqlanadi | AC-TE-15 |
| E2E-12 | Analytics: 5 sessiyadan keyin grafiklar; undan oldin bo'sh holat | AC-AN-1, 8 |
| E2E-13 | Tarix → sessiya → replay ijrosi | AC-AN-9, AC-CE-8 |
| E2E-14 | Sozlamalar: tema, lokal (4 til), shrift — qayta yuklashda saqlanadi | AC-UX-1, 10 |
| E2E-15 | Admin: mashq yaratish → validatsiya xatosi → tuzatish → submit → boshqa editor publish → foydalanuvchi ko'radi | AC-EM-1…5, AC-AD-3 |
| E2E-16 | Admin: foydalanuvchini bloklash → u login qila olmaydi | AC-AD-4 |
| E2E-17 | Qulflangan daraja: ochilish sharti ko'rinadi; shart bajarilgach ochiladi | AC-LS-1 |
| E2E-18 | Klaviatura bilan to'liq sikl (sichqonchasiz): dashboard → mashq → natija → keyingisi | AC-UX-6 |
| E2E-19 (R2) | Weakness, Challenge, Sprint; achievement ochilishi; reyting yangilanishi; coach rejasi | AC-TM-5…7, AC-GM-*, AC-AI-* |
| E2E-20 (R2) | Shubhali sessiya (sun'iy tekis ritmli keylog) → reytingga kirmaydi → admin navbatida | AC-SP-20, AC-AD-5 |

E2E'da typing: Playwright `keyboard.press` real klavish hodisalari bilan, belgilar orasida 30–80 ms tasodifiy (seed bilan) kechikish — shubha qoidalariga tushmaslik uchun.

## 4. Acceptance Criteria

Format: **Berilgan** (boshlang'ich holat) / **Qachon** / **Unda** — yoki tekshiriladigan tasdiq. Har AC bitta yoki bir nechta FR'ni yopadi.

### 4.1 User Management

| ID | Mezon | FR |
| --- | --- | --- |
| AC-UM-1 | To'g'ri ma'lumot bilan `register` → 201, foydalanuvchi bazada (`email_verified=false`), tasdiqlash xati yuborilgan (tanlangan lokalda), refresh cookie o'rnatilgan. Band email → 409 `EMAIL_TAKEN`; band username (boshqa registrda) → 409; 9 belgili parol → 400; zaxiralangan username → 409. | UM-1 |
| AC-UM-2 | Tasdiqlash havolasi 24 soat ichida → `email_verified=true`; ikkinchi marta → 410 `TOKEN_USED`; 24 soatdan keyin → 410 `TOKEN_EXPIRED`. Tasdiqlanmagan foydalanuvchi sessiyasi `ranked=false`, `rankedReason=EMAIL_NOT_VERIFIED`. | UM-2 |
| AC-UM-3 | Noto'g'ri parol va mavjud bo'lmagan login bir xil javob (kod, status) beradi; javob vaqtlari farqi statistik ahamiyatsiz (100 so'rov, medianlar farqi < 20%). | UM-3, SP-3 |
| AC-UM-4 | OAuth (stub provayder bilan): yangi email → akkaunt yaratiladi; mavjud tasdiqlangan email → bog'lanadi; tasdiqlanmagan provayder emaili → `OAUTH_EMAIL_UNVERIFIED`. | UM-4 |
| AC-UM-5 | Parol tiklashdan keyin eski refresh token bilan `refresh` → 401; yangi parol bilan login ishlaydi; `forgot-password` mavjud va mavjud bo'lmagan email uchun bir xil 202. | UM-5 |
| AC-UM-6 | Refresh rotation: har `refresh` yangi token beradi; eski token qayta ishlatilsa butun oila bekor (`TOKEN_REUSE_DETECTED`); `DELETE /me/auth-sessions/{id}` dan keyin o'sha qurilma 401 oladi. | UM-6 |
| AC-UM-7 | Mehmon 2 ta `SPEED_TEST` tugatadi → ro'yxatdan o'tadi (`guestToken` bilan) → `GET /sessions` da 2 sessiya; XP berilgan; reyting va rekordda yo'q; takroriy `claim` → `claimed: 0`. Mehmon `PRACTICE` ocholmaydi (403). | UM-7 |
| AC-UM-8 | `PATCH /me` ruxsat etilgan maydonlarni saqlaydi; `role` yuborilsa 400; username 30 kunda ikkinchi marta → `USERNAME_CHANGE_TOO_SOON`. | UM-8 |
| AC-UM-9 | Sozlama o'zgarishi boshqa brauzer sessiyasida (qayta yuklashdan keyin) ko'rinadi; noma'lum kalit → 400. | UM-9 |
| AC-UM-10 | `EDITOR` 2FA'siz admin endpoint'ga → 403 `TWO_FACTOR_REQUIRED`; 2FA yoqib, TOTP bilan kirgach → 200. Noto'g'ri TOTP → `INVALID_TOTP`; ishlatilgan kod qayta → rad. | UM-10 |
| AC-UM-11 | `GET /me/export` foydalanuvchining barcha ma'lumotini (profil, sessiyalar, statistika, XP) qaytaradi. `DELETE /me` → login 30 kun ichida o'chirishni bekor qiladi; muddatdan keyin (soat surilgan test) email/username anonim, sessiyalar o'chgan. | UM-11 |
| AC-UM-12 | Bloklangan foydalanuvchi: login → 403 `ACCOUNT_BLOCKED`; mavjud refresh → 401; reyting ro'yxatida ko'rinmaydi. | UM-12 |

### 4.2 Programming Languages

| ID | Mezon | FR |
| --- | --- | --- |
| AC-PL-1 | `GET /languages` faqat `PUBLISHED` tillarni to'liq, `DRAFT` larni "tez orada" sifatida qaytaradi; har tilda `indent`, `tokenPatterns`, `grammarId` bor. | PL-1, 2 |
| AC-PL-2 | Shart bajarilmagan tilni `publish` → 409 `LANGUAGE_NOT_READY`, `meta.missing` da aniq yetishmovchiliklar. Shart bajarilganda → `PUBLISHED`. | PL-3 |
| AC-PL-3 | `PUT /me/languages`: 0 til → 400; 2 ta `primary` → 400; tilni olib tashlab qayta qo'shganda progress va statistika avvalgidek. | PL-4 |
| AC-PL-4 | Har til uchun statistika, rank, rekord, reyting alohida: Java sessiyasi PL/SQL ko'rsatkichlarini o'zgartirmaydi. | PL-6 |
| AC-PL-5 | **Kod o'zgarishisiz yangi til:** integratsiya testida admin API orqali yangi til (`lua`), 8 mavzu va kerakli mashqlar yaratiladi va nashr qilinadi → foydalanuvchi shu tilda sessiya ochadi, yakunlaydi, statistika ko'radi. Backend/frontend kodi o'zgarmagan. | PL-5 |
| AC-PL-6 | `sql` va `plsql` da umumiy `exercise_key`, mavzu yoki statistika qatori yo'q (baza so'rovi bilan tekshiriladi). | PL-7 |

### 4.3 Language Learning Structure

| ID | Mezon | FR |
| --- | --- | --- |
| AC-LS-1 | Yangi foydalanuvchi: har mavzuda BEGINNER `AVAILABLE`, qolganlari `LOCKED`. Qulflangan darajaga `POST /sessions` → 409 `LEVEL_LOCKED` + `completed/required`. | LS-2, 3 |
| AC-LS-2 | 6 mashqli darajada 5 tasi (≥ 80%) accuracy ≥ 95% bilan tugatilganda → `COMPLETED`, keyingi daraja `AVAILABLE`, `TopicLevelCompleted` nashr qilinadi. 94% accuracy bilan tugatilgan mashq hisobga kirmaydi. | LS-2 |
| AC-LS-3 | Barcha mashqlar ≥ 98% → `MASTERED`. Shu darajaga yangi mashq nashr qilinsa → `COMPLETED` ga qaytadi (pastga emas). | LS-2 |
| AC-LS-4 | Placement: 4 natija kombinatsiyasi `BL-LS-3` jadvaliga mos holatlar beradi. | LS-4 |
| AC-LS-5 | Til progressi foizi `BL-LS-4` formulasiga teng. | LS-6 |
| AC-LS-6 | Mashqning yangi versiyasi nashr qilingach foydalanuvchining "tugatilgan" belgisi saqlanadi. | LS-7 |

### 4.4 SQL Training

| ID | Mezon | FR |
| --- | --- | --- |
| AC-SQL-1 | `sql` tilida 16 mavzu `06` §3.3 tartibida; R1 da birinchi 8 tasi nashr qilingan, har birida 4 daraja × ≥ 5 mashq. | SQL-1 |
| AC-SQL-2 | Kontent CI: har `sql` snippet PostgreSQL 16 da (namunaviy sxema) `PREPARE` dan xatosiz o'tadi; `oracle` tegi borlari Oracle Free'da. Tegsiz snippetda `NVL(`, `::`, `ILIKE` → validatsiya xatosi. | SQL-2, 4 |
| AC-SQL-3 | Formatlash linti: kalit so'zlar UPPER, identifikatorlar lower, indent qoidasi — buzilgan snippet rad etiladi. E2E: river-tekislangan qatorda kursor birinchi bo'shliq-bo'lmagan belgiga tushadi. | SQL-3 |
| AC-SQL-4 | SQL sessiyasi natijasida "Registr" turkumi ko'rsatkichi bor; `select` (kichik) yozilganda xato sanaladi. | SQL-5 |

### 4.5 PL/SQL Training

| ID | Mezon | FR |
| --- | --- | --- |
| AC-PLS-1 | `plsql` tilida 18 mavzu `06` §4.4 tartibida; R1 da birinchi 8 tasi, har birida 4 daraja × ≥ 5 mashq. | PLS-1 |
| AC-PLS-2 | Validator: `$$`, `LANGUAGE plpgsql`, `RAISE NOTICE`, `RETURNS`, `PERFORM`, `DELIMITER`, `DECLARE @x` ni o'z ichiga olgan snippet → `FORBIDDEN_CONSTRUCT`. | PLS-2 |
| AC-PLS-3 | Kontent CI: har `plsql` snippet Oracle Database Free'da kompilyatsiya qilinadi; xatoli snippet build'ni to'xtatadi. | PLS-6 |
| AC-PLS-4 | Qamrov hisoboti: `06` §4.3 dagi har konstruksiya (regex ro'yxati) kamida 10 nashr qilingan snippetda (to'liq dastur, R2). R1 da — tegishli 8 mavzu konstruksiyalari ≥ 5 snippetda. | PLS-3 |
| AC-PLS-5 | Engine: `v_x := 1;` da `:=` bitta pattern sifatida statistikaga tushadi; `employees.salary%TYPE` da `%TYPE` — pattern; blok oxiridagi `/` alohida qator sifatida teriladi va sessiya shundan keyin tugaydi. | PLS-4, 5 |
| AC-PLS-6 | Formatlash linti: prefikslar, `END <nom>;`, parametr rejimlari — buzilgan snippet uchun ogohlantirish/xato. | PLS-7 |

### 4.6 Typing Engine

| ID | Mezon | FR |
| --- | --- | --- |
| AC-TE-1 | Barcha umumiy vektorlar (≥ 120) Vitest va JUnit'da o'tadi; holat va metrikalar aynan mos. | TE-1…9, 13, 14 |
| AC-TE-2 | To'g'ri/xato/tuzatilgan belgilar `09` §4 bo'yicha ko'rinadi (vizual test); joriy pozitsiyada kursor. | TE-2, 3 |
| AC-TE-3 | Ma'lum keylog uchun WPM, raw WPM, CPM, accuracy, consistency qo'lda hisoblangan qiymatlarga 2 kasr xonagacha teng (kamida 10 ta hujjatlashtirilgan misol). | TE-4 |
| AC-TE-4 | Taymer birinchi belgidan boshlanadi; 20 s pauza faol vaqtga kirmaydi; Speed Test 60 s da `elapsedMs = 60000`. | TE-5, 6 |
| AC-TE-5 | Enter'dan keyin kursor avtomatik indentdan sakraydi; strict rejimda Tab bitta indent birligini to'ldiradi va 1 bosish sanaladi. | TE-7, 8 |
| AC-TE-6 | Maxsus belgilar to'plami `{} [] () <> ; : = := => -> \|\| && ! % * / + - _ .` ning har biri to'g'ri qabul qilinadi va statistikada o'z turkumiga tushadi. | TE-9 |
| AC-TE-7 | Server va klient metrikalari bir xil keylog'da farq qilmaydi (`engine_mismatch_total = 0` E2E davomida). | TE-13, NFR-11 |
| AC-TE-8 | `clientMetrics` ni soxtalashtirish (`wpm: 300`) natijaga ta'sir qilmaydi — javobda server qiymati. | TE-13, 14 |
| AC-TE-9 | UI'dagi WPM = `round(server wpm)`, accuracy = 1 kasr xona; hech qayerda yuqoriga "yaxshilash" yo'q (unit test: formatlash funksiyasi). | TE-14 |
| AC-TE-10 | Paste, drop, cut hodisalari matn kiritmaydi; kontekst menyu ochilmaydi. | TE-10 |
| AC-TE-11 | IME kompozitsiyasi, dead key, kirill, emoji: belgi qabul qilinmaydi, xato sanalmaydi, taymer boshlanmaydi, ogohlantirish ko'rinadi. | TE-11 |
| AC-TE-12 | Kiritish adapteri testlari: Windows AltGr, macOS Option, `Cmd+Backspace`, `Ctrl+Backspace`, avtotakror — `07` §4 jadvaliga mos. | TE-12 |
| AC-TE-13 | Erkin rejimda 8 ketma-ket xatodan keyin 9-belgi qabul qilinmaydi; Backspace'dan keyin qabul qilinadi. | TE-1 |
| AC-TE-14 | Qayta boshlash (`Tab`+`Enter`): eski sessiya `ABANDONED`, yangi sessiya yangi `sessionId` bilan. | TE-6 |
| AC-TE-15 | Tarmoq uzilgan holda tugagan sessiya natijasi aloqa tiklangach saqlanadi; 24 soatdan keyin yuborilsa `SESSION_EXPIRED`. | TE-15 |
| AC-TE-16 | Keylog validatsiyasi: `ΣΔt` server vaqtidan 2 s dan ortiq katta → `TIMING_MISMATCH`; noto'g'ri header → `KEYLOG_INVALID`. | TE-13 |

### 4.7 Training Modes

| ID | Mezon | FR |
| --- | --- | --- |
| AC-TM-1 | Practice: stop-on-error (xatoda kursor siljimaydi); pauza ishlaydi; 15 s harakatsizlikda avtomatik pauza. `practiceStopOnError=false` da erkin rejim. | TM-1 |
| AC-TM-2 | Speed Test: snippetlar ranked pool'dan; oqim uzluksiz; aynan limitda tugaydi; mehmon uchun ham ishlaydi. | TM-2 |
| AC-TM-3 | Accuracy: 20+ bosishdan keyin accuracy < 97% → sessiya tugaydi, `completionPct < 100`; master: birinchi xatoda. | TM-3 |
| AC-TM-4 | Special Characters: generatsiya qilingan matn 140–160 belgi, `BL-EM-1` 1–6-qoidalariga mos, tanlangan turkum zichligi ≥ 25%; bir xil seed → bir xil matn. | TM-4 |
| AC-TM-5 | Challenge: vaqt chegarasi `BL-TM-6` formulasiga teng; vaqt tugaganda sessiya tugaydi. | TM-5 |
| AC-TM-6 | Weakness: profil bo'sh → 409 `WEAKNESS_PROFILE_EMPTY`; aks holda 3 snippet, birinchisi generatsiya qilingan va zaif belgilarni o'z ichiga oladi. | TM-6 |
| AC-TM-7 | Code Sprint: 5 snippet, darajalar `BL-TM-5` ga mos; 5/5 da +30 XP. | TM-7 |
| AC-TM-8 | Ochilmagan sessiyaga yoki boshqa foydalanuvchi sessiyasiga `complete` → 404; `completeToken` noto'g'ri → 401. | TM-8 |
| AC-TM-9 | 25 mashqli pool'da ketma-ket 20 sessiyada takror yo'q; pool 5 ta bo'lsa — eng eski ko'rilgani qaytadi. | TM-9 |
| AC-TM-10 | Natija javobida `metrics`, `wpmSeries`, `weakTokens`, `slowLines`, `rewards`, `progress`, `next` bor va UI ularni ko'rsatadi. | TM-10 |
| AC-TM-11 | `K✓+K✗ < 20` yoki < 5 s sessiya → 422 `SESSION_TOO_SHORT`, XP yo'q. `complete` ikki marta → bir xil javob, XP bir marta. | BL-TM-2, 3 |

### 4.8 Special Characters · 4.9 Virtual Keyboard · 4.10 Code Editor

| ID | Mezon | FR |
| --- | --- | --- |
| AC-SC-1 | Har bosiladigan ASCII belgi aynan bitta turkumga tegishli (unit test, 95 belgi); frontend va backend moslamasi bir xil (umumiy JSON'dan). | SC-1 |
| AC-SC-2 | Natijada `specialAccuracy` bor va faqat maxsus belgilar bosishlaridan hisoblangan. | SC-4 |
| AC-SC-3 | Chalkashlik: `)` o'rniga `0` 3 marta bosilgach `confusions[")"]["0"] = 3`. | SC-5 |
| AC-SC-4 | ≥ 30 kuzatuvli foydalanuvchida tavsiya — eng yuqori zaiflik balli turkum; aks holda standart tartib. | SC-6 |
| AC-VK-1 | Kutilgan belgi `{` (ANSI): `BracketLeft` va `ShiftRight` ajratilgan; `a` da faqat `KeyA`. 4 layoutning har biri uchun `charMap` barcha 95 belgini qamraydi. | VK-1, 2 |
| AC-VK-2 | macOS user-agent'da modifikator yorliqlari `control/option/command`; Windows'da `Ctrl/Win/Alt`. | VK-2 |
| AC-VK-3 | Ko'rsatish qoidasi `BL-VK-1` jadvaliga mos (parametrlangan test); < 768 px da yo'q. | VK-5 |
| AC-VK-4 | Klaviaturadagi klavishni sichqoncha bilan bosish engine holatini o'zgartirmaydi. | VK-6 |
| AC-VK-5 | Dvorak'da yozayotgan (ANSI tanlangan) foydalanuvchiga 20 belgidan keyin layoutni almashtirish taklif qilinadi. | VK-8 |
| AC-VK-6 | Heatmap: < 30 kuzatuvli klavish shtrixlangan; tooltip qiymatlari API javobiga mos. | VK-7 |
| AC-CE-1 | Muharrirda qator raqamlari, joriy qator foni, indent yo'riqchilari, syntax highlighting (dark va light) — vizual test, 5 til. | CE-1, 2 |
| AC-CE-2 | Tema almashganda qayta tokenizatsiya chaqirilmaydi (spy). Grammatika yuklanmasa typing ishlaydi (highlight'siz). | CE-2 |
| AC-CE-3 | Joriy qator doim 3-o'rinda (snippet boshidan va oxiridan tashqari); bitta klavishda ≤ 2 qator qayta render (Profiler testi). | CE-4 |
| AC-CE-4 | `(` yozilganda `)` avtomatik paydo bo'lmaydi; matn tanlab bo'lmaydi; undo yo'q. | CE-6 |
| AC-CE-5 | Har muharrir sozlamasi (`05` §5.17) darhol qo'llanadi va saqlanadi. | CE-7 |
| AC-CE-6 | Juft qavs ajratilishi: kursor `(` oldida → mos `)` belgilangan. | CE-5 |
| AC-CE-7 | Klavish → kadr p95 ≤ 16 ms (`17` §2 testi). | CE, SP-60 |
| AC-CE-8 | Replay: 1×/2×/4×, pauza, ±5 s; oxirgi holat sessiya natijasiga mos. O'chirilgan keylog → `REPLAY_NOT_AVAILABLE`. | CE-8 |
| AC-CE-9 | `SnippetEditor`: validatsiya xatolari to'g'ri qator/ustunda; hisoblangan metrikalar server javobiga mos; preview sessiya yaratmaydi. | CE-9 |

### 4.11 Analytics · 4.12 AI Coach

| ID | Mezon | FR |
| --- | --- | --- |
| AC-AN-1 | Ma'lum sessiyalar to'plami (fixture, 30 sessiya) uchun barcha KPI qo'lda hisoblangan qiymatlarga teng; accuracy bosishlar bo'yicha. | AN-1, 2 |
| AC-AN-2 | Vaqt qatori: kun chegarasi foydalanuvchi timezone'ida (UTC+5 da 23:30 va 00:30 sessiyalari turli kunlarda). | AN-3 |
| AC-AN-3 | `group=CORE` da `SPECIAL_CHARS` sessiyalari WPM'ga ta'sir qilmaydi. | AN-4 |
| AC-AN-4 | Agregat ishlovchisi idempotent: bir hodisa 2 marta → hisoblagichlar 1 marta oshadi. `rebuild` dan keyin agregatlar aynan bir xil. | AN-1 |
| AC-AN-5 | Zaiflik profili: sun'iy ma'lumotda (bitta belgi 20% xato, qolganlari 2%) o'sha belgi profilda birinchi; < 30 kuzatuvli belgi kirmaydi; < 8 qator → bo'sh profil. | AN-5, 7 |
| AC-AN-6 | Eng zaif til: `ratio < 0.75` bo'lgan til qaytadi; bitta tilda — `null`. | AN-7 |
| AC-AN-7 | Faollik kalendari 365 kun; intensivlik chegaralari `10` §A.4 ga mos. | AN-6 |
| AC-AN-8 | < 5 sessiya: `enoughData=false`, UI'da `EmptyState`, hech qanday namunaviy grafik yo'q. | AN-8 |
| AC-AN-9 | Tarix: filtrlar va cursor sahifalash to'g'ri (takror/tushib qolish yo'q, 250 sessiyada); boshqa foydalanuvchi sessiyasi → 404. | AN-9 |
| AC-AI-1 | LLM o'chiq holda reja, maslahat, maqsad, hisobot to'liq ishlaydi (shablon matn, 4 lokal). | AI-1, 6 |
| AC-AI-2 | Reja: `BL-AI-1` qaror jadvalining har shoxi uchun test (5 dan kam sessiya; past accuracy; zaif belgilar; plato). 3 vazifa, takrorsiz; kun davomida o'zgarmaydi. | AI-2 |
| AC-AI-3 | Vazifa mos sessiya tugaganda bajarilgan bo'ladi; 3/3 da +20 XP bir marta. | AI-2 |
| AC-AI-4 | Maslahat: `BL-AI-2` ustuvorligi; oxirgi 5 sessiyada takrorlanmaydi. | AI-3 |
| AC-AI-5 | Maqsad: chiziqli o'suvchi sun'iy qatorda ETA kutilgan sanaga ±1 kun; tekis qatorda `STALLED`; < 10 sessiya `INSUFFICIENT_DATA`; erishilganda `ACHIEVED` + 100 XP. | AI-4 |
| AC-AI-6 | Haftalik hisobot payload'i `10` §B.5 sxemasiga mos; < 3 sessiyali hafta uchun yaratilmaydi. | AI-5 |
| AC-AI-7 | LLM so'roviga yuborilgan matnda email, username, snippet kodi yo'q (contract test, stub `LlmClient`). | AI-7 |
| AC-AI-8 | LLM noto'g'ri JSON / timeout / URL li matn qaytarsa → shablon matn; foydalanuvchi xato ko'rmaydi; `llm_calls.status` to'g'ri. | AI-8 |
| AC-AI-9 | `coachEnabled=false`: `GET /coach/plan` → 403 `COACH_DISABLED`; natijada `coachTip=null`. | AI-9 |

### 4.13 Gamification

| ID | Mezon | FR |
| --- | --- | --- |
| AC-GM-1 | XP formulasi: jadvaldagi 12 ta misol (har rejim, har `accFactor` bo'lagi chegarasi: 84.99, 85, 94.99, 95, 97.99, 98, 99.99, 100) kutilgan qiymatga teng. | GM-1 |
| AC-GM-2 | Takror: shu mashq 24 soatda 2-marta 50%, 3-marta 10%. Kunlik chegara: 3 000 dan keyin 0, `capped=true`. | GM-1 |
| AC-GM-3 | `Σ xp_transactions.amount = user_progress.total_xp` har testdan keyin (invariant). | GM-1 |
| AC-GM-4 | Level chegaralari `11` §2 jadvaliga teng; level oshganda javobda `levelUp`. | GM-2 |
| AC-GM-5 | Rank: 10-sessiyadan keyin beriladi; chegaralar; pasayish gisterezisi (2 WPM). | GM-3 |
| AC-GM-6 | Streak: 179 s → hisoblanmaydi, 180 s → hisoblanadi; ketma-ket kun +1; 1 kun o'tkazib yuborish + freeze → davom etadi, `freezes_left−1`; freeze'siz → 1; UTC+5 va UTC−8 da kun chegarasi; 7-kunda +50 XP. | GM-4 |
| AC-GM-7 | Rekord: accuracy 94.9% → rekord emas; birinchi yozuv bonus bermaydi; yaxshilanganda +25. | GM-5 |
| AC-GM-8 | Achievement: har `metric` turi uchun test; bosqichlar ketma-ket ochiladi; XP bir marta; yashirin achievement ochilmaguncha nomi chiqmaydi. | GM-6 |
| AC-GM-9 | Challenge: scheduler kuniga aynan bitta `DAILY` yaratadi (2 instansda ham — ShedLock); progress va mukofot bir marta; `DAILY_SNIPPET` 3 urinish. | GM-7 |
| AC-GM-10 | Reyting: yaxshiroq natija yozuvni yangilaydi, yomonrog'i yo'q; tartib qoidasi (teng WPM'da accuracy, keyin vaqt); `me.rank` to'g'ri (10 000 yozuvda); kirish shartlarining har biri alohida test bilan. | GM-8 |
| AC-GM-11 | DevScore = eng yuqori 3 ta ST60 rekord yig'indisi. | GM-9 |
| AC-GM-12 | Musobaqa: urinishlar chegarasi; tugagach jadval muzlaydi; mukofotlar 24 soatdan keyin bir marta. | GM-10 |

### 4.14 Exercise Management · 4.15 User Dashboard · 4.16 UI/UX · 4.17 Admin

| ID | Mezon | FR |
| --- | --- | --- |
| AC-EM-1 | `BL-EM-1` ning har 12 qoidasi uchun buzuvchi misol → mos `rule` kodi va to'g'ri `line/col`. | EM-3 |
| AC-EM-2 | Hisoblangan maydonlar (`char_count`, `special_ratio`, `char_freq`, `nesting_depth`, `expected_seconds`) ma'lum snippetlar uchun kutilgan qiymatga teng. | EM-4 |
| AC-EM-3 | Muallif o'z mashqini `publish` → 409 `SELF_REVIEW_NOT_ALLOWED`; noto'g'ri holat o'tishi → `INVALID_STATUS_TRANSITION`. | EM-2 |
| AC-EM-4 | Nashr qilingan mashq kodi tahrirlansa yangi versiya (`DRAFT`) yaratiladi; eski sessiyalar eski versiyani ko'rsatadi; nashrdan keyin eski `ARCHIVED`. | EM-5 |
| AC-EM-5 | Import: 10 fayl (7 to'g'ri, 2 xato, 1 dublikat) → hisobot aynan shunday; takroriy import → 0 yangi. `dryRun=true` bazaga yozmaydi. | EM-6 |
| AC-EM-6 | Qamrov matritsasi soni baza so'roviga teng; tarjimasi to'liq bo'lmagan mashq `publish` → `TRANSLATIONS_INCOMPLETE`. | EM-7 |
| AC-EM-7 | Shikoyat: bir foydalanuvchi bir mashqqa bir marta; admin navbatida ko'rinadi. | EM-8 |
| AC-UD-1 | `GET /me/dashboard` bitta so'rov; ≤ 8 SQL; barcha bloklar ma'lumoti bazadan (0 sessiyali foydalanuvchida `isNewUser=true`, soxta qiymat yo'q). | UD-1, 8 |
| AC-UD-2 | "Davom ettirish" `BL-UD-1` ning 4 shoxi uchun to'g'ri. | UD-2 |
| AC-UD-3 | Streak "xavf ostida": mahalliy 20:00 dan keyin va bugun bajarilmagan bo'lsa `atRisk=true`. | UD-3 |
| AC-UD-4 | Til kartochkasi: sparkline 14 nuqta, sessiyasiz kunlar `null`. | UD-4 |
| AC-UX-1 | Tema: dark/light/system; almashtirish qayta yuklamasiz; qayta yuklashda miltillash yo'q (birinchi kadr to'g'ri temada — screenshot). | UX-1 |
| AC-UX-2 | 320, 768, 1280, 1920, 3840 px da har sahifa: gorizontal scroll yo'q, element ustma-ust tushmaydi, matn kesilmaydi (vizual test + `scrollWidth ≤ clientWidth` tekshiruvi). | UX-2 |
| AC-UX-3 | Navigatsiya: ≥ 1280 yon panel; 768–1279 yig'ilgan; < 768 pastki tab bar. | UX-3 |
| AC-UX-4 | Har sahifa 4 holatda render qilinadi (yuklanish, bo'sh, xato, ma'lumotli) — komponent testlari. Xato holatida "Qayta urinish" ishlaydi. | UX-4 |
| AC-UX-5 | Toast: 5 s da yo'qoladi; xato toast qo'lda yopiladi; `aria-live`. | UX-5 |
| AC-UX-6 | Sichqonchasiz to'liq sikl (E2E-18); modal `Esc` bilan yopiladi, fokus qaytadi; fokus halqasi ko'rinadi; axe 0 jiddiy xato. | UX-6 |
| AC-UX-7 | `Ctrl+K` palitra: til, mavzu, sahifa qidiradi va o'tadi. | UX-7 |
| AC-UX-8 | `prefers-reduced-motion`: animatsiya davomiyligi 0. | UX-8 |
| AC-UX-9 | Har interaktiv element (tugma, havola, dropdown, modal) E2E'da bosilganda kutilgan natija beradi — "o'lik" tugma yo'q (crawler testi: sahifadagi har `button`/`a` ga handler yoki `href` bog'langan). | UX, QS-1 |
| AC-UX-10 | 4 lokalda: barcha kalitlar mavjud; asosiy 8 ekranda matn sig'adi. | UX-10 |
| AC-AD-1 | `USER` uchun `/admin/**` → 404 (UI) / 403 (API); `EDITOR` foydalanuvchilar endpoint'iga → 403. | AD-1 |
| AC-AD-2 | Til CRUD va nashr (AC-PL-2). | AD-2 |
| AC-AD-3 | Mashq to'liq sikli (E2E-15). | AD-3 |
| AC-AD-4 | Bloklash; o'z rolini o'zgartirish → `CANNOT_CHANGE_OWN_ROLE`; oxirgi admin → `LAST_ADMIN`. | AD-4 |
| AC-AD-5 | `CONFIRM`: sessiya `REJECTED`, XP qaytarilgan (manfiy tranzaksiya), reyting yozuvi qayta hisoblangan. `CLEAR`: `ranked` tiklanadi. | AD-5 |
| AC-AD-6 | Har admin yozuvchi amali `audit_log` da (aktor, amal, eski/yangi qiymat); `audit_log` ni API orqali o'zgartirib bo'lmaydi. | AD-8 |

### 4.18–4.21 Backend, Security & Performance, Testing, Quality Standards

| ID | Mezon | FR |
| --- | --- | --- |
| AC-BE-1 | Bo'sh bazada migratsiyalar o'tadi; Hibernate `validate` yashil; oldingi relizdan yangilanish o'tadi. | BE-2 |
| AC-BE-2 | Modulith `verify()` va ArchUnit yashil. | BE-3 |
| AC-BE-3 | Generatsiya qilingan OpenAPI `docs/openapi.yaml` bilan bir xil; har endpoint `14` dagi jadvalda bor (skript). | BE-4 |
| AC-BE-4 | Har xato javobi `ProblemDetail` sxemasiga mos, `code` bilan. | BE-5 |
| AC-BE-5 | Sahifalash/saralash qoidalari (`14` §1.4): `limit=101` → 400 yoki 100 ga kesiladi (hujjatdagidek — kesiladi); noma'lum `sort` → 400. | BE-6 |
| AC-BE-6 | Hodisa ishlovchisi sun'iy xato berganda javob muvaffaqiyatli, hodisa qayta uriniladi va oxir-oqibat qayta ishlanadi; ikki marta ishlansa ham natija bir xil. | BE-7, 8 |
| AC-BE-7 | Parallel 2 ta `complete` bitta sessiyaga → bitta natija, XP bir marta. | BE-7 |
| AC-BE-8 | So'rovlar soni chegaralari (`17` §3) — testda. | SP |
| AC-SP-1…19 | `16` dagi FR-SP-1…52 — §3.3 xavfsizlik testlari to'plami bilan (har test nomida FR-SP ID). | SP |
| AC-SP-20 | Tekis ritmli (CV < 0.15) sun'iy keylog → `suspicion ≥ 50`, `ranked=false`, admin navbatida. | SP |
| AC-SP-21 | `17` §1 SLO lari k6 hisobotida bajarilgan (hisobot artefakt sifatida saqlanadi). | SP-60…69 |
| AC-SP-22 | Lighthouse CI: ommaviy sahifalar 4 kategoriyada mo'ljaldan past emas (3 o'lchov mediani; hisobot artefakt). | SP-68 |
| AC-QA-1 | `ac-coverage` skripti: bu hujjatdagi har `AC-*` ID uchun ≥ 1 test mavjud va yashil. | QA |
| AC-QS-1 | CI: `main` kodida `TODO|FIXME|XXX|HACK` (ruxsat: `TODO(OQ-n)`), `*Mock*` klasslar, `dangerouslySetInnerHTML`, JSX literal matn — 0. | QS-1, 2 |
| AC-QS-2 | Qamrov chegaralari (`FR-QS-3`) CI'da majburiy. | QS-3 |
| AC-QS-3 | Brauzer matritsasi (NFR-5) E2E yashil. | QS-6 |

### 4.22 Qo'shimcha AC'lar (qolgan FR'lar)

| ID | Mezon | FR |
| --- | --- | --- |
| AC-UM-13 | (R3) Follow: ochiq profilni kuzatish/bekor qilish idempotent; o'zini kuzatish → 400; yopiq profil → 404; `scope=following` reytingida faqat kuzatilganlar va o'zi. | UM-13 |
| AC-LS-7 | Ierarxiya: har mashq aynan bitta til, mavzu (SPECIAL_CHARS dan tashqari) va darajaga tegishli (DB `CHECK` va API javobi); mavzular `sort_order` bo'yicha qaytadi. | LS-1 |
| AC-LS-8 | Mavzu sahifasi: daraja tablari, har mashqda eng yaxshi WPM/accuracy/tugatilgan belgisi `user_exercise_best` ga mos; "keyingi tavsiya" — birinchi tugatilmagan mashq. | LS-5 |
| AC-TE-17 | (R3) Touch kiritish: `key='Unidentified'` holatida `beforeinput` orqali belgi qabul qilinadi; sessiya `inputDevice=TOUCH`, `ranked=false`, `rankedReason=TOUCH_INPUT`. | TE-16 |
| AC-SC-5 | Har til uchun `token_patterns` va `special_templates` mavjud; Special Characters mashqi faqat shu tilning shablon va lug'atidan foydalanadi (Java mashqida `:=` yo'q, PL/SQL mashqida `=>` va `:=` bor). | SC-2 |
| AC-SC-6 | Generatsiya: real qatorlar ulushi 55–65%, shablon qatorlari 35–45% (pool yetarli bo'lsa); pool bo'sh → faqat shablonlar; ikkalasi ham yo'q → `NO_EXERCISES_AVAILABLE`. | SC-3 |
| AC-VK-7 | Barmoq yo'riqnomasi yoqilganda klavishlar `08` §4.2 moslamasi bo'yicha 9 zonaga bo'yalgan; o'chirilganda — bir xil fon. | VK-3 |
| AC-VK-8 | Bosilgan klavish 120 ms yonadi: to'g'ri — accent, xato — error (bosilgan `event.code` klavishida); keyingi klavish belgisi saqlanadi. | VK-4 |
| AC-EM-8 | Mashq maydonlari to'liq: til, mavzu, daraja, tur, sarlavha, 4 lokal izoh, kod, teglar, kutilgan davomiylik (tahrirlanadi), ranked pool, manba — admin API orqali saqlanadi va qaytadi; majburiy maydonsiz → 400. | EM-1 |
| AC-EM-9 | (R2) Mashq statistikasi: urinishlar, o'rtacha WPM/accuracy, tashlab ketish ulushi — fixture sessiyalardan qo'lda hisoblangan qiymatga teng. | EM-9 |
| AC-UD-5 | Dashboard XP paneli: `totalXp`, `level`, `xpIntoLevel`, `xpToNext` `11` §2 formulasiga mos. | UD-5 |
| AC-UD-6 | Oxirgi 5 sessiya — vaqt bo'yicha kamayuvchi, faqat `COMPLETED`, faqat o'ziniki. | UD-6 |
| AC-UD-7 | (R2) Kunlik challenge va Coach rejasi dashboard'da; feature flag o'chiq bo'lsa `null` va UI'da blok ko'rinmaydi. | UD-7 |
| AC-GM-13 | (R3) Jonli poyga: 2–8 ishtirokchi; holatlar `LOBBY → COUNTDOWN → RUNNING → FINISHED`; WebSocket progressi ≤ 500 ms; yakuniy natija REST `complete` dan (WebSocket ma'lumoti natijaga ta'sir qilmaydi). To'liq AC — OQ-8 spetsifikatsiyasi bilan. | GM-11 |
| AC-AD-7 | (R2) Achievement, challenge shabloni, musobaqa CRUD: yaratilgan achievement kod o'zgarishisiz ishlaydi (yangi qoida bilan ochiladi); musobaqa holatlari scheduler bilan o'tadi. | AD-6 |
| AC-AD-8 | (R2) Mahsulot metrikalari: fixture kun uchun `dau`, `activated_users`, `sessions_*` qo'lda hisoblanganga teng. | AD-7 |
| AC-AD-9 | (R2) Shikoyatlar navbati: `OPEN` lar ro'yxati, `resolve` → `RESOLVED`/`DISMISSED`, audit yozuvi. | AD-9 |
| AC-BE-9 | Brauzer xotirasida faqat ruxsat etilgan kalitlar: E2E oxirida `localStorage`/`sessionStorage`/IndexedDB tarkibi oq ro'yxat bilan solishtiriladi (`guestToken`, UI sozlamalari, yuborilmagan natija navbati). Statistika, XP, progress — yo'q. | BE-1 |
| AC-QS-4 | WCAG 2.1 AA: axe (avtomatik) + `21` §4 qo'lda ekran o'quvchi sinovi. | QS-4 |
| AC-QS-5 | Lighthouse mo'ljallari (= AC-SP-22). | QS-5 |
| AC-QS-6 | DoD: har PR tavsifida AC ID'lari, 4 lokal, hujjat yangilanishi belgilari — PR shablonidagi majburiy checklist; CI `pr-checklist` qadami bo'sh qolganini rad etadi. | QS-7 |
| AC-QS-7 | OpenAPI o'zgarishi `docs/14` da aks etmagan bo'lsa (`openapi-diff` + `14` dagi endpoint jadvali skripti) — build to'xtaydi. | QS-8 |
| AC-QS-8 | Sinov xatosi (frontend va backend) xato kuzatuv tizimida 1 daqiqa ichida paydo bo'ladi (staging smoke). | QS-9 |

## 5. Test ma'lumotlari

- **Fabrikalar:** `UserFactory`, `SessionFactory`, `ExerciseFactory`, `KeylogBuilder` (inson ritmiga o'xshash intervallar, seed bilan) — faqat test manbalarida.
- **Kontent fixture:** `src/test/resources/content/` — 2 til, 8 mavzu, daraja boshiga 6 mashq (haqiqiy, validatsiyadan o'tgan snippetlar).
- **Vaqt:** backend — `Clock` inject (`MutableClock` testda); frontend — `vi.useFakeTimers()`; E2E — Playwright `clock` API.
- **Email:** Mailpit (lokal, CI, staging). **OAuth:** WireMock stub provayder. **LLM:** stub `LlmClient`.

## 6. CI bosqichlari va darvozalar

| Bosqich | Qachon | Tarkib | Vaqt mo'ljali |
| --- | --- | --- | --- |
| PR | Har push | Lint, typecheck, unit, vektorlar, arxitektura, integratsiya, API, komponent, E2E (Chromium), a11y, size-limit, i18n, kontent validatsiya (o'zgargan fayllar), SAST, secret skan | ≤ 15 daq |
| `main` | Merge | PR + E2E 3 brauzer, vizual, Lighthouse CI, to'liq kontent kompilyatsiyasi, dependency skan, image skan, staging deploy | ≤ 30 daq |
| Tungi | Cron | k6 yuklama (staging), ZAP baseline, migratsiya yangilanish testi, to'liq vizual matritsa | — |
| Reliz | Teg | `21` nazorat ro'yxati; hisobotlar artefakt | — |

Yashil bo'lmagan bosqich — merge/deploy yo'q. Beqaror (flaky) test: 2 marta qayta urinish ruxsat, lekin beqarorlik hisoblanadi va haftalik tuzatiladi; `skip` faqat `OQ`/issue havolasi bilan.
