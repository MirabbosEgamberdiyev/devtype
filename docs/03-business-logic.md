# 03. Business Logic va algoritmlar

Har qoida `BL-<MOD>-<n>` ID'ga ega. Engine (`07`), maxsus belgilar va klaviatura (`08`), analytics va Coach (`10`), gamification (`11`) algoritmlari o'z hujjatlarida; bu yerda ularning indeksi va qolgan modullar to'liq berilgan.

## 0. Indeks

| ID | Mavzu | Qayerda |
| --- | --- | --- |
| BL-UM-1…5 | Ro'yxat, token rotation, mehmon claim, o'chirish, username | §1 |
| BL-PL-1…2 | Til tanlash, nashr tayyorligi | §2 |
| BL-LS-1…4 | Daraja holatlari, ochilish, placement, progress | §3 |
| BL-TM-1…7 | Snippet tanlash, rejimlar, sessiya hayot sikli | §4 |
| BL-EM-1…4 | Validatsiya, hisoblanadigan maydonlar, taqiqlar, versiyalash | §5 |
| BL-UD-1 | "Davom ettirish" | §6 |
| BL-AD-1…2 | Shubhali sessiya qarori, rol qoidalari | §7 |
| BL-TE-* | Engine qoidalari, formulalar, replay, validatsiya | `07` |
| BL-SC-*, BL-VK-* | Belgi mashqi generatsiyasi, klaviatura | `08` |
| BL-AN-*, BL-AI-* | Agregatlar, zaiflik, Coach | `10` |
| BL-GM-* | XP, level, streak, achievement, reyting | `11` |

---

## 1. User Management

### BL-UM-1 Ro'yxatdan o'tish

```text
register(email, username, password, locale, guestToken?):
  normalize: email = lower(trim(email)); username = trim(username)
  validate format (FR-UM-1)                         → 400 VALIDATION_FAILED
  if username ∈ RESERVED or matches /^(uzl|uzc|ru|en)$/ → 409 USERNAME_TAKEN
  if exists(email)                                   → 409 EMAIL_TAKEN
  if exists(username, case-insensitive)              → 409 USERNAME_TAKEN
  if password ∈ breached list (HIBP k-anonymity; API ishlamasa — o'tkazib yuboriladi, log)
                                                     → 400 PASSWORD_BREACHED
  TX:
    user = insert(users: status=ACTIVE, email_verified=false, role=USER,
                  timezone = so'rovdagi IANA tz yoki 'UTC', locale)
    insert(user_settings: defaults), insert(gamification.user_progress)
    token = createOneTimeToken(user, VERIFY_EMAIL, 24h)
    publish UserRegistered(userId)
  after commit: sendMail(verify-email, locale)
  if guestToken valid: claimGuestSessions(user, guestId)   (BL-UM-3)
  return tokens (access + refresh cookie)
```

### BL-UM-2 Refresh token rotation

- Har `refresh` da: eski token `revoked_at=now`, yangi token **shu `family_id`** bilan beriladi.
- Bekor qilingan token qayta ishlatilsa (`revoked_at IS NOT NULL`): butun oila bekor qilinadi → `401 TOKEN_REUSE_DETECTED`; xavfsizlik xati yuboriladi.
- Bir vaqtda ikki tab muammosi: bekor qilinganidan ≤ 10 s o'tgan va yangi token hali ishlatilmagan bo'lsa, o'sha yangi token qayta beriladi (reuse hisoblanmaydi).
- Parol o'zgarishi/tiklash, bloklash, "hammasidan chiqish" — foydalanuvchining barcha oilalarini bekor qiladi.

### BL-UM-3 Mehmon sessiyalarini ko'chirish (claim)

