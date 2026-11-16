# 20. Development Roadmap va implementatsiya bosqichlari

Reja **28 hafta** va **4 kishilik jamoa**ni nazarda tutadi: 2 backend, 1 frontend, 1 dizayner/kontent muallifi (**OQ-2**). Jamoa hajmi boshqacha bo'lsa muddatlar mutanosib o'zgaradi; bosqichlar tartibi va mezonlari o'zgarmaydi. Bitta ijrochi (yoki Claude Code) uchun ham tartib shu — §3 dagi ketma-ketlik bo'yicha.

## 1. Bosqichlar

| Bosqich | Hafta | Natija | Chiqish mezoni (gate) |
| --- | --- | --- | --- |
| **0. Poydevor** | 1–2 | Repo, CI, muhitlar, dizayn tokenlari, engine yadrosi, vertikal kesim | **G0:** TS va Java engine ≥ 40 umumiy vektordan bir xil o'tadi; bitta snippet bilan to'liq zanjir (yozish → `complete` → server natijasi) staging'da ishlaydi |
| **1. MVP (R1)** | 3–12 | `01` §5.1 dagi R1 qamrovi | **G1:** barcha R1 AC'lari yashil; 800 snippet nashr qilingan (5 til × 8 mavzu × 4 daraja × 5); E2E-1…18 uch brauzerda; Lighthouse va typing perf byudjetlari; 4 lokal 100%; `21` nazorat ro'yxati → ochiq beta |
| **2. Raqobat va tahlil (R2)** | 13–20 | Weakness, Challenge, Sprint; to'liq analytics; Coach (qoida); achievement, challenge, reyting, musobaqa; +6 til; MVP tillarida qolgan mavzular | **G2:** R2 AC'lari; reyting faqat validatsiyadan o'tgan natijalar bilan (AC-SP-20, AC-GM-10); k6 SLO; ≥ 40 achievement |
| **3. Jonli musobaqa (R3)** | 21–28 | Jonli poyga, Coach LLM, ommaviy profil/follow, mobile Lite, +5 til | **G3:** 16 til nashr qilingan; yuklama testi 1 000 foydalanuvchi + 50 parallel poyga xonasi; LLM zaxira yo'li testlangan |

Mezon bajarilmaguncha keyingi bosqich boshlanmaydi.

## 2. Bosqichlar tafsiloti

### 2.1 Bosqich 0 — Poydevor (1–2-hafta)

