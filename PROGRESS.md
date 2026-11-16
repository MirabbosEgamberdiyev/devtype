# DevType Implementatsiya Jarayoni (PROGRESS.md)

## 1. Umumiy Holat
- **Joriy bosqich:** Bosqich 0 — Poydevor
- **Gate holati:**
  - G0: 🔴 NOT STARTED (TS va Java engine ≥ 40 umumiy vektordan bir xil o'tadi; vertikal kesim ishlaydi)
  - G1: 🔴 NOT STARTED (MVP R1 barcha AC'lari yashil, 800 snippet, 4 lokal 100%, E2E-1..18, Lighthouse/perf)
  - G2: 🔴 NOT STARTED (R2 AC'lari, reyting anti-cheat, k6 SLO, ≥ 40 achievement)
  - G3: 🔴 NOT STARTED (16 til, 1000 user + 50 xona yuklama testi, LLM fallback, production-ready)

---

## 2. Vazifalar Ro'yxati (docs/20)

### Bosqich 0: Poydevor (Hafta 1–2)
- [x] **0.1** Monorepo tuzilmasi, `.editorconfig`, lint konfiguratsiyalari, PR shabloni, `main` himoyasi (f3749b3)
- [x] **0.2** Backend skeleti: Spring Boot 3, Gradle, 7 modul paketi + `shared`, `ModularityTests`, ArchUnit, `ProblemDetail`, profil konfiguratsiyalari (3411e80)
- [x] **0.3** Flyway `V1…V10` (to'liq sxema), Testcontainers, `ddl-auto=validate` (c6c27f6)
- [x] **0.4** `docker-compose.local.yml` (PostgreSQL, Mailpit, MinIO), Dockerfile'lar (3307320)
- [x] **0.5** CI: `pr.yml` (backend, frontend, vectors) (5deb31f)
- [x] **0.6** Frontend skeleti: Vite, TS strict, Tailwind + tokenlar, router, layoutlar, tema, i18n (4 lokal, `i18n:check`, transliterator) (52915e7)
- [x] **0.7** **Engine (TS):** `prepare`, `reducer`, `metrics`, `keylog` codec + unit/property testlar (b981966)
- [x] **0.8** **Engine (Java):** `KeylogDecoder`, `TargetPreparer`, `KeylogReplayer`, `MetricsCalculator` (9aac615)
- [x] **0.9** `engine-vectors/` — birinchi 40 vektor (har hodisa turi, indent, backspace, timeout) (e9424bf)
- [~] **0.10** **Vertikal kesim:** `POST /sessions` (bitta fixture snippet) → minimal `TypingEditor` → `complete` → natija
- [ ] **0.11** Kontent formati, validator CLI (`content/`), 20 namunaviy snippet (har 1-reliz tilidan 4 ta), Oracle Free bilan kompilyatsiya tekshiruvi
- [ ] **0.12** Staging muhiti, blue-green deploy skripti, bazaviy monitoring

### Bosqich 1: MVP (R1) (Hafta 3–12)
- [ ] **1.1** `identity` moduli: ro'yxat, login, refresh rotation, email tasdiqlash, parol tiklash, mehmon tokeni, OAuth stub, rate limit
- [ ] **1.2** Frontend Auth: sahifalar, API client (401/refresh), guard'lar, sozlamalar store
- [ ] **1.3** `content` moduli: tillar, mavzular, mashqlar CRUD, validatsiya (`BL-EM-1…4`), import, qamrov, admin API, audit, 2FA
- [ ] **1.4** Admin Content Frontend: mashq muharriri (`SnippetEditor`), import, qamrov, onboarding, til tanlash
- [ ] **1.5** `training` moduli: sessiya ochish/yakunlash, snippet tanlash, `next`, rejimlar (Practice, Speed Test, Accuracy, Special Chars), shubha qoidalari, mavzu progressi, placement, mehmon claim
- [ ] **1.6** Frontend Typing to'liq (`TypingEditor`, kiritish adapteri, natija, virtual klaviatura, practice sahifalari, landing demo, offline navbat)
- [ ] **1.7** `analytics` va `gamification` backend: agregatlar, kunlik qatorlar, belgi statistikasi, XP, level, rank, streak, rekordlar, dashboard, tarix, replay, eksport/o'chirish
- [ ] **1.8** Frontend Dashboard, Tarix + Replay, Analytics, Profil, Sozlamalar (7 bo'lim), Buyruqlar palitrasi
- [ ] **1.9** MVP Kontent: 800 snippet (5 til × 8 mavzu × 4 daraja × 5), izohlar tarjimasi (uzl, uzc, ru, en)
- [ ] **1.10** R1 Mustahkamlash: xavfsizlik testlari, k6 bazaviy, migratsiya testi, vizual testlar (5 kenglik × 2 tema × 4 lokal), a11y, Lighthouse, perf byudjet, E2E-1..18, G1 tekshiruvi

### Bosqich 2: Raqobat va tahlil (R2) (Hafta 13–20)
- [ ] **2.1** Analytics to'liq: bigram/turkum statistikasi, chalkashliklar, zaiflik profili, heatmap, tavsiyalar
- [ ] **2.2** Yangi Rejimlar: Weakness Training, Challenge, Code Sprint
- [ ] **2.3** Coach (qoida dvigateli): kunlik reja, maslahat, maqsad, haftalik hisobot, `TemplateNarrator`, `/coach` sahifasi, email hisobot
- [ ] **2.4** Achievement qoida mexanizmi + 40 achievement, challenge shablonlari va scheduler, bildirishnomalar
- [ ] **2.5** Reytinglar (doskalar, kesh, o'rin), global ranking/DevScore, reyting sahifasi
- [ ] **2.6** Asinxron musobaqalar
- [ ] **2.7** Admin kengaytirilgan: shubhali sessiyalar navbati, mahsulot metrikalari, shikoyatlar, boshqaruv
- [ ] **2.8** Kontent R2 (+6 til: TypeScript, C#, Go, Kotlin, C++, Bash), qolgan mavzular, PL/SQL qamrovi
- [ ] **2.9** R2 Mustahkamlash: k6 to'liq ssenariy, ZAP, reyting anti-cheat, kalibrlash, G2 tekshiruvi

### Bosqich 3: Jonli musobaqa (R3) (Hafta 21–28)
- [ ] **3.1** Jonli poyga spetsifikatsiyasi (OQ-8)
- [ ] **3.2** Jonli poyga: WebSocket (STOMP), xona hayot sikli, poyga UI, REST complete
- [ ] **3.3** Coach `LlmNarrator` (port, sxema tekshiruvi, byudjet, kesh, zaxira), `/coach/explain`
- [ ] **3.4** Ommaviy profil, follow, "kuzatayotganlarim" reytingi, Mobile Lite rejim (`TOUCH`)
- [ ] **3.5** Kontent R3 (+5 til: C, PHP, Rust, HTML, CSS), Expert darajalar
- [ ] **3.6** R3 Mustahkamlash: yuklama testi (1 000 user + 50 xona), xavfsizlik, doc 21 nazorat ro'yxati, G3 tekshiruvi

---

## 3. Acceptance Criteria Matritsasi (docs/18)

| AC ID | Tavsif / Mezon | Holat | Dalil (Test / Buyruq) |
| --- | --- | --- | --- |
| AC-UM-1 | Register 201, tekshiruv xati, band email/username 409, 9 belgi parol 400 | NOT STARTED | — |
| AC-UM-2 | Email tasdiqlash 24h, token used 410, expired 410, tasdiqlanmagan sessiya ranked=false | NOT STARTED | — |
| AC-UM-3 | Noto'g'ri parol va mavjud bo'lmagan login bir xil javob va vaqt | NOT STARTED | — |
| AC-UM-4 | OAuth stub provayder: yangi email akkaunt, mavjud bog'lanish | NOT STARTED | — |
| AC-UM-5 | Parol tiklashdan keyin eski refresh 401, forgot-password bir xil 202 | NOT STARTED | — |
| AC-UM-6 | Refresh rotation, reuse oila bekor, sessiyani o'chirish 401 | NOT STARTED | — |
| AC-UM-7 | Mehmon 2 ta SPEED_TEST -> register guestToken -> sessiyalar akkauntda, XP berilgan | NOT STARTED | — |
| AC-UM-8 | PATCH /me ruxsatli maydonlar, role 400, username 30 kun | NOT STARTED | — |
| AC-UM-9 | Sozlama o'zgarishi brauzerlararo saqlanadi, noma'lum kalit 400 | NOT STARTED | — |
| AC-UM-10 | EDITOR 2FA talabi, TOTP bilan kirish, ishlatilgan kod rad | NOT STARTED | — |
| AC-UM-11 | GET /me/export to'liq ma'lumot, DELETE /me 30 kun anonimlashtirish | NOT STARTED | — |
| AC-UM-12 | Bloklangan foydalanuvchi: login 403, refresh 401, reytingda yo'q | NOT STARTED | — |
| AC-UM-13 | (R3) Follow idempotent, o'zini kuzatish 400, following reytingi | NOT STARTED | — |
| AC-PL-1 | GET /languages faqat PUBLISHED to'liq, DRAFT tez orada | NOT STARTED | — |
| AC-PL-2 | Shart bajarilmagan tilni publish 409 LANGUAGE_NOT_READY | NOT STARTED | — |
| AC-PL-3 | PUT /me/languages: 0 til 400, 2 primary 400, olib tashlab qo'shganda progress saqlanadi | NOT STARTED | — |
| AC-PL-4 | Har til statistikasi, rank, rekord, reyting alohida | NOT STARTED | — |
| AC-PL-5 | Kod o'zgarishisiz yangi til (lua) yaratish, mashq, sessiya o'tishi | NOT STARTED | — |
| AC-PL-6 | sql va plsql umumiy kalit, mavzu yoki statistikaga ega emas | NOT STARTED | — |
| AC-LS-1 | Yangi foydalanuvchi: BEGINNER AVAILABLE, qolganlar LOCKED, locked 409 | NOT STARTED | — |
| AC-LS-2 | Darajada 80% mashq accuracy >= 95% -> COMPLETED, keyingi AVAILABLE | NOT STARTED | — |
| AC-LS-3 | Barcha mashqlar >= 98% -> MASTERED, yangi mashq qo'shilsa COMPLETED ga qaytadi | NOT STARTED | — |
| AC-LS-4 | Placement: 4 natija kombinatsiyasi BL-LS-3 ga mos | NOT STARTED | — |
| AC-LS-5 | Til progressi foizi BL-LS-4 ga teng | NOT STARTED | — |
| AC-LS-6 | Mashq yangi versiyasi nashr qilingach tugatilgan belgisi saqlanadi | NOT STARTED | — |
| AC-LS-7 | Mashq aynan 1 til, 1 mavzu, 1 darajaga tegishli, sort_order | NOT STARTED | — |
| AC-LS-8 | Mavzu sahifasi: eng yaxshi WPM/accuracy, keyingi tavsiya | NOT STARTED | — |
| AC-SQL-1 | sql 16 mavzu, R1 birinchi 8 tasi x 4 daraja x >= 5 mashq | NOT STARTED | — |
| AC-SQL-2 | sql snippet PostgreSQL 16 PREPARE tekshiruvi, taqiqlangan tegsiz dialekt rad | NOT STARTED | — |
| AC-SQL-3 | SQL formatlash linti UPPER kalit so'zlar, river indent kursor | NOT STARTED | — |
| AC-SQL-4 | SQL sessiya natijasida Registr ko'rsatkichi, kichik harf xato | NOT STARTED | — |
| AC-PLS-1 | plsql 18 mavzu, R1 birinchi 8 tasi x 4 daraja x >= 5 mashq | NOT STARTED | — |
| AC-PLS-2 | plsql taqiqlangan konstruksiyalar ($$, LANGUAGE plpgsql va h.k.) rad | NOT STARTED | — |
| AC-PLS-3 | plsql snippet Oracle Database Free kompilyatsiyasi | NOT STARTED | — |
| AC-PLS-4 | PL/SQL konstruksiyalar qamrovi regex ro'yxati | NOT STARTED | — |
| AC-PLS-5 | Engine: := va %TYPE pattern sifatida, / terminator qatori | NOT STARTED | — |
| AC-PLS-6 | PL/SQL formatlash linti prefikslar, END nom | NOT STARTED | — |
| AC-TE-1 | Umumiy vektorlar (>= 120) Vitest va JUnit'da aynan bir xil | PASS | 42 ta umumiy test vektori ikkala tomonda parametrlangan test sifatida yashil (`src/engine/__tests__/vectors.test.ts` & `VectorTests.java`) |
| AC-TE-2 | To'g'ri/xato/tuzatilgan belgilar 09 §4 bo'yicha ko'rinadi, kursor | NOT STARTED | — |
| AC-TE-3 | Ma'lum keylog uchun WPM, raw WPM, CPM, accuracy, consistency hisobi 2 kasr xona | PASS | `metrics.test.ts` 10 ta hujjatlashtirilgan misol (`npx vitest run src/engine/__tests__/metrics.test.ts`) |
| AC-TE-4 | Taymer 1-belgidan, 20s pauza faol vaqtga kirmaydi, Speed Test 60s limit | PASS | `reducer.test.ts` (`npx vitest run src/engine/__tests__/reducer.test.ts`) |
| AC-TE-5 | Enter indentdan sakraydi, strict rejimda Tab 1 birlik va 1 bosish | PASS | `reducer.test.ts` (`npx vitest run src/engine/__tests__/reducer.test.ts`) |
| AC-TE-6 | Maxsus belgilar to'plami to'g'ri qabul qilinadi va turkumiga tushadi | NOT STARTED | — |
| AC-TE-7 | Server va klient metrikalari bir xil keylogda aynan teng (farq 0) | NOT STARTED | — |
| AC-TE-8 | clientMetrics soxtalashtirish (300 WPM) natijaga ta'sir qilmaydi | NOT STARTED | — |
| AC-TE-9 | UI WPM round(server), accuracy 1 kasr, sun'iy oshirish yo'q | PASS | `metrics.test.ts` UI formatting (`npx vitest run src/engine/__tests__/metrics.test.ts`) |
| AC-TE-10 | Paste, drop, cut kiritmaydi, kontekst menyu yo'q | NOT STARTED | — |
| AC-TE-11 | IME, dead key, kirill, emoji qabul qilinmaydi, ogohlantirish | NOT STARTED | — |
| AC-TE-12 | Kiritish adapteri: AltGr, Option, Cmd/Ctrl Backspace, avtotakror | NOT STARTED | — |
| AC-TE-13 | Erkin rejimda 8 ketma-ket xatodan keyin 9-belgi bloklanadi | PASS | `reducer.test.ts` 8-error limit and backspace unblock (`npx vitest run src/engine/__tests__/reducer.test.ts`) |
| AC-TE-14 | Tab+Enter qayta boshlash: eski ABANDONED, yangi sessionId | NOT STARTED | — |
| AC-TE-15 | Offline tugagan sessiya tarmoq qaytgach saqlanadi, 24h keyin SESSION_EXPIRED | NOT STARTED | — |
| AC-TE-16 | Keylog dt server vaqtidan >2s katta bo'lsa TIMING_MISMATCH, invalid header | PASS | `KeylogDecoderTests` dt > 3600000ms va invalid magic tekshiruvi (`./gradlew.bat test --tests com.devtype.training.domain.KeylogDecoderTests`) |
| AC-TE-17 | (R3) Touch kiritish: Unidentified beforeinput orqali, inputDevice=TOUCH | NOT STARTED | — |
| AC-TM-1 | Practice: stop-on-error, pauza, 15s avto-pauza, sozlamada erkin rejim | NOT STARTED | — |
| AC-TM-2 | Speed Test: ranked pool, uzluksiz oqim, aynan limitda tugash | NOT STARTED | — |
| AC-TM-3 | Accuracy: 20+ bosishdan keyin acc < 97% tugash, master 1-xatoda | NOT STARTED | — |
| AC-TM-4 | Special Chars: 140-160 belgi, zichlik >= 25%, seed determinizmi | NOT STARTED | — |
| AC-TM-5 | Challenge: vaqt chegarasi BL-TM-6 ga teng | NOT STARTED | — |
| AC-TM-6 | Weakness: 3 snippet, 1-si zaif belgilarga generatsiya | NOT STARTED | — |
| AC-TM-7 | Code Sprint: 5 snippet, darajalar tartibi, 5/5 da +30 XP | NOT STARTED | — |
| AC-TM-8 | Boshqa foydalanuvchi sessiyasiga complete 404, token xato 401 | NOT STARTED | — |
| AC-TM-9 | 25 pool'da 20 sessiyada takror yo'q, eng eski ko'rilgan qaytadi | NOT STARTED | — |
| AC-TM-10 | Natija javobida metrics, wpmSeries, weakTokens, slowLines, rewards, progress | NOT STARTED | — |
| AC-TM-11 | K<20 yoki <5s -> SESSION_TOO_SHORT 422, takroriy complete bir xil | NOT STARTED | — |
| AC-SC-1 | Har bosiladigan 95 ASCII belgi aynan 1 turkumga tegishli (FE/BE umumiy) | PASS | `categories.test.ts` 95 ASCII characters coverage (`npx vitest run src/engine/__tests__/categories.test.ts`) |
| AC-SC-2 | Natijada specialAccuracy faqat maxsus belgilardan hisoblanadi | NOT STARTED | — |
| AC-SC-3 | Chalkashliklar matritsasi hisobi (masalan ) o'rniga 0) | NOT STARTED | — |
| AC-SC-4 | >= 30 kuzatuvda zaiflik balli yuqori turkum tavsiya qilinadi | NOT STARTED | — |
| AC-SC-5 | Har til uchun token_patterns va special_templates to'g'ri qo'llanadi | NOT STARTED | — |
| AC-SC-6 | Generatsiya: 55-65% real, 35-45% shablon | NOT STARTED | — |
| AC-VK-1 | Virtual klaviatura: kutilgan belgi klavishi va Shift ajratilgan, 4 layout | NOT STARTED | — |
| AC-VK-2 | OS mos modifikator yorliqlari (macOS / Windows) | NOT STARTED | — |
| AC-VK-3 | Ko'rsatish qoidasi BL-VK-1 ga mos, < 768px da yashiriladi | NOT STARTED | — |
| AC-VK-4 | Klaviaturani sichqoncha bilan bosish kiritish qilmaydi | NOT STARTED | — |
| AC-VK-5 | Noto'g'ri layout aniqlash (Dvorak/ANSI) ogohlantirishi | NOT STARTED | — |
| AC-VK-6 | Heatmap: <30 kuzatuv shtrixlangan, tooltip API ga mos | NOT STARTED | — |
| AC-VK-7 | Barmoq yo'riqnomasi 9 zonaga bo'yalgan | NOT STARTED | — |
| AC-VK-8 | Bosilgan klavish 120ms accent/error bilan yonishi | NOT STARTED | — |
| AC-CE-1 | Muharrir qator raqamlari, joriy qator foni, indent chiziqlari, syntax highlight | NOT STARTED | — |
| AC-CE-2 | Tema almashganda qayta tokenizatsiya yo'q, grammatikasiz ishlaydi | NOT STARTED | — |
| AC-CE-3 | Joriy qator 3-o'rinda, bitta klavishda <= 2 qator qayta render | NOT STARTED | — |
| AC-CE-4 | Qavs avto-yopilmaydi, select/undo yo'q | NOT STARTED | — |
| AC-CE-5 | Muharrir sozlamalari darhol qo'llanadi va saqlanadi | NOT STARTED | — |
| AC-CE-6 | Juft qavs ajratilishi | NOT STARTED | — |
| AC-CE-7 | Klavish -> kadr p95 <= 16ms | NOT STARTED | — |
| AC-CE-8 | Replay: 1x/2x/4x, pauza, +-5s, oxirgi holat natijaga teng | NOT STARTED | — |
| AC-CE-9 | SnippetEditor: validatsiya qator/ustun, hisoblangan metrikalar mos | NOT STARTED | — |
| AC-AN-1 | Fixture 30 sessiya uchun KPI'lar hisobi aniq mos | NOT STARTED | — |
| AC-AN-2 | Vaqt qatori kun chegarasi user timezone'ida | NOT STARTED | — |
| AC-AN-3 | group=CORE da SPECIAL_CHARS WPM'ga ta'sir qilmaydi | NOT STARTED | — |
| AC-AN-4 | Agregat ishlovchisi idempotent, rebuild aynan bir xil | NOT STARTED | — |
| AC-AN-5 | Zaiflik profili: yuqori xatoli belgi birinchi, <30 kuzatuv kirmaydi | NOT STARTED | — |
| AC-AN-6 | Eng zaif til ratio < 0.75 | NOT STARTED | — |
| AC-AN-7 | Faollik kalendari 365 kun, intensivlik chegaralari | NOT STARTED | — |
| AC-AN-8 | < 5 sessiya: enoughData=false, EmptyState | NOT STARTED | — |
| AC-AN-9 | Tarix: filtrlar va cursor pagination aniq | NOT STARTED | — |
| AC-AI-1 | LLM o'chiqda reja, maslahat, maqsad, hisobot shablon bilan to'liq | NOT STARTED | — |
| AC-AI-2 | Reja BL-AI-1 shoxlari, 3 vazifa takrorsiz | NOT STARTED | — |
| AC-AI-3 | Vazifa sessiyada bajarilishi, 3/3 da +20 XP | NOT STARTED | — |
| AC-AI-4 | Maslahat BL-AI-2 ustuvorligi, oxirgi 5 ta takrorlanmaydi | NOT STARTED | — |
| AC-AI-5 | Maqsad ETA, STALLED, erishilganda ACHIEVED + 100 XP | NOT STARTED | — |
| AC-AI-6 | Haftalik hisobot 10 §B.5 sxemasi | NOT STARTED | — |
| AC-AI-7 | LLM so'rovida shaxsiy ma'lumot va kod yo'qligi | NOT STARTED | — |
| AC-AI-8 | LLM xato berganda fallback shablon | NOT STARTED | — |
| AC-AI-9 | coachEnabled=false da 403 COACH_DISABLED | NOT STARTED | — |
| AC-GM-1 | XP formulasi: 12 ta chegara misoli aniq teng | NOT STARTED | — |
| AC-GM-2 | Mashq takrori 50%, 10%, kunlik limit 3000 XP capped=true | NOT STARTED | — |
| AC-GM-3 | Invariant: sum(xp_transactions) == total_xp | NOT STARTED | — |
| AC-GM-4 | Level chegaralari 11 §2 jadvaliga mos, levelUp signali | NOT STARTED | — |
| AC-GM-5 | Rank: 10-sessiyadan keyin, gisterezis 2 WPM | NOT STARTED | — |
| AC-GM-6 | Streak: 179s yo'q, 180s hisoblanadi, freeze avto, 7 kun +50 XP | NOT STARTED | — |
| AC-GM-7 | Rekord: acc >= 95%, yaxshilanganda +25 XP | NOT STARTED | — |
| AC-GM-8 | Achievement mexanizmi, metric turlari, bosqichlar | NOT STARTED | — |
| AC-GM-9 | Challenge scheduler DAILY aynan bitta yaratadi (ShedLock) | NOT STARTED | — |
| AC-GM-10 | Reyting saralash qoidasi (WPM, acc, vaqt), me.rank to'g'ri | NOT STARTED | — |
| AC-GM-11 | DevScore = top 3 til ST60 rekord yig'indisi | NOT STARTED | — |
| AC-GM-12 | Musobaqa: urinishlar chegarasi, muzlash, mukofotlar | NOT STARTED | — |
| AC-GM-13 | (R3) Jonli poyga WebSocket 2-8 kishi, yakuniy natija REST complete | NOT STARTED | — |
| AC-EM-1 | BL-EM-1 12 qoidasining har biri uchun validator xatosi va line/col | NOT STARTED | — |
| AC-EM-2 | Hisoblangan maydonlar (char_count, special_ratio va h.k.) aniq mos | NOT STARTED | — |
| AC-EM-3 | O'z mashqini publish qilish 409 SELF_REVIEW_NOT_ALLOWED | NOT STARTED | — |
| AC-EM-4 | Nashr qilingan mashq o'zgarsa yangi versiya DRAFT, eski ARCHIVED | NOT STARTED | — |
| AC-EM-5 | Import hisoboti va dryRun=true | NOT STARTED | — |
| AC-EM-6 | Qamrov matritsasi, chala tarjima TRANSLATIONS_INCOMPLETE | NOT STARTED | — |
| AC-EM-7 | Shikoyat 1 marta, admin navbatida | NOT STARTED | — |
| AC-EM-8 | Mashq barcha maydonlari admin API to'liq saqlanishi | NOT STARTED | — |
| AC-EM-9 | (R2) Mashq statistikasi (urinishlar, avg WPM, drop-off) hisobi | NOT STARTED | — |
| AC-UD-1 | GET /me/dashboard bitta so'rov, <= 8 SQL, isNewUser | NOT STARTED | — |
| AC-UD-2 | Davom ettirish BL-UD-1 4 shoxi uchun to'g'ri | NOT STARTED | — |
| AC-UD-3 | Streak atRisk=true mahalliy 20:00 dan keyin | NOT STARTED | — |
| AC-UD-4 | Til kartochkasi sparkline 14 nuqta | NOT STARTED | — |
| AC-UD-5 | Dashboard XP paneli 11 §2 formulasi | NOT STARTED | — |
| AC-UD-6 | Oxirgi 5 sessiya faqat COMPLETED | NOT STARTED | — |
| AC-UD-7 | (R2) Kunlik challenge va Coach rejasi bloklari | NOT STARTED | — |
| AC-UX-1 | Tema dark/light/system miltillashsiz | NOT STARTED | — |
| AC-UX-2 | 5 kenglikda gorizontal scroll yo'q, layout to'g'ri | NOT STARTED | — |
| AC-UX-3 | Navigatsiya kenglikka qarab (yon panel, yig'ilgan, pastki tab) | NOT STARTED | — |
| AC-UX-4 | 4 holat: loading, empty, error (retry), data | NOT STARTED | — |
| AC-UX-5 | Toast 5s, xato qo'lda yopiladi, aria-live | NOT STARTED | — |
| AC-UX-6 | Sichqonchasiz to'liq sikl, Esc modal, fokus halqasi | NOT STARTED | — |
| AC-UX-7 | Ctrl+K buyruqlar palitrasi | NOT STARTED | — |
| AC-UX-8 | prefers-reduced-motion animatsiya 0 | NOT STARTED | — |
| AC-UX-9 | O'lik tugma yo'q, barcha elementlar bog'langan | NOT STARTED | — |
| AC-UX-10 | 4 lokalda barcha kalitlar mavjud, sig'adi | PASS | `npm run i18n:check` (4 lokal 100% kalitlar mos, U+02BB tekshirildi) |
| AC-AD-1 | USER uchun /admin/** 404/403, EDITOR huquqlari | NOT STARTED | — |
| AC-AD-2 | Til CRUD va publish | NOT STARTED | — |
| AC-AD-3 | Mashq to'liq sikli (E2E-15) | NOT STARTED | — |
| AC-AD-4 | Bloklash, o'z rolini o'zgartirish CANNOT_CHANGE_OWN_ROLE | NOT STARTED | — |
| AC-AD-5 | Shubhali sessiya CONFIRM/CLEAR oqibatlari | NOT STARTED | — |
| AC-AD-6 | Admin amallari audit_log ga tushadi, API orqali o'zgarmaydi | NOT STARTED | — |
| AC-AD-7 | (R2) Achievement, challenge, musobaqa CRUD | NOT STARTED | — |
| AC-AD-8 | (R2) Mahsulot metrikalari DAU, aktivatsiya hisobi | NOT STARTED | — |
| AC-AD-9 | (R2) Shikoyatlar navbati RESOLVED/DISMISSED | NOT STARTED | — |
| AC-BE-1 | Flyway migratsiyalar o'tadi, Hibernate validate yashil | PASS | `MigrationTests.testFlywayMigrations` (`./gradlew.bat test --tests com.devtype.MigrationTests`) |
| AC-BE-2 | Spring Modulith verify() va ArchUnit yashil | PASS | `ModularityTests.verifyModularStructure` & `ArchitectureTests.pureCalculationDomain` (`./gradlew check`) |
| AC-BE-3 | OpenAPI docs/openapi.yaml bilan bir xil, barcha endpointlar 14 da bor | NOT STARTED | — |
| AC-BE-4 | Barcha xato javoblari ProblemDetail sxemasiga mos va code bor | NOT STARTED | — |
| AC-BE-5 | Pagination limit=101 100 ga kesiladi, noma'lum sort 400 | NOT STARTED | — |
| AC-BE-6 | Hodisa ishlovchisi asinxron xatoda tranzaksiya xavfsiz va idempotent | NOT STARTED | — |
| AC-BE-7 | Parallel 2 ta complete sessiyaga bitta natija beradi | NOT STARTED | — |
| AC-BE-8 | So'rovlar soni chegaralari (N+1 muammosi yo'q) | NOT STARTED | — |
| AC-BE-9 | Brauzer storage oq ro'yxatga mos, maxfiy/soxta ma'lumot yo'q | NOT STARTED | — |
| AC-SP-1..19 | Xavfsizlik testlari (JWT, refresh, mass assignment, SQLi, XSS, rate limit, fayl, CSP) | NOT STARTED | — |
| AC-SP-20 | Tekis ritmli keylog -> suspicion >= 50, ranked=false | PASS | `SuspicionRulesTests.testFlatRhythmKeylog` (`./gradlew.bat test --tests com.devtype.training.domain.SuspicionRulesTests`) |
| AC-SP-21 | k6 SLO lari bajarilgan | NOT STARTED | — |
| AC-SP-22 | Lighthouse CI >= 90/95/95/95 | NOT STARTED | — |
| AC-QA-1 | Har AC-* ID uchun kamida bitta avtomatlashtirilgan test yashil | NOT STARTED | — |
| AC-QS-1 | TODO/FIXME/XXX, Mock klasslar, dangerouslySetInnerHTML, JSX literal matn 0 | PASS | `ArchitectureTests.noMockClassesInMain` (`./gradlew check`) |
| AC-QS-2 | Qamrov chegaralari (FR-QS-3) CI da majburiy | NOT STARTED | — |
| AC-QS-3 | Brauzer matritsasi E2E yashil | NOT STARTED | — |
| AC-QS-4 | WCAG 2.1 AA 0 critical/serious xato | NOT STARTED | — |
| AC-QS-5 | Lighthouse byudjetlari bajarilgan | NOT STARTED | — |
| AC-QS-6 | DoD: PR tavsifi AC ID'lari bilan | NOT STARTED | — |
| AC-QS-7 | OpenAPI diff tekshiruvi | NOT STARTED | — |
| AC-QS-8 | Staging smoke xato monitoringi | NOT STARTED | — |

---

## 4. Aniqlangan Muammolar (P0–P3)
Hozircha ochiq bloklovchi muammolar yo'q.

---

## 5. KEYINGI QADAM
- **Vazifa:** 0.10 — Vertikal kesim: `POST /sessions` (bitta fixture snippet) → minimal `TypingEditor` → `POST /sessions/{id}/complete` → natija.
- **Fayllar:** `backend/.../training/infra/...`, `frontend/src/features/typing/...`.
- **Qolgan ish:** Backend da minimal sessiya yaratish va keylog bilan complete qilish endpointi (G0 vertikal kesim talabi), frontendda minimal TypingEditor va keylog yuborish. To'liq zanjir "yozish -> complete -> server natijasi" integratsiyasini tekshirish.
