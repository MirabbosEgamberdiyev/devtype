# 14. REST API Specification

Baza: `/api/v1`. Format: JSON (`application/json; charset=utf-8`). Bu hujjat kontraktning inson o'qiydigan manbasi; mashina o'qiydigan `openapi.yaml` backend'dan generatsiya qilinadi va shu hujjatga mos bo'lishi shart.

## 1. Umumiy qoidalar

### 1.1 Autentifikatsiya

| Tur | Qanday | Qayerda |
| --- | --- | --- |
| Access token | `Authorization: Bearer <JWT>` | Barcha `User`/`Editor`/`Admin` endpoint'lar |
| Refresh token | Cookie `dt_refresh` (`HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`) | Faqat `/auth/refresh`, `/auth/logout` |
| Mehmon tokeni | `X-Guest-Token: <signed>` | `Guest` ruxsat etilgan endpoint'lar |

Jadvaldagi "Rol": `—` ommaviy · `Guest` mehmon tokeni yoki user · `User` · `Editor` (EDITOR yoki ADMIN + 2FA) · `Admin` (ADMIN + 2FA).

### 1.2 Sarlavhalar

- So'rov: `Accept-Language` (mehmon uchun lokal), `X-Request-Id` (ixtiyoriy; bo'lmasa server yaratadi), `X-Engine-Version` (sessiya endpoint'larida majburiy).
- Javob: `X-Request-Id`, `X-Trace-Id`; rate limit — `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `Retry-After` (429/503).

### 1.3 Xatolar (RFC 9457)

```json
{
  "type": "https://devtype.dev/problems/level-locked",
  "title": "Daraja qulflangan",
  "status": 409,
  "detail": "Bu darajani ochish uchun yana 2 ta mashqni tugating.",
  "code": "LEVEL_LOCKED",
  "traceId": "8f3c2a1b9d0e4f67",
  "errors": [ { "field": "email", "code": "INVALID_FORMAT", "message": "…" } ],
  "meta": { "completed": 2, "required": 4 }
}
```

- `code` — barqaror, klient mantiqi shunga tayanadi. `title`/`detail` — lokalga tarjima qilingan.
- `errors[]` faqat `400 VALIDATION_FAILED` da; `meta` — kodga xos qo'shimcha ma'lumot.

### 1.4 Sahifalash, filtr, saralash

| Tur | Parametrlar | Javob | Qayerda |
| --- | --- | --- | --- |
| Cursor | `?cursor=<opaque>&limit=20` (maks. 100) | `{ "items": [...], "nextCursor": "…" \| null }` | Tarix, reyting, bildirishnomalar |
| Sahifa | `?page=0&size=20` (maks. 100) `&sort=field,asc\|desc` | `{ "items": [...], "page": 0, "size": 20, "totalItems": 134, "totalPages": 7 }` | Admin ro'yxatlari |

- Saralash maydonlari har endpoint uchun oq ro'yxatda; noma'lum maydon → `400 INVALID_SORT`.
- Filtrlar — aniq nomlangan query parametrlar; noma'lum parametr e'tiborsiz qoldiriladi.
- Cursor noto'g'ri/eskirgan → `400 INVALID_CURSOR`.

### 1.5 Boshqa qoidalar

- **Idempotentlik:** `POST /sessions/{id}/complete`, `/abandon`, `/sessions/claim`, `PUT` lar. Qolgan `POST` lar uchun ixtiyoriy `Idempotency-Key` (24 soat).
- **Versiyalash:** URL'da. Buzuvchi o'zgarish → `/v2`; `/v1` kamida 6 oy qo'llanadi. Maydon qo'shish buzuvchi emas — klient noma'lum maydonlarni e'tiborsiz qoldiradi.
- **Vaqt:** ISO-8601 UTC. **Sana:** `YYYY-MM-DD`. **Enum:** `UPPER_SNAKE`.
- **Lokalizatsiya:** tarjima qilinadigan maydonlar (mavzu nomi, izoh, achievement) so'rov lokalida **bitta qiymat** sifatida qaytadi; admin endpoint'lari `*I18n` obyektlarini to'liq qaytaradi.
- **So'rov hajmi:** 256 KB (import va avatar: `multipart`, 5 MB / 2 MB).
- **Kesh:** `GET /languages`, `/languages/{slug}/topics` (anonim) — `Cache-Control: public, max-age=300` + `ETag`; shaxsiy javoblar — `private, no-store`.

### 1.6 Umumiy xato kodlari

| HTTP | `code` | Ma'no |
| --- | --- | --- |
| 400 | `VALIDATION_FAILED`, `INVALID_CURSOR`, `INVALID_SORT`, `MALFORMED_JSON` | So'rov noto'g'ri |
| 401 | `UNAUTHENTICATED`, `TOKEN_EXPIRED`, `TOKEN_INVALID`, `TOKEN_REUSE_DETECTED` | Autentifikatsiya |
| 403 | `FORBIDDEN`, `TWO_FACTOR_REQUIRED`, `EMAIL_NOT_VERIFIED`, `ACCOUNT_BLOCKED`, `GUEST_MODE_NOT_ALLOWED` | Ruxsat yo'q |
| 404 | `NOT_FOUND` (resurs turi bilan `meta.resource`) | Yo'q yoki boshqaga tegishli |
| 409 | `CONFLICT`, `CONCURRENT_MODIFICATION` va modul kodlari | Holat ziddiyati |
| 410 | `TOKEN_EXPIRED`, `TOKEN_USED` | Bir martalik token |
| 413 | `PAYLOAD_TOO_LARGE` | |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | |
| 422 | Domen tekshiruvi kodlari (sessiya) | |
| 426 | `ENGINE_VERSION_UNSUPPORTED` | Klient eskirgan |
| 429 | `RATE_LIMITED` | |
| 500 | `INTERNAL_ERROR` | |
| 503 | `SERVICE_UNAVAILABLE` | |

Boshqa foydalanuvchiga tegishli resurs har doim `404` (mavjudligi oshkor qilinmaydi), `403` emas.

---

## 2. Auth

| Metod va yo'l | Vazifa | Rol | Xato kodlari |
| --- | --- | --- | --- |
| `POST /auth/register` | Ro'yxatdan o'tish | — | `EMAIL_TAKEN`, `USERNAME_TAKEN`, `PASSWORD_BREACHED`, `VALIDATION_FAILED` |
| `POST /auth/login` | Login | — | `INVALID_CREDENTIALS`, `ACCOUNT_BLOCKED`, `TWO_FACTOR_REQUIRED` (2FA yoqilgan: `meta.challengeToken`) |
| `POST /auth/login/2fa` | TOTP kodi bilan yakunlash | — | `INVALID_TOTP`, `TOKEN_EXPIRED` |
| `POST /auth/refresh` | Token rotation | cookie | `TOKEN_INVALID`, `TOKEN_REUSE_DETECTED` |
| `POST /auth/logout` | Joriy oilani bekor qilish | cookie | — (har doim 204) |
| `POST /auth/logout-all` | Barcha kirishlarni bekor qilish | User | — |
| `POST /auth/verify-email` | `{token}` | — | `TOKEN_EXPIRED`, `TOKEN_USED`, `TOKEN_INVALID` |
| `POST /auth/resend-verification` | Qayta yuborish | User | `ALREADY_VERIFIED`, `RATE_LIMITED` |
| `POST /auth/forgot-password` | `{email}` → har doim 202 | — | `RATE_LIMITED` |
| `POST /auth/reset-password` | `{token, newPassword}` | — | `TOKEN_*`, `PASSWORD_BREACHED` |
| `POST /auth/change-password` | `{currentPassword, newPassword}` | User | `INVALID_CREDENTIALS` |
| `GET /auth/oauth2/{provider}` | Provayderga yo'naltirish (`302`) | — | `PROVIDER_NOT_SUPPORTED` |
| `GET /auth/oauth2/{provider}/callback` | Kod almashinuvi → refresh cookie → `302 /oauth/callback` | — | `OAUTH_FAILED`, `OAUTH_EMAIL_UNVERIFIED` |
| `DELETE /auth/oauth2/{provider}` | Bog'lanishni uzish | User | `LAST_LOGIN_METHOD` (parol ham yo'q bo'lsa) |
| `POST /auth/guest` | Mehmon tokeni | — | `RATE_LIMITED` |
| `POST /auth/2fa/setup` | TOTP siri + `otpauth://` URI | User | `ALREADY_ENABLED` |
| `POST /auth/2fa/enable` | `{code}` → yoqish + 10 ta zaxira kod | User | `INVALID_TOTP` |
| `POST /auth/2fa/disable` | `{password, code}` | User | `REQUIRED_FOR_ROLE` (EDITOR/ADMIN) |

```json
// POST /auth/register
{ "email": "ali@example.com", "username": "ali_dev", "password": "…",
  "locale": "uzl", "timezone": "Asia/Samarkand", "guestToken": "…" }
// 201
{ "accessToken": "eyJ…", "expiresIn": 900,
  "user": { "id": 1042, "username": "ali_dev", "email": "ali@example.com", "emailVerified": false,
            "role": "USER", "locale": "uzl", "onboarded": false, "onboardingStep": 1 },
  "claimedSessions": 1 }
// + Set-Cookie: dt_refresh=…
```

`POST /auth/login`: `{ "login": "<email yoki username>", "password": "…" }` → yuqoridagi shakl (`claimedSessions` siz).

## 3. Profil

| Metod va yo'l | Vazifa | Rol |
| --- | --- | --- |
| `GET /me` | Joriy foydalanuvchi | User |
| `PATCH /me` | `displayName`, `bio`, `countryCode`, `timezone`, `locale`, `isPublic`, `showOnLeaderboard`, `username` (30 kunda 1 marta) | User |
| `DELETE /me` | O'chirishni rejalashtirish (`{password}` yoki OAuth qayta tasdiq) | User |
| `POST /me/avatar` (`multipart`) · `DELETE /me/avatar` | Avatar | User |
| `GET /me/settings` · `PUT /me/settings` | Sozlamalar obyekti (`05` §5.17); `PUT` qisman — berilgan kalitlar birlashtiriladi; noma'lum kalit → 400 | User |
| `POST /me/onboarding` | `{step, …}` qadamni saqlash; oxirgisida `onboarded=true` | User |
| `GET /me/dashboard` | Dashboard yig'ma javobi (§3.1) | User |
| `GET /me/auth-sessions` · `DELETE /me/auth-sessions/{id}` | Faol kirishlar | User |
| `GET /me/export` | JSON eksport (kuniga 1 marta; `202` + email havola, katta bo'lsa) | User |
| `GET /me/notifications/unseen` · `POST /me/notifications/seen` | Asinxron mukofot xabarlari | User |
| `GET /users/{username}` | Ommaviy profil (yopiq → 404) | — |
| `PUT /users/{username}/follow` · `DELETE …/follow` | Kuzatish (R3) | User |
| `GET /me/following` | Kuzatilayotganlar (R3) | User |
| `GET /users/check-username?value=` | `{available: bool}` | — |

Xato kodlari: `USERNAME_TAKEN`, `USERNAME_CHANGE_TOO_SOON`, `TIMEZONE_CHANGE_TOO_SOON`, `INVALID_IMAGE`, `IMAGE_TOO_LARGE`.

### 3.1 `GET /me/dashboard`

```json
{
  "resume": { "language": "plsql", "topic": "cursor", "topicName": "CURSOR", "difficulty": "ADVANCED",
              "exerciseKey": "…", "exerciseTitle": "Parameterized cursor", "completed": 3, "total": 6 },
  "gamification": { "totalXp": 12480, "level": 10, "xpIntoLevel": 1374, "xpToNext": 3162,
                    "streak": { "current": 12, "longest": 19, "doneToday": false, "atRisk": true, "freezesLeft": 1 } },
  "languages": [ { "slug": "plsql", "name": "PL/SQL", "primary": true, "currentWpm": 49.6, "rank": "SILVER",
                   "progressPct": 31, "sparkline": [44.1, 45.0, null, 47.2] } ],
  "recentSessions": [ { "id": "…", "language": "plsql", "mode": "PRACTICE", "wpm": 49.54, "accuracy": 96.38,
                        "finishedAt": "2026-11-14T14:45:00Z", "xp": 102 } ],
  "dailyChallenge": { "id": 311, "title": "…", "progress": 1, "target": 3, "endsAt": "…", "completed": false },
  "coachPlan": { "planId": 9001, "items": [ { "seq": 1, "mode": "WEAKNESS", "reason": "…", "completed": false } ] },
  "isNewUser": false
}
```

`dailyChallenge`, `coachPlan` — reliz/feature flag yoqilmagan bo'lsa `null`. `sparkline` — 14 kun, sessiyasiz kun `null`.

## 4. Katalog va o'quv yo'nalishi

| Metod va yo'l | Vazifa | Rol |
| --- | --- | --- |
| `GET /languages` | Nashr qilingan tillar + konfiguratsiya (`indent`, `tokenPatterns`, `grammarId`) + "tez orada" (`status=DRAFT`, faqat nom) | — |
| `GET /languages/{slug}` | Bitta til + mavzular soni | — |
| `GET /languages/{slug}/topics` | Mavzular (+ user bo'lsa daraja holatlari) | — / User |
| `GET /languages/{slug}/topics/{topic}` | Mavzu tafsiloti + 4 daraja holati va ochilish sharti | User |
| `GET /languages/{slug}/topics/{topic}/exercises?difficulty=` | Mashqlar ro'yxati (**kodsiz**) + foydalanuvchining eng yaxshi natijasi | User |
| `GET /me/languages` · `PUT /me/languages` | Tanlangan tillar (`BL-PL-1`) | User |
| `POST /exercises/{exerciseKey}/report` | Shikoyat `{reason, comment}` | User |

Xato kodlari: `LANGUAGE_NOT_AVAILABLE`, `PRIMARY_REQUIRED`, `AT_LEAST_ONE_LANGUAGE`, `ALREADY_REPORTED`.

```json
// GET /languages/plsql/topics   (User)
{ "language": { "slug": "plsql", "name": "PL/SQL" },
  "topics": [ { "slug": "cursor", "name": "CURSOR", "order": 8,
      "levels": [ { "difficulty": "BEGINNER", "state": "MASTERED", "completed": 6, "total": 6 },
                  { "difficulty": "INTERMEDIATE", "state": "COMPLETED", "completed": 5, "total": 6 },
                  { "difficulty": "ADVANCED", "state": "IN_PROGRESS", "completed": 3, "total": 6, "required": 5 },
                  { "difficulty": "EXPERT", "state": "LOCKED", "completed": 0, "total": 5 } ] } ] }
```

## 5. Sessiyalar

| Metod va yo'l | Vazifa | Rol |
| --- | --- | --- |
| `POST /sessions` | Sessiya ochish | Guest |
| `POST /sessions/{id}/next` | Keyingi snippet(lar) — SPEED_TEST oqimi | Guest (egasi) |
| `POST /sessions/{id}/complete` | Keylog → rasmiy natija | Guest (egasi) |
| `POST /sessions/{id}/abandon` | Tashlab ketish (204) | Guest (egasi) |
| `GET /sessions/{id}` | Sessiya + natija | Guest (egasi) |
| `GET /sessions/{id}/replay` | Keylog + snippetlar | User (egasi) |
| `GET /sessions?lang=&mode=&status=&from=&to=&cursor=&limit=` | Tarix | User |
| `POST /sessions/claim` | `{guestToken}` → `{claimed: n}` | User |

### 5.1 `POST /sessions`

```json
// so'rov (rejimga qarab maydonlar)
{ "language": "plsql", "mode": "PRACTICE",
  "topic": "bulk-collect", "difficulty": "ADVANCED",     // PRACTICE, ACCURACY, CODE_SPRINT(topic)
  "exerciseKey": null,                                    // PRACTICE: aniq mashq (ixtiyoriy)
  "duration": null,                                       // SPEED_TEST: 30|60|120
  "accuracyMaster": false,                                // ACCURACY
  "specialCategory": null,                                // SPECIAL_CHARS: BRACKETS|…|MIXED|null (= tavsiya)
  "challengeId": null, "competitionId": null, "placement": false,
  "inputDevice": "PHYSICAL", "strictIndent": false }

// 201
{ "sessionId": "0193a1f2-7c1e-7b3a-9d10-2f6c1e0a4b77",
  "startedAt": "2026-11-14T14:42:10.120Z", "expiresAt": "2026-11-15T14:42:10.120Z",
  "mode": "PRACTICE", "errorMode": "STOP", "durationLimitS": null,
  "language": { "slug": "plsql", "grammarId": "plsql",
                "indent": { "style": "SPACE", "size": 2 },
                "tokenPatterns": [":=", "=>", "||", "..", "%TYPE"] },
  "exercises": [ { "seq": 0, "exerciseKey": "…", "title": "BULK COLLECT with LIMIT",
                   "note": "LIMIT xotira sarfini chegaralaydi.", "difficulty": "ADVANCED",
                   "code": "DECLARE\n  TYPE t_order_ids IS TABLE OF orders.order_id%TYPE;\n…",
                   "charCount": 612 } ],
  "completeToken": "…" }
```

Maydon qoidalari: `mode` ga tegishli bo'lmagan maydonlar `null` bo'lishi kerak (aks holda `400`). `errorMode` — server belgilaydi (`practiceStopOnError` sozlamasini hisobga olib).

Xato kodlari: `LANGUAGE_NOT_AVAILABLE` (409), `MODE_NOT_AVAILABLE` (409), `GUEST_MODE_NOT_ALLOWED` (403), `LEVEL_LOCKED` (409, `meta.completed/required`), `NO_EXERCISES_AVAILABLE` (409), `WEAKNESS_PROFILE_EMPTY` (409, `meta.sessionsNeeded`), `CHALLENGE_ATTEMPTS_EXHAUSTED` (409), `COMPETITION_NOT_ACTIVE` (409), `COMPETITION_ATTEMPTS_EXHAUSTED` (409), `NOT_JOINED` (409), `ENGINE_VERSION_UNSUPPORTED` (426), `RATE_LIMITED` (429).

### 5.2 `POST /sessions/{id}/next`

Faqat `SPEED_TEST`. So'rov: `{ "afterSeq": 2 }` → `{ "exercises": [ {seq: 3…}, {seq: 4…} ] }`. Klient oxirgi snippetga o'tganda oldindan chaqiradi. Sessiyada ko'pi bilan 40 snippet → `409 SESSION_EXERCISE_LIMIT`. Idempotent (`afterSeq` bo'yicha bir xil javob).

### 5.3 `POST /sessions/{id}/complete`

```json
// so'rov
{ "completeToken": "…", "keylog": "H4sIAAAAA…", "clientElapsedMs": 148230,
  "clientMetrics": { "wpm": 49.54, "accuracy": 96.38 } }

// 200
{ "sessionId": "…", "status": "COMPLETED",
  "ranked": false, "rankedReason": "NOT_RANKED_MODE",
  "metrics": { "wpm": 49.54, "rawWpm": 51.41, "cpm": 247.72, "accuracy": 96.38, "consistency": 81.20,
               "errors": 23, "uncorrectedErrors": 0, "backspaces": 0, "correctChars": 612,
               "elapsedMs": 148230, "completionPct": 100.00,
               "specialAccuracy": 93.10 },
  "wpmSeries": [ { "t": 5, "wpm": 41.0, "raw": 46.0, "errors": 1 } ],
  "weakTokens": [ { "token": "%", "errors": 5, "expected": 9, "avgMs": 620 },
                  { "token": ":=", "errors": 1, "expected": 6, "avgMs": 410 } ],
  "slowLines": [ { "line": 12, "ms": 9800 } ],
  "rewards": { "xp": 102, "xpBreakdown": { "base": 61.2, "modeCoef": 1.0, "diffCoef": 1.5, "accFactor": 1.0,
                                           "repeatFactor": 1.0, "bonuses": { "firstOfDay": 10 }, "capped": false },
               "totalXp": 12582, "level": 10, "levelUp": null,
               "streak": { "current": 13, "extendedToday": true },
               "personalRecord": null, "rankChange": null },
  "progress": { "topic": "bulk-collect", "difficulty": "ADVANCED", "state": "IN_PROGRESS",
                "completed": 4, "required": 5, "exerciseCompleted": true, "levelUnlocked": null },
  "coachTip": { "code": "CHAR_TROUBLE", "text": "`%` belgisida 5 ta xato — …" },
  "next": { "exerciseKey": "…", "title": "…" } }
```

- Mehmon uchun `rewards`, `progress`, `coachTip` = `null`.
- `wpmSeries` — 5 soniyalik qadam (≤ 60 nuqta).
- Rad etish: `422` + `code` ∈ `KEYLOG_INVALID`, `TIMING_MISMATCH`, `SESSION_EXPIRED`, `SESSION_TOO_SHORT`; `409 SESSION_NOT_ACTIVE`; `401 COMPLETE_TOKEN_INVALID`.
- `REJECTED` sessiyaga takroriy chaqiruv — o'sha `422` javob.

### 5.4 Tarix elementi

`{ id, language, mode, topic, difficulty, durationLimitS, status, wpm, accuracy, elapsedMs, xp, ranked, finishedAt, replayAvailable }`. Filtr: `status` standart `COMPLETED`; `from`/`to` — sana (foydalanuvchi tz).

## 6. Analytics

| Metod va yo'l | Vazifa | Rol |
| --- | --- | --- |
| `GET /analytics/overview?group=CORE` | Umumiy KPI + til bo'yicha qisqa jadval + eng zaif til | User |
| `GET /analytics/languages/{slug}?period=7d\|30d\|90d\|all&bucket=day\|week\|month&group=` | KPI + vaqt qatori | User |
| `GET /analytics/languages/{slug}/chars` | Belgi va bigram statistikasi, zaiflik ballari, chalkashliklar | User |
| `GET /analytics/languages/{slug}/categories` | Turkumlar bo'yicha accuracy va ms | User |
| `GET /analytics/activity?year=2026` | Faollik kalendari | User |
| `GET /analytics/recommendations` | Tavsiyalar (Coach'siz ham ishlaydi) | User |

```json
// GET /analytics/languages/plsql?period=30d&bucket=day
{ "kpi": { "currentWpm": 49.6, "avgWpm": 46.2, "maxWpm": 58.1, "avgCpm": 231.0, "accuracy": 95.8,
           "specialAccuracy": 92.4, "totalTimeMs": 5820000, "totalChars": 22410, "sessions": 61, "errors": 980 },
  "series": [ { "date": "2026-11-13", "avgWpm": 48.2, "maxWpm": 52.0, "accuracy": 96.1, "sessions": 4, "timeMs": 420000 } ],
  "enoughData": true, "sessionsNeeded": 0 }
```

`enoughData=false` bo'lsa `series` bo'sh va `sessionsNeeded` > 0.

## 7. Coach (R2+)

| Metod va yo'l | Vazifa | Rol |
| --- | --- | --- |
| `GET /coach/plan?lang=` | Bugungi reja (yo'q bo'lsa yaratadi) | User |
| `GET /coach/goals` · `POST /coach/goals` · `DELETE /coach/goals/{id}` | Maqsadlar | User |
| `GET /coach/reports?cursor=` · `GET /coach/reports/{weekKey}` | Haftalik hisobotlar | User |
| `POST /coach/explain` | LLM/shablon tushuntirish `{context: "PLAN"\|"REPORT", id}` (R3) | User |

Xato kodlari: `COACH_DISABLED` (403), `GOAL_EXISTS` (409), `GOAL_TARGET_INVALID` (400), `COACH_LIMIT_REACHED` (429).

## 8. Gamification

| Metod va yo'l | Vazifa | Rol |
| --- | --- | --- |
| `GET /gamification/me` | XP, level, streak, til ranklari | User |
| `GET /gamification/xp-history?cursor=` | Ledger | User |
| `GET /achievements` | Barchasi + foydalanuvchi progressi | User |
| `GET /challenges/current` | Kunlik va haftalik | User |
| `GET /records` | Shaxsiy rekordlar | User |
| `GET /leaderboards?lang=&board=ST60&period=week\|day\|month\|all&scope=global\|country\|following&country=&cursor=&limit=` | Reyting + `me` | — |
| `GET /competitions?status=` · `GET /competitions/{id}` · `GET /competitions/{id}/standings?cursor=` | Musobaqalar | — |
| `POST /competitions/{id}/join` | Qatnashish | User |

```json
// GET /leaderboards?lang=java&board=ST60&period=week
{ "boardKey": "java:ST60:W2026-46", "endsAt": "2026-11-16T00:00:00Z",
  "items": [ { "rank": 1, "username": "typefast", "countryCode": "UZ", "avatarUrl": "…",
               "score": 112.40, "accuracy": 98.10, "achievedAt": "…" } ],
  "nextCursor": "…",
  "me": { "rank": 482, "score": 61.20, "accuracy": 96.00, "eligible": true, "reason": null } }
```

`me` — anonim so'rovda `null`; foydalanuvchi doskada yo'q bo'lsa `rank: null` va `reason` (masalan `EMAIL_NOT_VERIFIED`, `NO_RANKED_SESSION`).

Xato kodlari: `BOARD_NOT_FOUND`, `EMAIL_NOT_VERIFIED`, `COMPETITION_NOT_OPEN`, `ALREADY_JOINED`.

## 9. Admin

Barcha yo'llar `/admin/…`; 2FA majburiy; har yozuvchi amal `audit_log` ga.

| Metod va yo'l | Vazifa | Rol |
| --- | --- | --- |
| `GET /admin/languages` · `POST` · `PUT /{id}` · `POST /{id}/publish` · `POST /{id}/archive` | Tillar | Admin |
| `GET /admin/topics?languageId=` · `POST` · `PUT /{id}` · `DELETE /{id}` (faqat mashqsiz) · `PUT /admin/topics/reorder` | Mavzular | Editor |
| `GET /admin/exercises?languageId=&topicId=&difficulty=&status=&tag=&q=&page=` | Mashqlar ro'yxati | Editor |
| `GET /admin/exercises/{id}` · `POST /admin/exercises` · `PUT /{id}` | CRUD (`BL-EM-4`) | Editor |
| `POST /admin/exercises/{id}/submit` · `/publish` · `/reject` · `/archive` | Hayot sikli | Editor (`publish`: muallif emas) |
| `POST /admin/exercises/validate` | `{languageId, difficulty, type, code}` → `{valid, errors[], warnings[], computed{…}}` | Editor |
| `POST /admin/exercises/import` (`multipart`, `?dryRun=true`) | Ommaviy import → hisobot | Editor |
| `GET /admin/content/coverage?languageId=` | Qamrov matritsasi | Editor |
| `GET /admin/reports?status=` · `POST /admin/reports/{id}/resolve` | Mashq shikoyatlari | Editor |
| `GET /admin/users?q=&role=&status=&page=` · `GET /admin/users/{id}` | Foydalanuvchilar | Admin |
| `PATCH /admin/users/{id}` | `{role?, status?, reason}` (`BL-AD-2`) | Admin |
| `GET /admin/sessions/flagged?page=` · `GET /admin/sessions/{id}` · `POST /admin/sessions/{id}/review` | `{decision: CLEAR\|CONFIRM, reason}` | Admin |
| `GET/POST/PUT /admin/achievements[/{id}]` | Achievement'lar | Admin |
| `GET/POST/PUT /admin/challenge-templates[/{id}]` | Shablonlar | Admin |
| `GET/POST/PUT /admin/competitions[/{id}]` · `POST /{id}/cancel` | Musobaqalar | Admin |
| `GET /admin/metrics?from=&to=` | Mahsulot metrikalari | Admin |
| `GET /admin/audit-log?actorId=&entityType=&entityId=&from=&to=&page=` | Audit | Admin |
| `POST /admin/analytics/rebuild?userId=` | Agregatlarni qayta qurish (202) | Admin |

Xato kodlari: `LANGUAGE_NOT_READY` (`meta.missing[]`), `EXERCISE_VALIDATION_FAILED` (`errors[]` — `BL-EM-1` kodlari), `DUPLICATE_EXERCISE`, `SELF_REVIEW_NOT_ALLOWED`, `INVALID_STATUS_TRANSITION`, `TOPIC_HAS_EXERCISES`, `CANNOT_CHANGE_OWN_ROLE`, `LAST_ADMIN`, `TRANSLATIONS_INCOMPLETE` (`meta.missingLocales[]`), `IMPORT_PARSE_ERROR`.

Import hisoboti: `{ "total": 120, "accepted": 112, "skippedDuplicates": 5, "rejected": [ { "file": "plsql/cursor/advanced-3.yaml", "errors": [ { "rule": "LINE_TOO_LONG", "line": 14, "col": 81 } ] } ] }`.

## 10. Texnik endpoint'lar

| Yo'l | Vazifa | Kirish |
| --- | --- | --- |
| `GET /actuator/health/liveness`, `/readiness` | Sog'liq | Ichki tarmoq / Nginx'dan yopiq |
| `GET /actuator/prometheus` | Metrikalar | Faqat ichki tarmoq |
| `GET /v3/api-docs`, `/swagger-ui` | OpenAPI | `local`, `staging` da; `prod` da o'chiq |
| `GET /api/v1/meta` | `{ engineVersions: {min, current}, locales, features: {coach, leaderboards, …} }` | — |

`/meta` klientga qaysi rejim/funksiya yoqilganini aytadi (feature flag'lar) — UI shunga qarab ko'rsatadi.