```text
claim(user, guestToken):
  guestId = verify(guestToken)                        → 400 GUEST_TOKEN_INVALID
  sessions = SELECT … WHERE guest_id=:guestId AND user_id IS NULL
             AND status='COMPLETED' ORDER BY started_at DESC LIMIT 50
  for s in sessions (eski→yangi):
     s.user_id = user.id; s.guest_id qoladi (iz uchun); s.ranked = false
     republish SessionCompleted(s, claimed=true)
       → analytics agregatlari yangilanadi
       → gamification: XP beriladi (repeatFactor qo'llanadi), rekord YO'Q, reyting YO'Q
       → streak: faqat bugungi (foydalanuvchi tz) sessiyalar hisobga olinadi
  qolgan (50 dan ortiq yoki COMPLETED bo'lmagan) mehmon sessiyalari o'chiriladi
  idempotent: ikkinchi chaqiruv 0 ta sessiya ko'chiradi
```

### BL-UM-4 Akkauntni o'chirish

1. `DELETE /me` (parol yoki OAuth qayta tasdiq bilan) → `status=DELETED`, `deletion_scheduled_at = now + 30 kun`; barcha tokenlar bekor; reyting yozuvlari yashirin.
2. 30 kun ichida login → o'chirish bekor (`status=ACTIVE`), foydalanuvchiga xabar.
3. Muddat o'tgach (kunlik job): `email = deleted-<id>@invalid`, `username = deleted_<id>`, parol/OAuth/avatar/bio/mamlakat tozalanadi; `training.sessions`, `analytics.*`, `gamification.*` (foydalanuvchiga tegishli), `coach.*` qatorlari o'chiriladi; `audit_log` yozuvi qoladi.

### BL-UM-5 OAuth username generatsiyasi

`base = sanitize(providerLogin)` (ruxsat etilmagan belgilar olib tashlanadi, 3 dan qisqa bo'lsa `user`); band bo'lsa `base + random(1000..9999)`, 5 urinishgacha; keyin `user_<id>`.

---

## 2. Programming Languages

### BL-PL-1 Foydalanuvchi tillarini saqlash (`PUT /me/languages`)

```text
input: [{slug, primary}]
validate: ≥ 1 til; aynan 1 primary; barcha slug PUBLISHED    → 400 / 409 LANGUAGE_NOT_AVAILABLE
TX:
  mavjud bo'lmaganlar: insert(is_active=true)
  ro'yxatda yo'qlar: is_active=false (progress saqlanadi)
  qayta qo'shilganlar: is_active=true
  is_primary: avval hammasi false, keyin tanlangani true
  yangi til uchun user_topic_progress yaratilmaydi (lazy — BL-LS-1)
```

### BL-PL-2 Tilni nashr qilish tayyorligi

`ready = topics_published ≥ 8 AND ∀(topic, difficulty): published_exercises ≥ 5 AND ranked_pool_count ≥ 30 AND barcha mavzu nomlari 4 lokalda`. Javobda yetishmayotgan `(topic, difficulty, have, need)` ro'yxati.

---

## 3. Language Learning Structure

### BL-LS-1 Daraja holati (lazy hisoblash)

`user_topic_progress` qatori birinchi tegishli sessiya tugaganda yoki placement'da yaratiladi. Qator yo'q bo'lsa holat hisoblanadi: `BEGINNER` → `AVAILABLE`, qolganlari → `LOCKED` (placement boshqacha belgilamagan bo'lsa).

```text
onSessionCompleted(session) — faqat PRACTICE, ACCURACY, CHALLENGE, CODE_SPRINT, WEAKNESS
                              (topic_id bor mashqlar uchun):
  for each exercise e in session (to'liq yozilgan, ya'ni shu snippet 100% tugagan):
     p = progress(user, e.topic, e.difficulty)  (yo'q bo'lsa yaratiladi)
     if p.state == LOCKED: skip   # qulflangan darajaga sessiya ochilmaydi (409 LEVEL_LOCKED), himoya
     acc = shu snippet bo'yicha accuracy
     upsert user_exercise_best(user, e.exercise_key): best_wpm, best_accuracy, completed(acc ≥ 95)
     recompute p:
        total      = COUNT(published exercises of topic×difficulty)        # exercise_key bo'yicha
        completed  = COUNT(user_exercise_best.completed)
        mastered   = COUNT(best_accuracy ≥ 98)
        state = MASTERED    if mastered == total
                COMPLETED   if completed ≥ ceil(0.8 × total)
                IN_PROGRESS if completed ≥ 1 yoki kamida 1 urinish
                AVAILABLE   aks holda
     if state ∈ {COMPLETED, MASTERED} and oldState ∉ {COMPLETED, MASTERED}:
        next = keyingi daraja; agar next.state == LOCKED → AVAILABLE
        publish TopicLevelCompleted(user, topic, difficulty)
```

