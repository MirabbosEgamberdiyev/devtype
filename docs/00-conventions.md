# 00. Konvensiyalar, atamalar va kanonik konstantalar

Bu hujjat barcha boshqa hujjatlar uchun umumiy asos. Boshqa hujjatdagi qiymat shu yerdagi qiymatdan farq qilsa — **shu hujjat to'g'ri**.

## 1. Hujjatlar ustuvorligi

Ziddiyat bo'lsa, yuqoridagisi ustun:

1. `00-conventions.md` (konstantalar, atamalar)
2. `23-decisions-open-questions.md` (qarorlar jurnali)
3. Modulga xos texnik hujjat (`07`, `08`, `09`, `10`, `11`, `12`, `14`, `16`, `22`)
4. `03-business-logic.md`
5. `02-functional-requirements.md`
6. `01-prd.md`, `05-ui-ux-design-system.md`
7. Qolganlar

## 2. Talab kalit so'zlari

| So'z | Ma'nosi |
| --- | --- |
| **SHART** (MUST) | Majburiy; bajarilmasa reliz chiqmaydi |
| **KERAK** (SHOULD) | Kutilgan; chetlanish qaror jurnaliga yoziladi |
| **MUMKIN** (MAY) | Ixtiyoriy |
| **OPEN QUESTION** | Mahsulot egasi qarori kutilmoqda; standart qiymat ko'rsatilgan va implementatsiya shu qiymat bilan davom etadi |

## 3. ID tizimi

| Tur | Format | Misol | Qayerda |
| --- | --- | --- | --- |
| Funksional talab | `FR-<MOD>-<n>` | `FR-TE-4` | `02` |
| Acceptance criteria | `AC-<MOD>-<n>` | `AC-TE-4` | `18` |
| Biznes qoida / algoritm | `BL-<MOD>-<n>` | `BL-GM-2` | `03` |
| Nofunksional talab | `NFR-<n>` | `NFR-3` | `01`, `17` |
| Qaror | `D-<n>` | `D-12` | `23` |
| Ochiq savol | `OQ-<n>` | `OQ-3` | `23` |
| API xato kodi | `UPPER_SNAKE` | `SESSION_EXPIRED` | `14` |

Modul kodlari: `UM` User Management · `PL` Programming Languages · `LS` Language Learning Structure · `SQL` SQL Training · `PLS` PL/SQL Training · `TE` Typing Engine · `TM` Training Modes · `SC` Special Characters · `VK` Virtual Keyboard · `CE` Code Editor · `AN` Analytics · `AI` AI Coach · `GM` Gamification · `EM` Exercise Management · `UD` User Dashboard · `UX` UI/UX · `AD` Admin · `BE` Database & Backend · `SP` Security & Performance · `QA` Testing · `QS` Quality Standards · `FF` Future Features · `AC` Acceptance Criteria.

## 4. Kanonik konstantalar

Kodda bu qiymatlar nomlangan konstanta sifatida bir joyda e'lon qilinadi. "Sozlanadi" ustuni: `cfg` — `application.yml` orqali o'zgaradi; `db` — admin panel/bazadan; `—` — kod konstantasi.

### 4.1 Tillar va lokallar

| Kalit | Qiymat |
| --- | --- |
| UI lokallari | `uzl`, `uzc`, `ru`, `en` (manba: `en`) |
| BCP 47 moslamasi | `uzl`→`uz-Latn`, `uzc`→`uz-Cyrl`, `ru`→`ru`, `en`→`en` |
| Zaxira zanjiri | `uzc`→`uzl`→`en`; `uzl`→`en`; `ru`→`en` |
| Dasturlash tillari (slug) | `java`, `python`, `javascript`, `typescript`, `sql`, `plsql`, `c`, `cpp`, `csharp`, `php`, `go`, `kotlin`, `rust`, `html`, `css`, `bash` |
| 1-reliz tillari | `java`, `python`, `javascript`, `sql`, `plsql` (OQ-1) |
| 2-reliz tillari | `typescript`, `csharp`, `go`, `kotlin`, `cpp`, `bash` |
| 3-reliz tillari | `c`, `php`, `rust`, `html`, `css` |

### 4.2 Kontent

