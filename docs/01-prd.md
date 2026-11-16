# 01. Product Requirements Document (PRD)

## 1. Muammo

Mavjud typing trenajyorlar (Monkeytype, TypeRacer, Keybr) oddiy matnga mo'ljallangan. Dasturchining kundalik yozuvi boshqacha: belgilarning katta qismi `{ } ( ) [ ] ; : = < > _ .`, indentatsiya, camelCase/snake_case identifikatorlar va kalit so'zlardan iborat. Kod rejimi bor saytlar (typing.io, SpeedCoder) til bo'yicha progress, zaif belgi tahlili, o'quv tuzilmasi va SQL / Oracle PL/SQL kabi yo'nalishlarni bermaydi.

## 2. Vizyon

Dasturchi o'z tilida haqiqiy, idiomatik kod yozib mashq qiladi. Tizim uning qaysi belgi, juftlik va konstruksiyalarda sekinlashishini o'lchaydi va aynan shularga mashq, reja va tushuntirish beradi.

Mahsulot **oddiy typing test emas**: bu o'quv tuzilmasi (til → mavzu → daraja → mashq), aniq o'lchov (server tasdiqlagan metrikalar), shaxsiy murabbiy (Coach) va raqobat (reyting, musobaqa) birlashgan platforma.

## 3. Maqsadli auditoriya

| Persona | Ehtiyoj | Asosiy funksiyalar |
| --- | --- | --- |
| **Junior / talaba** | Sintaksisni qo'lga o'rgatish, klaviaturaga qaramay yozish | Practice, Special Characters, Virtual Keyboard |
| **Middle / Senior** | Tezlikni oshirish, yangi tilga o'tish | Speed Test, Weakness Training, Coach |
| **DB dasturchi** | SQL va PL/SQL kalit so'zlari, `:=`, `%TYPE`, `||` | SQL va PL/SQL yo'nalishlari |
| **Raqobatchi** | Reyting, rekord, musobaqa | Challenge, Code Sprint, Leaderboard |
| **Kontent muharriri (EDITOR)** | Mavzu va mashqlarni boshqarish | Admin: kontent |
| **Administrator (ADMIN)** | Foydalanuvchilar, shubhali sessiyalar, tizim | Admin Panel |

## 4. Mahsulot tamoyillari

1. **Kod — markazda.** Typing ekranida kod va kursordan boshqa hamma narsa ikkinchi darajali.
2. **O'lchov halol.** Natija hech qachon "yaxshilab" ko'rsatilmaydi; rasmiy metrika serverda keylog'dan hisoblanadi.
3. **Haqiqiy kod.** Har snippet idiomatik, sintaktik to'g'ri va CI'da tekshirilgan; tasodifiy belgilar to'plami emas (Special Characters rejimidagi mashqlar bundan mustasno va alohida belgilangan).
4. **Klaviaturadan chiqmasdan.** Butun mashq sikli sichqonchasiz bajariladi.
5. **Ma'lumot — serverda.** Hech bir funksiya brauzer xotirasidagi ma'lumotga tayanmaydi.
6. **Konfiguratsiya, kod emas.** Yangi dasturlash tili kod o'zgarishisiz qo'shiladi.

## 5. Qamrov

### 5.1 Relizlar

