# 24. Hujjatlashtirish auditi

Audit sanasi: 2026-11-15. Qamrov: `README.md`, `CLAUDE.md`, `docs/00`–`docs/23`. Maqsad: hujjatlar to'plami Claude Code yoki boshqa developer uchun yagona, izchil va taxminsiz texnik spetsifikatsiya ekanini tekshirish.

> Bu audit **hujjatlarni** tekshiradi, kodni emas — kod hali yo'q. "Bajarildi" bu yerda "hujjatlashtirildi va tekshirildi" degani; funksiya ishlashi faqat implementatsiyadan keyin `18` va `21` bo'yicha isbotlanadi.

## 1. Xulosa

| Ko'rsatkich | Natija |
| --- | --- |
| Hujjatlar | 25 ta (`00`–`24`) + `README.md` + `CLAUDE.md`; ~54 000 so'z |
| Funksional talablar | 171 ta `FR-*` (23 modul) + 62 ta `FR-SP-*` (xavfsizlik, unumdorlik) |
| Acceptance Criteria | 176 ta qator `AC-*`; **AC'siz FR: 0** (skript bilan tekshirildi) |
| Biznes qoidalar | 44 ta `BL-*`; har biri aniq bo'limda ta'riflangan (havolasiz qoida: 0) |
| Qarorlar | 30 ta `D-*`; 21 ta ziddiyat (`Z-*`) yechimi bilan |
| Ochiq savollar | 14 ta `OQ-*`; har birida standart qiymat — implementatsiyani to'xtatmaydi |
| Ta'riflanmagan `D`/`OQ` havolasi | 0 |
| Buzilgan hujjat havolasi | 0 |
| Baza DDL | 44 jadval; **PostgreSQL 16 da xatosiz yuklandi** (§4) |

**Hukm:** hujjatlar to'plami implementatsiyani boshlash uchun yetarli va izchil. Qolgan noaniqliklar `OQ-*` sifatida aniq belgilangan va standart qiymatga ega.

## 2. 20 ta hujjat talabi → qamrov

| # | Talab | Hujjat(lar) | Holat |
| --- | --- | --- | --- |
| 1 | To'liq PRD | `01` | ✓ |
| 2 | 23 modulning Functional Requirements | `02` | ✓ |
| 3 | Har modul algoritmi va Business Logic | `03`, `07`, `08`, `10`, `11` | ✓ |
| 4 | Barcha sahifalar UI/UX va Design System | `05` (tokenlar, komponentlar, 18+ sahifa) | ✓ |
| 5 | User Journey va to'liq Sitemap | `04` | ✓ |
| 6 | SQL va Oracle PL/SQL batafsil o'quv dasturi | `06` §3, §4 (16 + 18 mavzu × 4 daraja mazmuni, formatlash, konstruksiyalar) | ✓ |
| 7 | Typing Engine algoritmlari va formulalari | `07` | ✓ |
| 8 | Special Characters va Virtual Keyboard | `08` | ✓ |
| 9 | Developer Code Editor | `09` | ✓ |
| 10 | AI Personal Coach va Analytics algoritmlari | `10` | ✓ |
| 11 | Gamification va Leaderboard qoidalari | `11` | ✓ |
| 12 | ERD, to'liq schema, migratsiya strategiyasi | `12` | ✓ |
| 13 | Backend Architecture va REST API | `13`, `14` | ✓ |
| 14 | Frontend Architecture va component structure | `15` | ✓ |
| 15 | Authentication, Authorization, Security | `16` | ✓ |
| 16 | Performance va Scalability | `17` | ✓ |
| 17 | Testing Strategy va Acceptance Criteria | `18` | ✓ |
| 18 | DevOps, Docker, CI/CD, Deployment | `19` | ✓ |
| 19 | Roadmap va implementatsiya bosqichlari | `20` | ✓ |
| 20 | Production Readiness Checklist | `21` | ✓ |
| + | 4 tilli sayt (uzl, uzc, ru, en) — oldingi talab | `22` | ✓ |

## 3. 23 modul → qamrov

