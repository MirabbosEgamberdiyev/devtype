# 11. Gamification va Leaderboard qoidalari

Barcha mukofotlar faqat server tasdiqlagan (`COMPLETED`) sessiyalardan. XP — ledger (`gamification.xp_transactions`); `user_progress.total_xp` shu ledger yig'indisi va har doim undan tiklanadi. Konstantalar: `00` §4.7.

## 1. XP (BL-GM-1)

```text
xp(session) = round( (C / 10) × modeCoef × diffCoef × accFactor × repeatFactor ) + bonuses
```

| Omil | Qoida |
| --- | --- |
| `C` | Server hisoblagan to'g'ri belgilar (`07` §6) |
| `modeCoef` | `00` §4.4 |
| `diffCoef` | B 1.0 · I 1.25 · A 1.5 · E 2.0. Ko'p snippetli sessiya: har snippetning yozilgan to'g'ri belgilari bo'yicha vaznli o'rtacha. `SPECIAL_CHARS` (generatsiya): 1.0. `SPEED_TEST` (ranked pool): 1.25 |
| `accFactor` | `00` §4.7 — bo'lakli funksiya, accuracy 2 kasr xonali qiymatdan |
| `repeatFactor` | Faqat bitta snippetli rejimlarda (PRACTICE, ACCURACY, CHALLENGE): shu `exercise_key` bo'yicha foydalanuvchining oxirgi 24 soatdagi `COMPLETED` sessiyalari soni `n` (joriyni hisobga olmay): `n=0`→1.0, `n=1`→0.5, `n≥2`→0.1. Boshqa rejimlarda 1.0 |
| Bonuslar | `+25` shaxsiy rekord (§5) · `+10` kunning (foydalanuvchi tz) birinchi `COMPLETED` sessiyasi · `+30` CODE_SPRINT 5/5 tugatilgan |