- Holat hech qachon pasaymaydi: yangi mashq qo'shilib `completed/total` 80% dan tushsa ham `COMPLETED` saqlanadi (`state` monoton), lekin `MASTERED` → `COMPLETED` ga qaytadi (yangi mashq hali 98% bilan yozilmagan).
- `user_exercise_best` jadvali `12-database.md` da (`training` sxemasi).

### BL-LS-2 Sessiya ochishda tekshiruv

`PRACTICE`/`ACCURACY`/`CODE_SPRINT` uchun: so'ralgan `(topic, difficulty)` holati `LOCKED` bo'lsa → `409 LEVEL_LOCKED` + ochilish sharti (`completed`, `required`).

### BL-LS-3 Placement test

3 snippet ketma-ket (`PRACTICE` qoidalari, pauza yo'q): B, I, A. Har biri uchun `pass = accuracy ≥ 95 AND completion = 100`.

| Natija | Boshlang'ich holat (shu tilning barcha mavzularida) |
| --- | --- |
| B o'tmadi yoki test o'tkazib yuborildi | B: `AVAILABLE`; qolgani `LOCKED` |
| B o'tdi, I o'tmadi | B: `AVAILABLE` (tavsiya I emas), I: `AVAILABLE` |
| B va I o'tdi, A o'tmadi | B, I, A: `AVAILABLE` |
| Hammasi o'tdi | B, I, A, E: `AVAILABLE` |

`user_languages.start_level` = eng yuqori ochilgan daraja. Placement sessiyalari oddiy sessiya sifatida saqlanadi (`mode=PRACTICE`, `is_placement=true`), XP beriladi, baseline WPM shu yerdan.

### BL-LS-4 Til progressi foizi

`progress% = COUNT(state ∈ {COMPLETED, MASTERED}) / (published_topics × 4) × 100`, butun songacha pastga yaxlitlanadi.

---

## 4. Training Modes va sessiya hayot sikli

### BL-TM-1 Snippet tanlash (umumiy)

```text
pick(user|guest, language, filter, count):
  candidates = PUBLISHED exercises matching filter
  recent = oxirgi 20 sessiyada ko'rilgan exercise_key lar (mehmon: guest_id bo'yicha)
  pool = candidates − recent
  if |pool| < count:
      pool = pool ∪ (candidates ∩ recent, eng eski ko'rilganidan boshlab) — yetguncha
  if |candidates| == 0 → 409 NO_EXERCISES_AVAILABLE
  return random sample(pool, count)   # SecureRandom shart emas
```

| Rejim | `filter` | `count` |
| --- | --- | --- |
| PRACTICE | topic, difficulty, type=SNIPPET; yoki aniq `exerciseKey` | 1 |
| SPEED_TEST | ranked_pool = true | 3 (boshida), keyin `next` bilan 2 tadan |
| ACCURACY | topic, difficulty (yoki "tavsiya": IN_PROGRESS darajalardan) | 1 |
| CHALLENGE | difficulty ∈ {ADVANCED, EXPERT}, ochilgan mavzulardan | 1 |
| SPECIAL_CHARS | `BL-SC-1` | 1 (generatsiya) |
| WEAKNESS | `BL-TM-4` | 3 |
| CODE_SPRINT | topic; darajalar `BL-TM-5` | 5 |

### BL-TM-2 Sessiya holat mashinasi

```text
            POST /sessions
                 │
             STARTED ──── POST /abandon yoki 24 soat muddat ───► ABANDONED
                 │
        POST /complete
                 │
   ┌─────────────┼───────────────────┐
strukturaviy/   OK                  OK, shubha ≥ 50
vaqt xatosi      │                   │
   ▼             ▼                   ▼
REJECTED     COMPLETED           COMPLETED (ranked=false, flagged)
             (ranked = kirish        │
              shartlariga ko'ra)     └─ admin: CLEAR → ranked qayta hisoblanadi
                                              CONFIRM → REJECTED (XP qaytariladi)
```

- `COMPLETED` yakuniy holat (faqat admin `CONFIRM` bilan `REJECTED` ga o'tadi).
- `complete` idempotent: `COMPLETED`/`REJECTED` sessiyaga takroriy chaqiruv saqlangan javobni qaytaradi (`200`).
- `ABANDONED` sessiyaga `complete` → `409 SESSION_NOT_ACTIVE`.
- Bir foydalanuvchida bir vaqtda ko'pi bilan 3 ta `STARTED` sessiya; to'rtinchisi ochilganda eng eskisi `ABANDONED`.

### BL-TM-3 Rejimga xos tugash va natija

| Rejim | Server tekshiruvi (replay'dan keyin) | `completionPct` |
| --- | --- | --- |
| PRACTICE | Oxirgi belgi yozilgan bo'lishi shart emas (foydalanuvchi erta tugatishi mumkin — `finishEarly` hodisasi) | yozilgan / jami |
| SPEED_TEST | Faol vaqt ≤ limit + 500 ms; limitdan keyingi hodisalar e'tiborsiz | har doim 100 (vaqt tugadi) yoki erta tugatilsa vaqt ulushi |
| ACCURACY | Accuracy birinchi marta < 97% bo'lgan nuqtada replay to'xtatiladi (≥ 20 bosishdan keyin tekshiriladi) | to'xtash nuqtasigacha |
| CHALLENGE | Faol vaqt ≤ chegarasi; undan keyingi hodisalar e'tiborsiz | yozilgan / jami |
| SPECIAL_CHARS | — | yozilgan / jami |
| WEAKNESS, CODE_SPRINT | Snippetlar tartibi `session_exercises.seq` ga mos | tugatilgan belgilar / jami |

`completionPct < 100` bo'lgan sessiya `COMPLETED` bo'ladi va XP oladi (yozilgan `C` bo'yicha), lekin mashq "tugatilgan" hisoblanmaydi va rekord/reytingga kirmaydi (SPEED_TEST bundan mustasno: u vaqt bo'yicha tugaydi).

Minimal sessiya: `K✓ + K✗ < 20` yoki faol vaqt `< 5 s` bo'lsa → `422 SESSION_TOO_SHORT`, sessiya `ABANDONED`.

### BL-TM-4 Weakness Training tanlovi

```text
profile = WeaknessProfile.forUser(user, lang)        # 10 §A.5: top 5 char + 5 bigram, bali > 1.0
if profile.empty → 409 WEAKNESS_PROFILE_EMPTY (UI: "kamida N ta sessiya kerak")
ex1 = SPECIAL_CHARS generatsiyasi, belgilar = profile.chars ∪ profile.bigrams (BL-SC-1)
candidates = PUBLISHED SNIPPET, daraja ∈ foydalanuvchi shu tilda ochgan darajalar
score(e) = Σ_{x ∈ profile} weakness(x) × freq(x, e) / e.char_count
           (bigram freq: e.bigram_freq dan; yo'q bo'lsa 0)
top20 = eng yuqori 20 (recent chiqarilgan)
ex2, ex3 = random 2 from top20
```

### BL-TM-5 Code Sprint darajalari

Mavzuda foydalanuvchi ochgan eng yuqori daraja `L` bo'lsin (indeks 0..3). 5 snippet darajalari: `[max(L−1,0), max(L−1,0), L, L, L]`; har darajadan `BL-TM-1` bilan tanlanadi. Snippetlar orasida klient 3 s tanaffus ko'rsatadi (keylog'da `next` hodisasi; tanaffus faol vaqtga kirmaydi).

### BL-TM-6 Challenge vaqt chegarasi

`limitSeconds = ceil(char_count / (medianCPM × 1.1) × 60)`, `[30, 900]` oralig'iga siqiladi. `medianCPM` — `00` §4.4. Chegaradan o'tilsa sessiya tugaydi; natija ekranida "vaqt tugadi" va erishilgan foiz.

### BL-TM-7 Sessiyani yakunlash (server)

To'liq oqim `13-backend-architecture.md` §4. Qisqa:

```text
complete(sessionId, completeToken, keylog, clientElapsedMs, clientMetrics):
  1. verify completeToken (imzo, sessionId, exp)                  → 401/422
  2. lock session FOR UPDATE; if status != STARTED → return stored result | 409
  3. if now > startedAt + 24h → ABANDONED, 422 SESSION_EXPIRED
  4. decode keylog (size limits)                                  → 422 KEYLOG_INVALID → REJECTED
  5. replay → official metrics, per-char stats                    (07 §8)
  6. validate timing & rhythm → REJECTED | suspicion score        (07 §9)
  7. mode rules (BL-TM-3); SESSION_TOO_SHORT tekshiruvi
  8. ranked = eligibility(…)                                      (11 §6.2)
  9. persist session, keylog, char stats, exercise bests, topic progress (BL-LS-1)
 10. publish SessionCompleted → (sync) gamification rewards; (async) analytics, leaderboard, coach
 11. return result + rewards + progress
```

---

## 5. Exercise Management

### BL-EM-1 Validatsiya (saqlash va import)

Xatolar `errors[]` da `{rule, line, col, message}` ko'rinishida; birortasi bo'lsa saqlanmaydi (`DRAFT` ham).

| # | Qoida | Xato kodi |
| --- | --- | --- |
| 1 | Faqat ruxsat etilgan belgilar (`00` §4.2) | `CHAR_NOT_ALLOWED` |
| 2 | Qator oxiri faqat `\n`; `\r` yo'q | `CRLF_NOT_ALLOWED` |
| 3 | Qator oxirida bo'shliq yo'q | `TRAILING_WHITESPACE` |
| 4 | Fayl oxirida bo'sh qator yo'q; ketma-ket 2 dan ortiq bo'sh qator yo'q | `BLANK_LINES` |
| 5 | Indent: `SPACE` tilda `\t` yo'q; `TAB` tilda qator boshida probel yo'q. Indent kengligi `indent_size` ga karrali — **bundan mustasno:** SQL/PL/SQL da klauza tekislash (river) uchun ixtiyoriy kenglik ruxsat | `INDENT_STYLE` |
| 6 | Qator uzunligi ≤ 80 | `LINE_TOO_LONG` |
| 7 | `char_count` daraja oralig'ida (`00` §4.2) | `LENGTH_OUT_OF_RANGE` |
| 8 | `code_hash` (SHA-256, normalizatsiyadan keyin) shu tilda unikal | `DUPLICATE_EXERCISE` |
| 9 | Til bo'yicha taqiqlangan konstruksiyalar (BL-EM-3) | `FORBIDDEN_CONSTRUCT` |
| 10 | "Aqlli" qo'shtirnoq va tire (`“ ” ‘ ’ – —`) yo'q (1-qoidadan kelib chiqadi, alohida xabar bilan) | `SMART_PUNCTUATION` |
| 11 | Kamida 2 qator (`SPECIAL_CHARS` turidan tashqari) | `TOO_FEW_LINES` |
| 12 | `SPECIAL_CHARS` turi: `topic_id` NULL; boshqa turlar: `topic_id` majburiy | `TOPIC_REQUIRED` |

Ogohlantirishlar (saqlashga to'sqinlik qilmaydi): maxsus belgilar ulushi daraja mo'ljalidan tashqarida; ichma-ichlik chuqurligi mo'ljaldan tashqarida; 2 dan ortiq izoh qatori.

### BL-EM-2 Hisoblanadigan maydonlar

- `char_count` = belgilar soni, avtomatik indent (qator boshidagi bo'shliq) va hisobga kirmaydi; `\n` kiradi.
- `line_count` = qatorlar soni.
- `special_ratio` = (harf, raqam, probel, `\n` bo'lmagan belgilar) / `char_count`.
- `char_freq` = `{belgi: son}` (`\n` → `"\\n"`), avtomatik indentsiz.
- `bigram_freq` = til `token_patterns` + kamida bitta maxsus belgi qatnashgan bigramlar soni.
- `nesting_depth` = maksimal indent darajasi (`leading / indent_size`, pastga yaxlitlab).
- `expected_seconds` = `00` §4.2 formulasi.

### BL-EM-3 Taqiqlangan konstruksiyalar (regex, registrdan mustaqil)

| Til | Taqiqlangan | Sabab |
| --- | --- | --- |
| `plsql` | `\$\$`, `LANGUAGE\s+plpgsql`, `RAISE\s+NOTICE`, `RETURNS\s`, `PERFORM\s`, `DELIMITER`, `@\w+\s*=`, `DECLARE\s+@`, `\bGO\b` (alohida qatorda) | PL/pgSQL, MySQL, T-SQL sintaksisi |
| `sql` (tegsiz) | `\bNVL\(`, `\bDECODE\(`, `\(\+\)`, `::\w`, `\bILIKE\b`, `\bTOP\s+\d`, `` ` `` | Dialektga xos — teg talab qilinadi (`oracle`/`postgres`); teg bo'lsa ruxsat |
| Barcha | `TODO`, `FIXME`, `lorem ipsum`, `foo`/`bar`/`baz` identifikatorlari | Idiomatik, real kod talabi |

### BL-EM-4 Versiyalash

`PUT /admin/exercises/{id}`:
- `status ∈ {DRAFT, IN_REVIEW}` → joyida tahrirlanadi.
- `status = PUBLISHED` va `code` o'zgargan → yangi qator (`exercise_key` bir xil, `version+1`, `status=DRAFT`); nashr qilinganda eski versiya `ARCHIVED`. Eski sessiyalar eski `exercise_id` ga ishora qilaveradi.
- `status = PUBLISHED` va faqat metama'lumot (izoh, teg, ranked pool, daraja **emas**) o'zgargan → joyida; audit yoziladi.
- Daraja yoki mavzuni o'zgartirish = yangi versiya.

---

## 6. User Dashboard

### BL-UD-1 "Davom ettirish"

```text
lang = oxirgi sessiya tili (7 kun ichida) yoki asosiy til
1. shu tilda state=IN_PROGRESS darajalar, updated_at DESC → birinchisi;
   mashq = shu darajada tugatilmagan, eng past sort tartibidagi
2. yo'q bo'lsa: state=AVAILABLE darajalar ichida (topic.sort_order ASC, difficulty ASC) birinchisi
3. yo'q bo'lsa (hammasi COMPLETED): eng past MASTERED bo'lmagan daraja — "100% ga yetkazing"
4. hammasi MASTERED: SPEED_TEST 60 s taklifi
```

---

## 7. Admin

### BL-AD-1 Shubhali sessiya qarori

- `CLEAR`: `suspicion_score` saqlanadi, `review_status=CLEARED`; `ranked` kirish shartlari bo'yicha qayta hisoblanadi; `true` bo'lsa rekord va reyting hodisalari qayta nashr qilinadi.
- `CONFIRM`: `status=REJECTED`, `reject_reason=ADMIN_CONFIRMED`; XP ledger'ga manfiy tranzaksiya (`source_type=ADMIN`, `source_id=revoke:<sessionId>`); rekord va reyting yozuvlari foydalanuvchining qolgan sessiyalaridan qayta hisoblanadi; analytics agregatlari tuzatilmaydi (ta'sir kichik) — **bundan mustasno** `max_wpm`, u qayta hisoblanadi.
- Bir foydalanuvchida 30 kunda 3 ta `CONFIRM` → admin'ga bloklash taklifi (avtomatik bloklanmaydi).

### BL-AD-2 Rol qoidalari

- Faqat `ADMIN` rol o'zgartiradi.
- Foydalanuvchi o'z rolini o'zgartira olmaydi → `409 CANNOT_CHANGE_OWN_ROLE`.
- Tizimda oxirgi faol `ADMIN` pasaytirilmaydi/bloklanmaydi → `409 LAST_ADMIN`.
- Rol `EDITOR`/`ADMIN` ga ko'tarilganda 2FA yoqilmagan bo'lsa, birinchi admin so'rovida `403 TWO_FACTOR_REQUIRED`.