| # | Modul | FR | AC | Algoritm | Ma'lumot | API | UI |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | User Management | UM-1…13 | ✓ | BL-UM-1…5 | `identity` | §2, §3 | `05` §5.2, §5.16–17 |
| 2 | Programming Languages | PL-1…7 | ✓ | BL-PL-1…2 | `content.languages` | §4, §9 | §5.4 |
| 3 | Language Learning Structure | LS-1…7 | ✓ | BL-LS-1…4 | `user_topic_progress`, `user_exercise_best` | §4 | §5.7–5.11 |
| 4 | SQL Training | SQL-1…5 | ✓ | `06` §3 | kontent | §4–5 | §5.7–5.11 |
| 5 | PL/SQL Training | PLS-1…7 | ✓ | `06` §4, BL-EM-3 | kontent | §4–5 | §5.7–5.11 |
| 6 | Typing Engine | TE-1…16 | ✓ | `07` | `sessions`, `session_keylogs` | §5 | `05` §4, `09` |
| 7 | Training Modes | TM-1…10 | ✓ | BL-TM-1…7 | `sessions` | §5.1 | §5.6–5.11 |
| 8 | Special Characters | SC-1…6 | ✓ | BL-SC-1 | `user_char_stats` | §5, §6 | `08` §3 |
| 9 | Virtual Keyboard | VK-1…8 | ✓ | BL-VK-1…2 | statik layout JSON | §6 (heatmap) | `08` §4 |
| 10 | Developer Code Editor | CE-1…9 | ✓ | `09` | — | §5 (replay) | `09` |
| 11 | Analytics Dashboard | AN-1…9 | ✓ | BL-AN-1…3 | `analytics` | §6 | §5.12–5.13 |
| 12 | AI Personal Coach | AI-1…9 | ✓ | BL-AI-1…4 | `coach` | §7 | §5.5, `/coach` |
| 13 | Gamification | GM-1…11 | ✓ | BL-GM-1…9 | `gamification` | §8 | §5.14–5.15 |
| 14 | Exercise Management | EM-1…9 | ✓ | BL-EM-1…4 | `content.exercises` | §9 | §5.18 |
| 15 | User Dashboard | UD-1…8 | ✓ | BL-UD-1 | (yig'ma) | §3.1 | §5.5 |
| 16 | UI/UX Design | UX-1…10 | ✓ | `05` | `user_settings` | — | `05` |
| 17 | Admin Panel | AD-1…9 | ✓ | BL-AD-1…2 | `admin` | §9 | §5.18 |
| 18 | Database & Backend | BE-1…8 | ✓ | `13` §4–5 | `12` | `14` §1 | — |
| 19 | Security & Performance | FR-SP-1…69 | ✓ | `16`, `17` | — | — | — |
| 20 | Testing & QA | `18` | AC-QA-1 | `18` §3 | — | — | — |
| 21 | Platform Quality Standards | QS-1…9 | ✓ | — | — | — | — |
| 22 | Future Features | `23` §4 (13 ta, kengaytirish nuqtalari bilan) | — (implementatsiya qilinmaydi) | — | — | — | — |
| 23 | Acceptance Criteria | `18` §4 | 176 ta | — | — | — | — |

## 4. Bajarilgan tekshiruvlar

| # | Tekshiruv | Usul | Natija |
| --- | --- | --- | --- |
| T-1 | README'dagi barcha havolalar mavjud faylga olib boradi | Skript | ✓ |
| T-2 | Har `FR-*` uchun kamida bitta `AC-*` | Skript (`18` dagi FR ustunini tahlil qiladi) | ✓ — 171/171 (dastlab 23 ta yetishmovchilik topildi va `18` §4.22 bilan yopildi) |
| T-3 | Har `D-*`, `OQ-*` havolasi `23` da ta'riflangan | Skript | ✓ |
| T-4 | Har `BL-*` havolasi sarlavhada ta'riflangan | Skript | ✓ — 44/44 |
| T-5 | `` `NN` `` ko'rinishidagi hujjat havolalari mavjud hujjatga ishora qiladi | Skript | ✓ |
| T-6 | **DDL haqiqiy PostgreSQL 16 da** | `12` dagi barcha SQL bloklari bo'sh bazaga yuklandi | ✓ — 0 xato, 44 jadval, 3 partitsiyalangan jadval |
| T-7 | Reyting upsert semantikasi (`11` §6.3) | Shu bazada: 60 → 55 urinish | ✓ — yomonroq natija yozuvni almashtirmadi |
| T-8 | `12` dagi jadval soni sarlavhadagi ro'yxatga teng | Sanash | ✓ — 44 = 6+6+7+5+13+5+2 |
| T-9 | O'quv dasturi hajmi | Qo'lda hisob | ✓ — 231 mavzu × 4 × 5 = 4 620 snippet; MVP 5 × 8 × 4 × 5 = 800 |
| T-10 | API namunasidagi raqamlar formulalarga mos (`14` §5.3) | Qo'lda hisob | ✓ — dastlab nomuvofiq edi, tuzatildi: WPM 49.54, raw 51.41, CPM 247.72, accuracy 96.38, XP 102 |
| T-11 | Engine vektor namunasi (`07` §11) holat satri va WPM | Qo'lda hisob | ✓ — tuzatildi (WPM 200.00, holat satri 22 belgi) |
| T-12 | Konstantalar `00` §4 va modul hujjatlarida bir xil | Qo'lda solishtirish (XP, rank, streak, anti-cheat, rate limit, ranked pool, daraja chegaralari) | ✓ — 3 ta farq topildi va tuzatildi (§5) |
| T-13 | Backend modul bog'liqliklari siklsiz | Qo'lda graf tahlili | ✓ — `training ↔ analytics` sikli topildi va port inversiyasi bilan yechildi (`13` §1) |

## 5. Audit davomida topilgan va tuzatilgan nomuvofiqliklar

| # | Joy | Muammo | Tuzatish |
| --- | --- | --- | --- |
| A-1 | `18` | 23 ta FR (asosan R2/R3 va sifat standartlari) uchun AC yo'q edi | `18` §4.22 — 23 ta yangi AC |
| A-2 | `13` §1 | `training → analytics` (zaiflik profili) va `analytics → training` (hodisa) — modul sikli; Spring Modulith buni rad etardi | `WeaknessProfilePort` `training.api` da e'lon qilinib, `analytics` tomonidan implementatsiya qilinadi |
| A-3 | `00` §4.6 ↔ `07` §9.3 | `NO_ERRORS_FAST` shubha qoidasi `00` da yo'q edi | `00` ga qo'shildi |
| A-4 | `00` §4.9 ↔ `16` §5 | Mehmon rate limiti farqli (60 vs 20/daq) | `00` aniqlashtirildi: sessiya ochish 20/daq, anonim API 60/daq |
| A-5 | `00` §4.4 ↔ `11` §7 | "Kunning snippeti" PRACTICE rejimida, lekin erkin xato rejimi — `00` jadvaliga zid | `00` ga istisno qo'shildi |
| A-6 | `14` §5.3 | Namunaviy metrikalar va XP formulaga mos emas edi | Qayta hisoblandi |
| A-7 | `07` §11 | Vektor namunasida WPM 0 va noto'g'ri holat satri | Tuzatildi |
| A-8 | `22` §7 | "Level" va "Difficulty" o'zbek tilida bir xil ("daraja") bo'lib qolardi | "Daraja" / "Qiyinlik" ajratildi |
| A-9 | `02` FR-UM-9 | Mavjud bo'lmagan bo'limga havola | `05` §5.17 ga tuzatildi |

## 6. Ataylab qoldirilgan ochiq joylar

Bular xato emas — mahsulot egasi qarori yoki ma'lumot kutilmoqda; har biri standart qiymat bilan (`23` §3):

- **OQ-1, OQ-2, OQ-10, OQ-11** — 0-bosqich oxirigacha hal qilinishi kerak (til tanlovi, jamoa, indent standarti, hosting).
- **OQ-4, OQ-7** — real foydalanuvchi ma'lumotisiz aniqlab bo'lmaydi (rank chegaralari, metrika mo'ljallari).
- **OQ-8** — jonli poyga spetsifikatsiyasi R3 boshida yoziladi; `18` dagi AC-GM-13 shunga qadar dastlabki.
- **OQ-5** — LLM provayderi; Coach LLM'siz to'liq ishlaydi.

Taxminiy raqamlar (aniq belgilangan): jamoa va muddat (`20`), hajm baholari (`12` §7), i18n kalitlari soni (`22` §9), muvaffaqiyat metrikalari (`01` §6), server o'lchamlari (`19` §2).

## 7. Tekshirib bo'lmagan narsalar (cheklovlar)

- **Oracle PL/SQL namunalari** (`06` §4.2, §4.5) haqiqiy Oracle bazasida kompilyatsiya qilinmagan — bu muhitda Oracle yo'q. Sintaksis qo'lda tekshirildi; implementatsiyada `FR-PLS-6` (Oracle Database Free'da CI kompilyatsiyasi) majburiy.
- **SQL namunasi** (`06` §3.2) namunaviy sxema (`employees`, `departments`) hali yaratilmagani uchun bajarilmagan.
- **Unumdorlik byudjetlari** (`17`) — mo'ljal; ular implementatsiyada o'lchanadi, hujjatda isbotlanmaydi.
- **Uchinchi tomon kutubxona versiyalari va API'lari** (Shiki, Spring Modulith, `vite-ssg`, HIBP) — xotiradan; 0-bosqichda joriy versiyalar bilan tasdiqlanishi kerak (bu muhitda internet qidiruvi yo'q edi).
- **Huquqiy talablar** (GDPR, yosh chegarasi) — huquqshunos tomonidan tasdiqlanmagan (OQ-14).

## 8. Hujjatlarni yangilash qoidasi

1. Kod va hujjat farq qilsa — avval hujjat (qaror jurnali bilan), keyin kod (`CLAUDE.md` 10-qoida).
2. Yangi FR → `02` + `18` (AC) + tegishli texnik hujjat; T-2 skripti CI'da ishlaydi (`scripts/ac-coverage`).
3. Konstanta o'zgarishi faqat `00` §4 da; boshqa hujjatlar unga havola qiladi.
4. Har bosqich (gate) oxirida shu audit qayta o'tkaziladi va §4 jadvali yangilanadi.