Qoidalar:
- `completionPct < 100` sessiya ham XP oladi (yozilgan `C` bo'yicha), bonuslarsiz (kun birinchi sessiyasi bonusi bundan mustasno).
- `REJECTED`, `ABANDONED`, mehmon sessiyasi (claim'gacha) — 0 XP.
- `ranked=false` (shubhali) sessiya XP oladi; admin `CONFIRM` qilsa qaytariladi (`BL-AD-1`).
- Kunlik chegara: bir kunda (foydalanuvchi tz) sessiyalardan ko'pi bilan **3 000 XP**; oshgan qismi berilmaydi (ledger'ga yozilmaydi), natijada "kunlik limit" belgisi.
- Idempotentlik: `UNIQUE (user_id, source_type, source_id)`; sessiya uchun `source_type=SESSION`, `source_id=<sessionId>`; bonuslar alohida tranzaksiya emas — shu yozuv `amount` iga qo'shilgan, tafsiloti `details` JSONB da (`{base, modeCoef, diffCoef, accFactor, repeatFactor, bonuses:{record,firstOfDay,sprint}, capped}`).

Boshqa XP manbalari:

| `source_type` | Miqdor | `source_id` |
| --- | --- | --- |
| `ACHIEVEMENT` | Achievement `xp_reward` | `<achievementId>:<tier>` |
| `DAILY_CHALLENGE` / `WEEKLY_CHALLENGE` | Challenge `xp_reward` | `<challengeId>` |
| `STREAK` | 50 / 300 / 1500 | `streak:<days>:<startDate>` |
| `COACH_PLAN` | 20 | `<planId>` |
| `GOAL` | 100 | `<goalId>` |
| `COMPETITION` | O'ringa ko'ra (§8) | `<competitionId>` |
| `ADMIN` | ± ixtiyoriy (sabab bilan) | `revoke:<sessionId>` yoki `manual:<auditId>` |

## 2. Level (BL-GM-2)

- `xpToNext(n) = round(100 × n^1.5)`, `n ≥ 1`. Level `L` uchun kumulyativ chegara `T(L) = Σ_{i=1}^{L−1} xpToNext(i)`; `T(1) = 0`.

| Level | Keyingisigacha | Kumulyativ |
| --- | --- | --- |
| 1 | 100 | 0 |
| 2 | 283 | 100 |
| 3 | 520 | 383 |
| 5 | 1 118 | 1 703 |
| 10 | 3 162 | 11 106 |
| 20 | 8 944 | 69 398 |
| 50 | 35 355 | 707 107 (taxminan; aniq qiymat formuladan) |

- Level har XP tranzaksiyasidan keyin `total_xp` dan qayta hisoblanadi (jadval xotirada oldindan hisoblangan, 1…200). Maksimal level 200.
- Level faqat ko'tariladi; XP qaytarilganda `total_xp` kamayadi, level qayta hisoblanadi (pasayishi mumkin — faqat admin `CONFIRM` holatida).
- `LevelUp` hodisasi: natija javobida `levelUp: {from, to}`.

## 3. Til ranki (BL-GM-3)

- Asos: shu tilda `recent_wpm` (`CORE` guruhi, oxirgi 10 sessiya mediani) — `10` §A.4.
- ≥ 10 `CORE` sessiya bo'lmaguncha rank yo'q ("Unranked").
- Chegaralar: `00` §4.7 (OQ-4: til bo'yicha kalibrlash).
- Gisterezis: rank **ko'tarilishi** — chegaradan o'tganda darhol; **pasayishi** — `recent_wpm` chegaradan 2 WPM past tushgandagina (tebranishni oldini oladi).
- Rank o'zgarishi natija ekranida e'lon qilinadi.

## 4. Streak (BL-GM-4)

```text
onSessionCompleted(s):                       # sinxron (BEFORE_COMMIT)
  day = toLocalDate(s.finished_at, user.timezone)
  activeToday = Σ elapsed_ms of COMPLETED sessions on `day` (shu sessiya bilan)
  if activeToday < 180_000: return
  p = user_progress (FOR UPDATE)
  if p.last_active_day == day: return                      # bugun allaqachon hisoblangan
  gap = days_between(p.last_active_day, day)               # null → ∞
  if gap == 1:                 p.current_streak += 1
  elif gap > 1 and gap − 1 ≤ p.freezes_left:               # o'tkazib yuborilgan kunlar freeze bilan yopiladi
       p.freezes_left −= (gap − 1); p.current_streak += 1
       insert streak_freeze_usages(user, har o'tkazilgan kun)
  else:                        p.current_streak = 1; p.streak_start_day = day
  p.last_active_day = day; p.longest_streak = max(…)
  if p.current_streak ∈ {7, 30, 100}: grant STREAK xp; publish StreakMilestone
  publish StreakUpdated
```

- Freeze "lazy" qo'llanadi (keyingi faol kunda), shuning uchun alohida yarim tun job'i shart emas. Dashboard'da ko'rsatish uchun `effectiveStreak(now)`: `gap ≤ 1` → `current_streak`; `gap − 1 ≤ freezes_left` → `current_streak` ("freeze ishlatiladi" belgisi bilan); aks holda 0.
- `freezes_left` har oyning 1-kuni (UTC) job bilan 2 ga tiklanadi (to'planmaydi).
- Timezone o'zgarishi: foydalanuvchi timezone'ni 30 kunda bir marta o'zgartira oladi (suiiste'moldan himoya); o'zgarish keyingi kundan kuchga kiradi.
- "Xavf ostida": mahalliy vaqt ≥ 20:00 va bugun hisoblanmagan va `effectiveStreak > 0`.
- Email eslatma (`emailStreakReminder`): soatlik job, mahalliy 21:00 da, yuqoridagi shart bajarilsa; kuniga 1 marta.

## 5. Shaxsiy rekordlar (BL-GM-5)

- Kalit: `(user, language, mode, duration_limit_s)`; `duration_limit_s = 0` vaqtsiz rejimlar uchun. Rekord yuritiladigan rejimlar: `SPEED_TEST` (30/60/120), `PRACTICE`, `CHALLENGE`, `CODE_SPRINT`.
- Shart: `accuracy ≥ 95`, `PHYSICAL`, `strict_indent=false`, shubha < 50, va (vaqtsiz rejimlarda) `completionPct = 100`, `C ≥ 150`.
- Yangi rekord: `wpm > best_wpm` (teng bo'lsa — yo'q). Birinchi yozuv "rekord" bonusini bermaydi (taqqoslash uchun oldingi qiymat yo'q); ikkinchisidan boshlab +25 XP.
- Natijada: `personalRecord: {previous, current}`.

## 6. Reytinglar (BL-GM-6)

### 6.1 Doskalar

`board_key = <lang>:<board>:<period>`

| `board` | Manba | Qiymat |
| --- | --- | --- |
| `ST30`, `ST60`, `ST120` | `SPEED_TEST`, mos davomiylik | WPM |
| `DAILY` | "Kunning snippeti" challenge'i (§7) | WPM |
| `XP` (`lang` = `all`) | XP ledger | Davr ichidagi XP |
| `DEVSCORE` (`lang` = `all`, faqat `ALL` davr) | Rekordlar | DevScore |

`period`: `D<yyyy-mm-dd>` (UTC kun), `W<yyyy>-<ww>` (ISO hafta), `M<yyyy-mm>`, `ALL`.

Har ranked sessiya 4 doskaga yoziladi (kun, hafta, oy, hammasi).

### 6.2 Kirish sharti (`ranked`)

Barchasi bajarilishi kerak: email tasdiqlangan · foydalanuvchi bloklanmagan · `showOnLeaderboard=true` · `input_device=PHYSICAL` · `strict_indent=false` · `accuracy ≥ 95` · shubha balli < 50 · sessiya snippetlari ranked pool'dan (ST doskalari) · mehmon emas · claim qilingan sessiya emas.

Bajarilmagan birinchi shart `ranked_reason` da saqlanadi va natijada ko'rsatiladi (`EMAIL_NOT_VERIFIED`, `ACCURACY_BELOW_95`, `TOUCH_INPUT`, `STRICT_INDENT`, `UNDER_REVIEW`, `LEADERBOARD_OPT_OUT`, `NOT_RANKED_MODE`).

### 6.3 Yangilash

```sql
INSERT INTO gamification.leaderboard_entries (board_key, user_id, score, accuracy, session_id, achieved_at)
VALUES (…)
ON CONFLICT (board_key, user_id) DO UPDATE
   SET score = EXCLUDED.score, accuracy = EXCLUDED.accuracy,
       session_id = EXCLUDED.session_id, achieved_at = EXCLUDED.achieved_at
 WHERE EXCLUDED.score > leaderboard_entries.score;
```

Asinxron (commit'dan keyin), idempotent. `XP` doskalari: har XP tranzaksiyasida `score += amount` (upsert). `DEVSCORE`: rekord o'zgarganda foydalanuvchining eng yuqori 3 tildagi `ST60` rekord WPM yig'indisi qayta yoziladi.

### 6.4 Tartib va o'rin

- Tartib: `score DESC, accuracy DESC, achieved_at ASC, user_id ASC`.
- O'rin = shu tartibda foydalanuvchidan oldin turganlar soni + 1 (indeks bo'yicha `COUNT`); teng natijalar turli o'rin oladi (yuqoridagi tartib bo'yicha).
- Top-100: 60 s kesh (Caffeine, kalit `board_key + scope + scopeArg`). O'z o'rni keshlanmaydi.
- Ko'rinishlar (`scope`): `global`; `country` (foydalanuvchi mamlakati yoki `?country=`; `users.country_code` bilan JOIN); `following` (R3).
- Sahifalash: cursor (`rank` bo'yicha), `limit` ≤ 100; chuqurlik chegarasi — 10 000-o'rin.
- Foydalanuvchi bloklansa/o'chirilsa/`showOnLeaderboard=false`: yozuvlari so'rovda filtrlangan (`users.status='ACTIVE' AND show_on_leaderboard`), o'chirilmaydi.

### 6.5 DevScore va global ranking

`DevScore = Σ top3(ST60 rekord WPM, til bo'yicha)`; < 3 til bo'lsa — borlari yig'indisi. Bitta tilga ixtisoslashganlar til doskalarida, ko'p tillilar DevScore'da ko'rinadi. "Global ranking" sahifasi standart holda `all:XP:ALL` ni ko'rsatadi; tab: DevScore.

## 7. Challenge'lar (BL-GM-7)

- Shablonlar: `gamification.challenge_templates` (`goal` JSON). Scheduler: har kuni 00:00 UTC — 1 ta `DAILY`; dushanba 00:00 UTC — 1 ta `WEEKLY`; shablon `weight` bo'yicha tasodifiy, oxirgi 7 ta (daily) / 4 ta (weekly) takrorlanmaydi. ShedLock bilan bitta instansda.
- Challenge barcha foydalanuvchilar uchun bir xil; `language_id IS NULL` → har kim o'z asosiy tilida bajaradi.

`goal` turlari:

| `type` | Maydonlar | Progress birligi |
| --- | --- | --- |
| `SESSIONS` | `mode?`, `count`, `minAccuracy?` | Mos sessiyalar soni |
| `CHARS` | `category?` (`08` §2), `count` | To'g'ri terilgan (turkumdagi) belgilar |
| `ACTIVE_MINUTES` | `minutes` | Faol daqiqalar |
| `ACTIVE_DAYS` (weekly) | `days` | Streak hisobiga o'tgan kunlar |
| `LEVEL_COMPLETE` (weekly) | `count` | `TopicLevelCompleted` hodisalari |
| `DAILY_SNIPPET` | `exerciseKeyByLang` (scheduler har nashr qilingan tildan ranked pool'dan bittadan tanlaydi) | 1 (tugatilsa); natija `DAILY` doskasiga |

- Progress `SessionCompleted` (va tegishli hodisalar) ishlovchisida `user_challenges` upsert; `progress ≥ target` → `completed_at`, XP (bir marta).
- `DAILY_SNIPPET`: maxsus sessiya (`mode=SPEED_TEST` emas — `mode=PRACTICE`, `challenge_id` bilan, erkin xato rejimi, pauzasiz); kuniga **3 urinish**, eng yaxshisi doskaga. Hamma bir xil kodni yozadi.
- Tugash vaqti UTC bo'yicha; UI qolgan vaqtni ko'rsatadi.

## 8. Achievement'lar (BL-GM-8)

`criteria` JSON:

```json
{ "metric": "max_wpm", "scope": { "language": "plsql", "mode": "SPEED_TEST", "duration": 60 },
  "op": ">=", "tiers": [50, 70, 90], "minAccuracy": 95 }
```

| `metric` | Manba | Baholash hodisasi |
| --- | --- | --- |
| `max_wpm` | `personal_records` | `SessionCompleted` |
| `total_chars`, `total_sessions`, `total_time_min` | `analytics.user_language_stats` (yoki barcha tillar) | `SessionCompleted` |
| `streak_days` | `user_progress.longest_streak` | `StreakUpdated` |
| `level` | `user_progress.level` | `LevelUp` |
| `topics_mastered`, `levels_completed` | `user_topic_progress` | `TopicLevelCompleted` |
| `languages_at_rank` | Til ranklari (`scope.rank`) | `SessionCompleted` |
| `category_error_rate` | `user_char_stats` turkum bo'yicha (`scope.category`, `minObservations`) — `op` `<=` | `SessionCompleted` |
| `perfect_sessions` | 100% accuracy, `scope.minDifficulty` | `SessionCompleted` |
| `leaderboard_rank` | Yopilgan haftalik doska (`scope.board`) — `op` `<=` | Haftalik yopilish job'i |
| `challenges_completed` | `user_challenges` | Challenge tugashi |

- Bosqichlar (`tiers`): 1–3 ta (Bronze/Silver/Gold); har bosqich alohida ochiladi va XP beradi (`xp_reward` × bosqich raqami).
- Progress: `current / next_tier_value` (`<=` metrikalarda teskari); `user_achievements.progress` hodisa ishlovchisida yangilanadi.
- `hidden=true` achievement: ochilmaguncha nomi/sharti ko'rsatilmaydi.
- Yangi achievement qo'shilganda mavjud foydalanuvchilar uchun retroaktiv baholash: fon job'i (bir marta, achievement `created_at` dan keyin).
- Boshlang'ich to'plam (R2, kamida 40 ta) — `content/gamification/achievements.yaml`; misollar:

| Kod | Kategoriya | Qoida |
| --- | --- | --- |
| `FIRST_STEPS` | Boshlash | `total_sessions ≥ 3` |
| `SPRINTER_<LANG>` | Tezlik | `max_wpm` ST60, tiers 40/60/80, accuracy ≥ 95 (har til uchun) |
| `FLAWLESS` | Aniqlik | `perfect_sessions ≥ 1/10/50`, `minDifficulty=ADVANCED` |
| `MILLION_KEYS` | Hajm | `total_chars ≥ 100k/500k/1M` |
| `CENTURION` | Davomiylik | `streak_days ≥ 7/30/100` |
| `PLSQL_BULK_MASTER` | Mahorat | `topics_mastered`: `bulk-collect`, `forall` |
| `BRACKET_WHISPERER` | Belgilar | `category_error_rate ≤ 2%`, `BRACKETS`, `minObservations=500` |
| `POLYGLOT` | Polyglot | `languages_at_rank ≥ 2/3/5`, `rank=GOLD` |
| `PODIUM` | Ijtimoiy | `leaderboard_rank ≤ 3`, haftalik ST60 |

## 9. Musobaqalar

### 9.1 Asinxron musobaqa (R2) — BL-GM-9

- Admin yaratadi: sarlavha (4 lokal), til, davomiylik (ST 30/60/120), boshlanish/tugash (UTC), `max_attempts` (standart 5), snippet to'plami (ranked pool'dan 10–20 ta, muzlatilgan ro'yxat).
- Holatlar: `SCHEDULED` → `ACTIVE` → `FINISHED` (scheduler daqiqalik tekshiruv bilan o'tkazadi) → `ARCHIVED`.
- Qatnashish: `join` (faqat email tasdiqlangan); urinish — `POST /sessions` `competitionId` bilan (`mode=SPEED_TEST`, snippetlar musobaqa to'plamidan, tartibi foydalanuvchi+urinish bo'yicha deterministik aralashtirilgan).
- Hisob: eng yaxshi ranked urinish WPM'i; urinish `attempts` ni `POST /sessions` da oshiradi (tashlab ketilgan ham sarflanadi).
- Tugagach: yakuniy jadval muzlatiladi; mukofot XP: 1-o'rin 500, 2-o'rin 300, 3-o'rin 200, top-10% 100, qatnashganlar (≥ 1 ranked urinish) 25.
- Shubhali (flagged) urinishlar jadvalda "tekshirilmoqda" bilan; admin qaroridan keyin jadval qayta hisoblanadi (mukofotlar `FINISHED` dan 24 soat keyin beriladi).

### 9.2 Jonli poyga (R3)

2–8 kishi, bitta snippet, WebSocket (STOMP) orqali har 500 ms da progress (`cursor/total`, jonli WPM) tarqatiladi. Xona holati: `LOBBY` → `COUNTDOWN (5 s)` → `RUNNING` → `FINISHED`. Yakuniy natija baribir REST `complete` + keylog validatsiyasidan o'tadi; WebSocket ma'lumoti faqat ko'rsatish uchun. Batafsil spetsifikatsiya R3 boshlanishidan oldin yoziladi (**OQ-8**).

## 10. Hodisalar oqimi (qisqa)

| Hodisa | Sinxron (javobga kiradi) | Asinxron |
| --- | --- | --- |
| `SessionCompleted` | XP, level, streak, rekord, rank o'zgarishi, coach tip | Reyting, challenge progress, achievement, coach plan, analytics |
| `TopicLevelCompleted` | — | Achievement, weekly challenge |
| `StreakUpdated` / `LevelUp` | — | Achievement |

Asinxron natijalar (achievement, challenge tugashi) keyingi `GET /me/notifications/unseen` da qaytadi va toast sifatida ko'rsatiladi; natija ekrani 1.5 s dan keyin bir marta shu endpoint'ni so'raydi.