| Kalit | Qiymat | Sozlanadi |
| --- | --- | --- |
| Darajalar | `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT` | — |
| Snippet uzunligi (belgi, avtomatik indentsiz) | B: 80–200 · I: 200–450 · A: 450–900 · E: 900–1800 | cfg |
| Maxsus belgilar ulushi (mo'ljal, qat'iy emas) | B: <15% · I: 15–22% · A: 22–30% · E: >30% | cfg |
| Qator uzunligi (maks.) | 80 belgi | cfg |
| Mavzu×daraja uchun minimal snippet | 5 | cfg |
| Ruxsat etilgan belgilar | ASCII 0x20–0x7E, `\n`; `\t` faqat `indent_style=TAB` tillarda qator boshida | — |
| Kutilgan davomiylik | `ceil(char_count / 200 × 60)` soniya (200 CPM bazasi) | cfg |
| Ranked pool mezoni | `INTERMEDIATE`, maxsus belgilar ulushi 15–22%, uzunlik 250–450 | cfg |

### 4.3 O'quv progressi

| Kalit | Qiymat |
| --- | --- |
| Mashq "tugatilgan" sharti | `COMPLETED` sessiya, accuracy ≥ 95%, completion 100% |
| Keyingi daraja ochilishi | Oldingi darajadagi nashr qilingan mashqlarning ≥ 80% i tugatilgan |
| `MASTERED` | Darajadagi barcha mashqlar accuracy ≥ 98% bilan tugatilgan |
| Placement test | 3 snippet (B, I, A dan bittadan); natija qoidasi `BL-LS-3` |
| Snippet takrorlanmasligi | Oxirgi 20 sessiyada ko'rilganlar chiqariladi |

### 4.4 Rejimlar