| Reliz | Mazmun |
| --- | --- |
| **R1 — MVP** | Modullar 1–3, 4–5 (SQL, PL/SQL — birinchi 8 mavzu), 6, 7 (Practice, Speed Test, Accuracy, Special Characters), 8, 9, 10, 11 (asosiy ko'rsatkichlar), 13 (XP, level, streak, rekordlar), 14, 15, 16, 17 (kontent va foydalanuvchilar), 18–21. 5 dasturlash tili. Sayt 4 tilda. |
| **R2 — Raqobat va tahlil** | 7 (Weakness, Challenge, Code Sprint), 11 (to'liq: belgi/bigram/turkum, heatmap), 12 (Coach — qoida asosidagi qism), 13 (achievement, challenge, reyting, asinxron musobaqa), 17 (shubhali sessiyalar, metrikalar). +6 til, MVP tillarida qolgan mavzular. |
| **R3 — Jonli musobaqa** | Jonli poyga (WebSocket), Coach LLM bayoni, ommaviy profil va follow, ommaviy API, mobile Lite rejim. +5 til. |

### 5.2 Qamrovdan tashqari (non-goals)

- Nazariy kurslar, video darslar, quiz.
- Kodni kompilyatsiya qilish yoki ishga tushirish (foydalanuvchi tomonida).
- LLM yordamida snippet generatsiyasi — kontent faqat inson tekshiruvidan o'tgan (Coach LLM faqat matnli izoh beradi).
- Pullik tariflar (OQ-6).
- Native mobil ilovalar.

`22 — Future Features` ro'yxati `23-decisions-open-questions.md` §4 da.

## 6. Muvaffaqiyat metrikalari

| Maqsad | Metrika | 6 oylik mo'ljal |
| --- | --- | --- |
| Aktivatsiya | Ro'yxatdan o'tgan kuni ≥ 3 mashq tugatganlar ulushi | 60% |
| Ushlab qolish | D7 / D30 retention | 25% / 10% |
| Rivojlanish | ≥ 20 sessiyali foydalanuvchilarda median WPM o'sishi | +15% |
| Jalb etish | Haftalik faol foydalanuvchining median sessiyalari | 8 / hafta |
| Ishonchlilik | `REJECTED` sessiyalar ulushi | < 1% |
| Kontent sifati | Foydalanuvchi shikoyati tushgan snippetlar ulushi | < 0.5% |

> **OPEN QUESTION (OQ-7):** mo'ljal qiymatlari boshlang'ich taxmin. Birinchi 4 hafta ma'lumotidan keyin qayta belgilanadi.

O'lchash: `admin.product_daily_metrics` (`12-database.md`), hisoblash qoidalari `10-analytics-ai-coach.md` §A.7.

## 7. Nofunksional talablar

| ID | Talab | Qiymat | Tekshirish |
| --- | --- | --- | --- |
| NFR-1 | Klavish → ekran kechikishi | ≤ 16 ms p95 | Playwright + Performance API (`17`) |
| NFR-2 | API javob vaqti | p95 ≤ 200 ms; `complete` ≤ 400 ms | k6 |
| NFR-3 | Mavjudlik | 99.5% / oy | Uptime monitoring |
| NFR-4 | Yuklama | 1 000 bir vaqtdagi foydalanuvchi, 1 instans | k6 |
| NFR-5 | Brauzerlar | Chrome, Firefox, Safari, Edge — oxirgi 2 major versiya | Playwright matritsasi |
| NFR-6 | Ekran o'lchamlari | 320 px dan 3840 px (4K) gacha | Vizual testlar: 320, 768, 1280, 1920, 3840 |
| NFR-7 | Accessibility | WCAG 2.1 AA | axe-core; Lighthouse ≥ 95 |
| NFR-8 | Lighthouse (ommaviy sahifalar) | Perf ≥ 90, A11y ≥ 95, BP ≥ 95, SEO ≥ 95 | Lighthouse CI |
| NFR-9 | Interfeys tillari | `uzl`, `uzc`, `ru`, `en` — 100% qamrov | CI i18n tekshiruvi |
| NFR-10 | Ma'lumot yo'qotilishi | RPO ≤ 5 daq, RTO ≤ 1 soat | Tiklash mashqi |
| NFR-11 | Natija aniqligi | Klient va server metrikasi farqi 0 (bir xil keylog'da) | Umumiy test vektorlari |
| NFR-12 | Xavfsizlik | OWASP Top 10 bo'yicha yuqori darajali topilma yo'q | ZAP, dependency skan |
| NFR-13 | Kengayuvchanlik | Yangi dasturlash tili — backend/frontend kodi o'zgarishisiz (Shiki grammatikasi mavjud bo'lsa) | AC-PL-5 |
| NFR-14 | Kuzatuvchanlik | Har so'rovda `traceId`; metrikalar, loglar, alertlar | `19` |

## 8. Taxminlar va cheklovlar

- Jamoa: 4 kishi (2 backend, 1 frontend, 1 dizayner/kontent) — **OQ-2**.
- Hosting: bitta ilova serveri + alohida PostgreSQL; Kubernetes ishlatilmaydi.
- Kontent mualliflik huquqi: barcha snippetlar jamoa tomonidan yozilgan yoki ruxsat beruvchi litsenziyadagi (MIT/Apache-2.0/CC0) manbadan moslashtirilgan; manba `content/` YAML'ida `source` maydonida ko'rsatiladi.
- Foydalanuvchi jismoniy klaviaturaga ega deb hisoblanadi; ekran klaviaturasi — cheklangan Lite rejim (R3).

## 9. Bog'liq hujjatlar

Funksional talablar — `02`; algoritmlar — `03`; qabul mezonlari — `18`; ochiq savollar — `23`.