| # | Vazifa | Natija | Hujjat |
| --- | --- | --- | --- |
| 0.1 | Monorepo tuzilmasi, `.editorconfig`, lint konfiguratsiyalari, PR shabloni, `main` himoyasi | Repo | `CLAUDE.md` |
| 0.2 | Backend skeleti: Spring Boot 3, Gradle, 7 modul paketi + `shared`, `ModularityTests`, ArchUnit, `ProblemDetail`, profil konfiguratsiyalari | `./gradlew check` yashil | `13` |
| 0.3 | Flyway `V1…V10` (to'liq sxema), Testcontainers, `ddl-auto=validate` | Migratsiya testi | `12` |
| 0.4 | `docker-compose.local.yml` (PostgreSQL, Mailpit, MinIO), Dockerfile'lar | Lokal muhit | `19` |
| 0.5 | CI: `pr.yml` (backend, frontend, vectors) | Yashil pipeline | `19` §4 |
| 0.6 | Frontend skeleti: Vite, TS strict, Tailwind + tokenlar, router, layoutlar, tema, i18n (4 lokal, `i18n:check`, transliterator) | Bo'sh sahifalar 4 tilda | `15`, `05`, `22` |
| 0.7 | **Engine (TS):** `prepare`, `reducer`, `metrics`, `keylog` codec + unit/property testlar | `engine/` 100% branch | `07` |
| 0.8 | **Engine (Java):** `KeylogDecoder`, `TargetPreparer`, `KeylogReplayer`, `MetricsCalculator` | Vektorlar ikkala tomonda | `07` §8, §11 |
| 0.9 | `engine-vectors/` — birinchi 40 vektor (har hodisa turi, indent, backspace, timeout) | Umumiy test | `07` §11 |
| 0.10 | **Vertikal kesim:** `POST /sessions` (bitta fixture snippet) → minimal `TypingEditor` → `complete` → natija | G0 | `13` §4–5 |
| 0.11 | Kontent formati, validator CLI (`content/`), 20 namunaviy snippet (har 1-reliz tilidan 4 ta), Oracle Free bilan kompilyatsiya tekshiruvi | Kontent pipeline prototipi | `06`, `03` §5 |
| 0.12 | Staging muhiti, blue-green deploy skripti, bazaviy monitoring | Staging'da vertikal kesim | `19` |

Nima uchun vertikal kesim birinchi: eng xavfli texnik taxmin — klient va server metrikasining aynan mosligi; u ikkinchi haftada tekshiriladi.

### 2.2 Bosqich 1 — MVP (3–12-hafta)

| Hafta | Backend | Frontend | Kontent / dizayn |
| --- | --- | --- | --- |
| 3–4 | `identity`: ro'yxat, login, refresh rotation, email tasdiqlash, parol tiklash, mehmon tokeni, OAuth (GitHub, Google), rate limit | Auth sahifalari, API klient (401/refresh), guard'lar, sozlamalar store | Typing ekrani va dashboard maketlari (yuqori aniqlik); snippet yozish boshlanadi: Java, SQL |
| 4–6 | `content`: tillar, mavzular, mashqlar CRUD, validatsiya (`BL-EM-1…4`), import, qamrov; admin API; audit; 2FA | Admin: mashq muharriri (`SnippetEditor`), import, qamrov; onboarding; til tanlash | PL/SQL, Python snippetlari; mavzu nomlari 4 lokalda |
| 6–9 | `training`: sessiya ochish/yakunlash, snippet tanlash, `next`, rejimlar (Practice, Speed Test, Accuracy, Special Chars + generator), shubha qoidalari, mavzu progressi, placement, mehmon claim | `TypingEditor` to'liq (`09`), kiritish adapteri (`07` §4), natija ko'rinishi, virtual klaviatura (`08`), practice va mavzu sahifalari, landing demo, offline navbat | JavaScript snippetlari; ranked pool belgilash; `SPECIAL_CHARS` shablonlari va lug'atlar; ikki bosqichli ko'rib chiqish |
| 9–11 | `analytics` (asosiy agregatlar, kunlik qatorlar, belgi statistikasi yig'ilishi), `gamification` (XP, level, rank, streak, rekordlar), dashboard endpoint'i, tarix, replay, eksport/o'chirish | Dashboard, tarix + replay, analytics (asosiy), profil, sozlamalar (7 bo'lim), buyruqlar palitrasi | 800 snippetga yetkazish; izohlar tarjimasi (uzl, ru) + uzc generatsiyasi |
| 12 | Mustahkamlash: xavfsizlik testlari, so'rovlar soni, k6 (bazaviy), migratsiya testi, monitoring/alertlar, backup mashqi | Vizual testlar (5 kenglik × 2 tema × 4 lokal), a11y, Lighthouse, perf byudjeti, prerender/SEO | Til sifatini ko'rib chiqish (ona tilida so'zlashuvchilar); huquqiy matnlar |

Feature flag'lar R1 da: `SPEED_TEST`, `PRACTICE`, `ACCURACY`, `SPECIAL_CHARS` yoqiq; `WEAKNESS`, `CHALLENGE`, `CODE_SPRINT`, `coach`, `leaderboards`, `achievements`, `challenges`, `competitions` o'chiq.

### 2.3 Bosqich 2 — Raqobat va tahlil (13–20-hafta)

| Hafta | Ish |
| --- | --- |
| 13–14 | Analytics to'liq: bigram/turkum statistikasi, chalkashliklar, zaiflik profili, heatmap, tavsiyalar; Weakness Training; Challenge; Code Sprint |
| 15–16 | Coach (qoida dvigateli): kunlik reja, maslahat, maqsad, haftalik hisobot, `TemplateNarrator`; `/coach` sahifasi; email hisobot |
| 16–18 | Achievement qoida mexanizmi + 40 achievement; challenge shablonlari va scheduler; bildirishnomalar; reytinglar (doskalar, kesh, o'rin), global ranking/DevScore; reyting sahifasi (prerender qobiq) |
| 18–19 | Asinxron musobaqalar; admin: shubhali sessiyalar navbati, mahsulot metrikalari, shikoyatlar, achievement/challenge/musobaqa boshqaruvi |
| 13–20 (parallel) | Kontent: TypeScript, C#, Go, Kotlin, C++, Bash (to'liq dastur); Java/Python/JS/SQL/PL/SQL qolgan mavzulari; PL/SQL konstruksiyalar qamrovi (`AC-PLS-4`) |
| 20 | Mustahkamlash: k6 to'liq ssenariy, ZAP, reyting anti-cheat testlari, kalibrlash (OQ-4 uchun ma'lumot) |

### 2.4 Bosqich 3 — Jonli musobaqa (21–28-hafta)

| Hafta | Ish |
| --- | --- |
| 21 | Jonli poyga spetsifikatsiyasi (**OQ-8**) — hujjat yoziladi va tasdiqlanadi |
| 22–24 | WebSocket (STOMP), xona hayot sikli, poyga UI, validatsiya REST orqali |
| 24–25 | Coach `LlmNarrator` (port, sxema tekshiruvi, byudjet, kesh, zaxira), `/coach/explain` |
| 25–26 | Ommaviy profil (prerender), follow, "kuzatayotganlarim" reytingi; mobile Lite rejim (`TOUCH`) |
| 21–27 (parallel) | Kontent: C, PHP, Rust, HTML, CSS; Expert darajalarni to'ldirish |
| 27–28 | Yuklama testi (poygalar bilan), xavfsizlik ko'rigi, `21` nazorat ro'yxati, reliz |

## 3. Implementatsiya tartibi (bog'liqliklar)

```text
shared ─► identity ─► content ─► training ─► analytics ─► gamification ─► coach ─► admin (har modul bilan parallel o'sadi)
              │                      ▲
engine (TS) ──┴── engine (Java) ─────┘        frontend: tokens → ui → auth → admin(content) → typing → practice → dashboard → analytics → …
```

Har modul uchun ichki tartib: (1) migratsiya mavjud (0.3) → (2) domen + unit testlar → (3) repository + integratsiya testlari → (4) use-case → (5) controller + API testlari → (6) hodisa tinglovchilari → (7) frontend → (8) E2E → (9) AC tekshiruvi.

**Har vazifa uchun "tayyor" ta'rifi (DoD):** kod + testlar (AC ID bilan) yashil · 4 lokal · OpenAPI va tegishli hujjat yangilangan · xato/bo'sh/yuklanish holatlari · a11y · ko'rib chiqilgan · staging'da tekshirilgan.

## 4. Xavflar

| Xavf | Ehtimol | Ta'sir | Yumshatish |
| --- | --- | --- | --- |
| Kontent hajmi ortda qoladi (800 → 4 620 snippet) | Yuqori | Reliz kechikadi | Kontent 0-bosqichdan; validator va kompilyator avtomatik; zaxira: MVP 3 til bilan (`java`, `sql`, `plsql`) |
| Klient/server engine farqi | O'rta | Noto'g'ri natijalar | Umumiy vektorlar CI'da majburiy; `engine_mismatch` alert; engine versiyalash |
| Typing unumdorligi (kuchsiz qurilma) | O'rta | Asosiy tajriba buziladi | 2-haftada perf testi; qator darajasida memo; byudjet CI'da |
| Reytingda aldash | Yuqori | Raqobat ma'nosiz | Reyting R2 da anti-cheat bilan birga; admin ko'rigi |
| 4 lokal relizni ushlaydi | O'rta | Chala tarjima | Tarjima har ekran bilan; uzc avtomatik; CI to'siq; psevdo-lokal |
| Oracle PL/SQL kontent sifati | O'rta | Ishonch yo'qoladi | Oracle Free'da kompilyatsiya; taqiqlangan konstruksiyalar; DB mutaxassisi ko'rigi |
| Qamrov kengayishi | Yuqori | Fokus yo'qoladi | Non-goals ro'yxati; yangi g'oyalar `23` §4 ga |
| LLM xarajati/ishonchliligi | Past | Coach matni sifatsiz | Deterministik yadro; LLM ixtiyoriy; byudjet chegarasi |
| Bitta server nosozligi | O'rta | Uzilish | Backup + tiklash mashqi; RTO 1 soat; `17` §6 masshtablash yo'li |

## 5. Relizdan keyingi birinchi 4 hafta

Kuzatish: aktivatsiya, D7, `REJECTED` ulushi, klavish kechikishi p95 (real foydalanuvchilar), eng ko'p tashlab ketiladigan mashqlar. Shu ma'lumot bilan: OQ-4 (rank chegaralari), OQ-7 (metrika mo'ljallari), daraja kalibrlash (`FR-EM-9`), shubha chegaralari qayta ko'riladi.