| Rejim (`mode`) | Xato rejimi | Tugash | XP koeff. | Reytingli |
| --- | --- | --- | --- | --- |
| `PRACTICE` | stop-on-error (sozlamada o'chiriladi); istisno: "kunning snippeti" challenge'i — erkin, pauzasiz (`11` §7) | Snippet oxiri | 1.0 | Yo'q (kunlik snippet — `DAILY` doskasi) |
| `SPEED_TEST` | erkin | 30 / 60 / 120 s | 1.2 | Ha |
| `ACCURACY` | stop-on-error | Snippet oxiri yoki accuracy < 97% | 1.3 | Yo'q |
| `CHALLENGE` | erkin | Snippet oxiri yoki vaqt chegarasi | 1.5 | Yo'q |
| `SPECIAL_CHARS` | stop-on-error | 150 belgi | 1.0 | Yo'q |
| `WEAKNESS` | stop-on-error | 3 snippet | 1.4 | Yo'q |
| `CODE_SPRINT` | erkin | 5 snippet | 1.5 (+30 XP bonus) | Yo'q |

| Kalit | Qiymat |
| --- | --- |
| Speed Test davomiyliklari | 30, 60, 120 s |
| Accuracy "master" varianti | Birinchi xatoda tugaydi |
| Challenge vaqt chegarasi | `char_count / (medianCPM × 1.1)` daqiqa; `medianCPM` — shu tilda oxirgi 10 sessiya mediani; < 5 sessiya bo'lsa 150 CPM |
| Sprint tanaffusi | 3 s |
| Erkin rejimda ketma-ket xato chegarasi | 8 (keyin kiritish bloklanadi, faqat Backspace) |
| Harakatsizlik pauzasi (stop-on-error rejimlar) | 15 s |
| Vaqtli rejimda sahifa yashirin | > 2 s → sessiya `ABANDONED` |
| Sessiya amal qilish muddati | `startedAt` + 24 soat (`complete` qabul qilinadi) |

### 4.5 Metrikalar

| Kalit | Formula |
| --- | --- |
| WPM (net) | `(C / 5) / m` |
| Raw WPM | `(T / 5) / m` |
| CPM | `C / m` |
| Accuracy | `K✓ / (K✓ + K✗) × 100` |
| Consistency | `clamp(100 × (1 − σ/μ), 0, 100)` — soniyalik raw WPM qatori bo'yicha |
| Progress | `typedPositions / (targetLen − autoCount) × 100` |
| Yaxlitlash | WPM, CPM, accuracy, consistency — 2 kasr xona, `HALF_UP`; UI'da WPM butun, accuracy 1 kasr |

Belgilanishlar `07-typing-engine.md` §6 da.

### 4.6 Validatsiya (anti-cheat)

| Kalit | Qiymat | Sozlanadi |
| --- | --- | --- |
| Vaqt bag'ri | `ΣΔt ≤ serverElapsed + 2000 ms` | cfg |
| WPM shubha chegarasi | > 220 → +50 | cfg |
| Tez intervallar | < 20 ms intervallar ulushi > 20% → +50 | cfg |
| Ritm | Intervallar CV < 0.15 → +40 | cfg |
| Sakrash | Shaxsiy mediandan (≥ 10 sessiya bo'lsa) ≥ 60% yuqori → +20 | cfg |
| Xatosiz tez | `K✗ = 0`, WPM > 150 va `C ≥ 300` → +20 | cfg |
| Bayroq chegarasi | Shubha balli ≥ 50 → `ranked=false`, admin navbati | cfg |
| Keylog chegaralari | So'rov tanasi ≤ 256 KB; ochilgan ≤ 1 MB; hodisalar ≤ 50 000 | cfg |

### 4.7 Gamification

| Kalit | Qiymat |
| --- | --- |
| XP | `round((C/10) × modeCoef × diffCoef × accFactor × repeatFactor) + bonuses` |
| `diffCoef` | B 1.0 · I 1.25 · A 1.5 · E 2.0; ko'p snippetli sessiyada belgilar bo'yicha vaznli o'rtacha |
| `accFactor` | acc < 85: 0.25 · 85 ≤ acc < 95: `0.5 + (acc − 85) × 0.05` · 95 ≤ acc < 98: 1.0 · 98 ≤ acc < 100: 1.1 · 100: 1.25 |
| `repeatFactor` | Shu mashq 24 soat ichida: 1-marta 1.0 · 2-marta 0.5 · 3+ 0.1 |
| Bonuslar | Shaxsiy rekord +25 · kunning birinchi sessiyasi +10 · Sprint to'liq +30 |
| Level formulasi | `xpToNext(n) = round(100 × n^1.5)` |
| Til ranki (oxirgi 10 sessiya median WPM) | Bronze < 30 · Silver 30–<45 · Gold 45–<60 · Platinum 60–<80 · Diamond ≥ 80 (OQ-4) |
| Streak kuni | Foydalanuvchi timezone'ida ≥ 180 s faol yozish |
| Streak freeze | Oyiga 2 ta (har oyning 1-kuni tiklanadi), avtomatik sarflanadi |
| Streak mukofotlari | 7 kun +50 XP · 30 kun +300 XP · 100 kun +1500 XP |
| Reyting kirish sharti | Email tasdiqlangan · `PHYSICAL` · strict indent o'chiq · accuracy ≥ 95% · shubha < 50 · ranked pool |
| Reyting keshi | Top-100, 60 s |
| DevScore | Eng kuchli 3 tildagi `SPEED_TEST` 60 s rekord WPM yig'indisi |
| Kunlik challenge yaratilishi | 00:00 UTC |
| Haftalik challenge | Dushanba 00:00 UTC |

### 4.8 Analytics va Coach

| Kalit | Qiymat |
| --- | --- |
| EWMA koeffitsienti | α = 0.2 |
| Zaiflik balli | `0.6 × z(ewma_error) + 0.4 × z(ewma_ms)` |
| Minimal kuzatuv | 30 (belgi/bigram uchun) |
| Zaiflik chegarasi | > 1.0 |
| Zaiflik profili hajmi | 5 belgi + 5 bigram |
| "Hozirgi WPM" | Oxirgi 10 sessiya mediani |
| Grafik uchun minimal sessiya | 5 |
| Plato | Oxirgi 15 sessiyada chiziqli trend qiyaligi < 0.1 WPM/sessiya |
| Coach kunlik rejasi | 3 ta vazifa, 00:00 foydalanuvchi vaqti bilan yangilanadi (lazy) |
| Coach LLM chaqiruv limiti | Foydalanuvchiga haftada 1 hisobot + kuniga 3 so'rov (OQ-5) |

### 4.9 Xavfsizlik

| Kalit | Qiymat |
| --- | --- |
| Parol | 10–128 belgi; Argon2id (m=19 MiB, t=2, p=1) |
| Access JWT | RS256, 15 daqiqa |
| Refresh token | 30 kun, rotation, `HttpOnly; Secure; SameSite=Strict`, yo'l `/api/v1/auth` |
| Mehmon tokeni | 7 kun |
| Email tasdiqlash tokeni | 24 soat |
| Parol tiklash tokeni | 1 soat |
| Rate limit | auth: 5/daq (IP+login) · sessiya ochish: 30/daq (user), 20/daq (mehmon IP) · umumiy API: 300/daq (user), 60/daq (anonim IP) — to'liq jadval `16` §5 |
| Avatar | PNG/JPEG/WebP, ≤ 2 MB, server tomonda 256×256 WebP ga qayta kodlanadi |
| Import fayl | JSON/YAML, ≤ 5 MB |

### 4.10 Unumdorlik

| Kalit | Qiymat |
| --- | --- |
| Klavish → ekran | ≤ 16 ms (p95), 1800 belgili snippetda |
| API p95 | ≤ 200 ms; `complete` ≤ 400 ms |
| Lighthouse (ommaviy sahifalar, mobile profil) | Performance ≥ 90 · Accessibility ≥ 95 · Best Practices ≥ 95 · SEO ≥ 95 |
| Boshlang'ich JS | ≤ 150 KB gzip |
| Bir vaqtdagi foydalanuvchilar (1-instans mo'ljali) | 1 000 |
| Mavjudlik | 99.5% / oy |

### 4.11 Saqlash muddatlari

| Ma'lumot | Muddat |
| --- | --- |
| `session_keylogs` | 90 kun (rekord va top-100 sessiyalarniki doimiy) |
| `session_char_stats` | 90 kun |
| Da'vo qilinmagan mehmon sessiyalari | 7 kun |
| Bir martalik tokenlar | 30 kun |
| Yopilgan kunlik/haftalik reyting doskalari | 90 kun |
| `audit_log` | 2 yil |
| O'chirilgan akkaunt | 30 kun muhlat, keyin anonimlashtirish |

## 5. Atamalar

| Atama | Ta'rif |
| --- | --- |
| **Snippet / mashq (exercise)** | Yoziladigan kod bo'lagi va uning metama'lumoti |
| **Sessiya** | Bitta mashg'ulot urinishi: ochilishdan `complete`/`abandon` gacha |
| **Keylog** | Sessiyadagi barcha klavish hodisalarining vaqt bilan yozuvi |
| **Replay** | Keylog'ni qayta ijro etish (serverda — validatsiya; UI'da — ko'rish) |
| **Avtomatik indent** | Qator boshidagi bo'shliq; standart rejimda foydalanuvchi termaydi |
| **Strict indent** | Indentni qo'lda terish rejimi; reytingga kirmaydi |
| **Stop-on-error** | Xato belgi tuzatilmaguncha kursor siljimaydigan rejim |
| **Erkin rejim** | Xato belgidan keyin kursor davom etadigan rejim |
| **Ranked pool** | Reytingli Speed Test uchun kalibrlangan snippetlar to'plami |
| **Token pattern** | Ko'p belgili operator (`:=`, `=>`), alohida kuzatiladi |
| **Bigram** | Ketma-ket ikki belgi juftligi |
| **Belgi turkumi** | Maxsus belgilar guruhi (qavslar, tinish, …) — `08` §2 |
| **Til (dasturlash)** | `content.languages` yozuvi; UI tili bilan adashtirilmasin — u "lokal" |
| **Mehmon** | Ro'yxatdan o'tmagan foydalanuvchi, `guest_id` bilan |
| **Coach** | AI Personal Coach moduli (`10` §B) |

## 6. Formatlash konvensiyalari

- Vaqt: UTC, ISO-8601 (`2026-11-14T14:42:10.120Z`); bazada `TIMESTAMPTZ`.
- ID: ichki — `BIGINT`; klientga chiqadigan sessiya ID — UUID v7.
- JSON maydonlari: `camelCase`; enum qiymatlari: `UPPER_SNAKE`.
- Baza: `snake_case`; jadval nomlari ko'plikda.
- Java paketlari: `com.devtype.<module>.<layer>`.
- Frontend: komponentlar `PascalCase.tsx`, hooklar `useX.ts`, test `*.test.ts(x)`.
- Commit: Conventional Commits (`feat(training): …`).
