# 02. Functional Requirements — 23 modul

Har talab ID'ga ega (`FR-<MOD>-<n>`); qabul mezoni `18-testing-acceptance.md` da shu raqam bilan (`AC-<MOD>-<n>`) yoki guruh bo'lib keladi. Algoritmlar — `03-business-logic.md`. Konstantalar — `00-conventions.md` §4. "R1/R2/R3" — reliz.

---

## 1. User Management (`UM`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-UM-1 | Email + parol bilan ro'yxatdan o'tish. Maydonlar: email, username (3–20 belgi, `[a-zA-Z0-9_]`, harf bilan boshlanadi, registrdan mustaqil unikal), parol (10–128). Zaxiralangan username'lar (`admin`, `api`, `me`, `settings`, lokal kodlari va h.k.) rad etiladi. | R1 |
| FR-UM-2 | Email tasdiqlash: havola 24 soat amal qiladi; qayta yuborish 60 soniyada bir marta. Tasdiqlanmagan akkaunt mashq qila oladi, lekin reytingga kirmaydi va ommaviy profilga ega emas. | R1 |
| FR-UM-3 | Login email yoki username + parol bilan. Muvaffaqiyatsiz urinishda javob umumiy ("email yoki parol noto'g'ri") — akkaunt mavjudligi oshkor qilinmaydi. | R1 |
| FR-UM-4 | OAuth2: GitHub va Google. Provayder emaili tasdiqlangan va mavjud akkauntga mos kelsa — bog'lanadi; aks holda yangi akkaunt (username provayder login'idan, band bo'lsa raqamli suffiks). | R1 |
| FR-UM-5 | Parolni tiklash: email → 1 soatlik bir martalik havola → yangi parol → barcha refresh tokenlar bekor qilinadi. So'rovga javob har doim bir xil (email bor-yo'qligi oshkor qilinmaydi). | R1 |
| FR-UM-6 | Sessiya boshqaruvi: access JWT + aylanadigan refresh token; "barcha qurilmalardan chiqish"; faol kirishlar ro'yxati (qurilma, oxirgi faollik) va alohida bekor qilish. | R1 |
| FR-UM-7 | Mehmon rejimi: ro'yxatsiz `SPEED_TEST` va landing demo. Mehmon sessiyalari serverda `guest_id` bilan 7 kun saqlanadi; ro'yxatdan o'tganda `claim` orqali akkauntga ko'chadi (eng ko'pi 50 ta, eng yangilari). | R1 |
| FR-UM-8 | Profil: username, ko'rsatiladigan ism (ixtiyoriy, ≤ 40), avatar, mamlakat (ISO 3166-1 alpha-2), bio (≤ 160), timezone (IANA), lokal, ochiq/yopiq. | R1 |
| FR-UM-9 | Sozlamalar serverda saqlanadi (ro'yxat: `05` §5.17), barcha qurilmalarda bir xil. | R1 |
| FR-UM-10 | Rollar: `USER`, `EDITOR`, `ADMIN`. `EDITOR` va `ADMIN` uchun TOTP 2FA majburiy — yoqilmaguncha admin endpoint'lari `403 TWO_FACTOR_REQUIRED`. | R1 |
| FR-UM-11 | Ma'lumotni eksport qilish (JSON) va akkauntni o'chirish (30 kunlik muhlat, shu davrda login qilinsa bekor bo'ladi). | R1 |
| FR-UM-12 | Bloklangan foydalanuvchi login qila olmaydi; faol tokenlari bekor qilinadi; reyting yozuvlari yashiriladi. | R1 |
| FR-UM-13 | Follow: foydalanuvchi boshqa ochiq profilni kuzatadi / bekor qiladi; reytingda "kuzatayotganlarim" ko'rinishi. | R3 |

Xato holatlari: band email → `409 EMAIL_TAKEN`; band username → `409 USERNAME_TAKEN`; muddati o'tgan token → `410 TOKEN_EXPIRED`; ishlatilgan token → `410 TOKEN_USED`.

## 2. Programming Languages (`PL`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-PL-1 | Tizim 16 dasturlash tilini qo'llaydi (`00` §4.1). Har til — `content.languages` yozuvi: slug, nom, grammar ID, indent uslubi va o'lchami, maxsus belgilar to'plami, token pattern'lar, kalit so'z registri, holat. | R1–R3 |
| FR-PL-2 | Til holati: `DRAFT` (faqat admin ko'radi) → `PUBLISHED` → `ARCHIVED` (yangi sessiya ochilmaydi, eski statistika ko'rinadi). | R1 |
| FR-PL-3 | Tilni nashr qilish sharti: kamida 8 mavzu, har mavzuda 4 darajaning har birida ≥ 5 nashr qilingan mashq, ranked pool'da ≥ 30 snippet. Shart bajarilmasa `409 LANGUAGE_NOT_READY` + yetishmayotganlar ro'yxati. | R1 |
| FR-PL-4 | Foydalanuvchi 1 yoki undan ko'p til tanlaydi; aynan bittasi "asosiy". Olib tashlangan til progressi va statistikasi saqlanadi. Oxirgi tilni olib tashlab bo'lmaydi. | R1 |
| FR-PL-5 | Yangi til admin paneldan qo'shiladi va nashr qilingach katalog, practice, statistika, reytingda avtomatik paydo bo'ladi — kod o'zgarishisiz (NFR-13). | R1 |
| FR-PL-6 | Har til uchun alohida: progress, statistika, rank, rekordlar, reyting doskalari. | R1 |
| FR-PL-7 | SQL (`sql`) va PL/SQL (`plsql`) — ikki mustaqil til yozuvi; ular orasida mashq, progress yoki statistika umumiy emas. | R1 |

## 3. Language Learning Structure (`LS`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-LS-1 | Ierarxiya: Til → Mavzu (tartiblangan) → Daraja (4 ta) → Mashq. To'liq dastur `06-curriculum.md`. | R1 |
| FR-LS-2 | Daraja holatlari: `LOCKED`, `AVAILABLE`, `IN_PROGRESS`, `COMPLETED`, `MASTERED`. O'tish qoidalari `BL-LS-1`. | R1 |
| FR-LS-3 | Mavzular orasida qulf yo'q: har mavzuning `BEGINNER` darajasi doim `AVAILABLE`. Qulf faqat bir mavzu ichida darajalar orasida. | R1 |
| FR-LS-4 | Placement test (onboarding yoki til qo'shganda, ixtiyoriy): natijaga ko'ra shu tilning barcha mavzularida boshlang'ich daraja belgilanadi (`BL-LS-3`). | R1 |
| FR-LS-5 | Mavzu sahifasi: daraja tablari; har mashq uchun eng yaxshi WPM, accuracy, tugatilgan belgisi; "keyingi tavsiya etilgan mashq". | R1 |
| FR-LS-6 | Til progressi foizi = tugatilgan (mavzu × daraja) juftliklari / jami juftliklar. | R1 |
| FR-LS-7 | Mashq yangi versiyaga o'tsa, foydalanuvchining eski versiyadagi "tugatilgan" belgisi saqlanadi (`exercise_key` bo'yicha). | R1 |

## 4. SQL Training (`SQL`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-SQL-1 | SQL — mustaqil yo'nalish, 16 mavzu (`06` §3): SELECT … Execution Plans, Query Optimization. | R1 (8 mavzu), R2 (qolgani) |
| FR-SQL-2 | Dialekt: ANSI SQL. Dialektga xos snippet `oracle` yoki `postgres` tegi bilan; tegsiz snippet ikkala SUBD'da ham sintaktik to'g'ri. | R1 |
| FR-SQL-3 | Formatlash standarti majburiy (`06` §3.2): kalit so'zlar UPPER, identifikatorlar lower snake_case, 2 probel indent, klauzalar alohida qatorda. | R1 |
| FR-SQL-4 | Har snippet kontent CI'da namunaviy sxema ustida sintaktik tekshiriladi (PostgreSQL 16; `oracle` tegi — Oracle Database Free). O'tmagan snippet nashr qilinmaydi. | R1 |
| FR-SQL-5 | SQL uchun maxsus statistika: kalit so'zlar (UPPER) tezligi va aniqligi alohida ko'rsatkich ("Registr" turkumi). | R2 |

## 5. PL/SQL Training (`PLS`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-PLS-1 | PL/SQL — mustaqil yo'nalish, 18 mavzu (`06` §4): Fundamentals … Performance Optimization. | R1 (8 mavzu), R2 (qolgani) |
| FR-PLS-2 | Faqat Oracle PL/SQL (19c+). PL/pgSQL, T-SQL, MySQL protsedura sintaksisi **taqiqlangan**; kontent validatori taqiqlangan konstruksiyalar ro'yxatini tekshiradi (`BL-EM-3`). | R1 |
| FR-PLS-3 | Oracle konstruksiyalari qamrovi: `06` §4.3 jadvalidagi har konstruksiya ≥ 10 nashr qilingan snippetda uchraydi (to'liq dastur chiqqanda). | R2 |
| FR-PLS-4 | Blok terminatori `/` alohida qatorda — snippet tarkibida va teriladi. | R1 |
| FR-PLS-5 | Token pattern'lar: `:=`, `=>`, `||`, `..`, `<>`, `!=`, `%TYPE`, `%ROWTYPE`, `%NOTFOUND`, `%FOUND`, `%ROWCOUNT`, `%ISOPEN`, `<<`, `>>` alohida kuzatiladi. | R1 |
| FR-PLS-6 | Har snippet kontent CI'da Oracle Database Free konteynerida kompilyatsiya qilinadi (anonim blok — bajariladi; `CREATE OR REPLACE` — `USER_ERRORS` bo'sh). | R1 |
| FR-PLS-7 | Formatlash standarti majburiy (`06` §4.2), jumladan o'zgaruvchi prefikslari (`v_`, `p_`, `c_`, `t_`, `e_`, `g_`, `r_`). | R1 |

## 6. Typing Engine (`TE`)

To'liq spetsifikatsiya `07-typing-engine.md`.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-TE-1 | Har bosilgan belgi kutilgan belgi bilan real vaqtda solishtiriladi (belgima-belgi validatsiya). | R1 |
| FR-TE-2 | Belgi holatlari: `pending`, `correct`, `incorrect`, `corrected`; joriy belgi kursor bilan ko'rsatiladi. | R1 |
| FR-TE-3 | Joriy so'z (token) va joriy qator kuzatiladi va ajratiladi. | R1 |
| FR-TE-4 | Jonli ko'rsatkichlar: WPM, CPM, accuracy, xatolar, backspace, o'tgan vaqt, progress. Formulalar `00` §4.5. | R1 |
| FR-TE-5 | Taymer birinchi belgi bosilganda boshlanadi; `performance.now()` (monoton). Pauza faol vaqtga kirmaydi. | R1 |
| FR-TE-6 | Pauza/davom ettirish (faqat stop-on-error rejimlarda), qayta boshlash (har rejimda, `Tab`+`Enter`). | R1 |
| FR-TE-7 | Yangi qator: Enter faqat kutilgan belgi `\n` bo'lsa to'g'ri. Avtomatik indent standart; strict indent sozlamada. | R1 |
| FR-TE-8 | Tab: standart rejimda e'tiborsiz; strict indentda bitta indent birligini to'ldiradi. | R1 |
| FR-TE-9 | Maxsus belgilar va ko'p belgili operatorlar alohida kuzatiladi (belgi, bigram, turkum). | R1 |
| FR-TE-10 | Paste, drop, kesish, avtoto'ldirish, brauzer imlo tekshiruvi bloklanadi. | R1 |
| FR-TE-11 | IME kompozitsiyasi, dead key va ASCII bo'lmagan kiritish belgi sifatida qabul qilinmaydi; klaviatura layouti ogohlantirishi chiqadi. | R1 |
| FR-TE-12 | Windows/macOS/Linux farqlari: `event.key` asosida; `Ctrl`↔`Cmd`, `AltGr`, Option-kombinatsiyalari to'g'ri ishlanadi (`07` §4). | R1 |
| FR-TE-13 | To'liq keylog yoziladi va `complete` da serverga yuboriladi; rasmiy natija — server hisobi. | R1 |
| FR-TE-14 | Natija hech qachon klient tomonda "yaxshilanmaydi": yaxlitlash faqat ko'rsatishda, `00` §4.5 bo'yicha. | R1 |
| FR-TE-15 | Tarmoq uzilganda sessiya davom etadi; natija IndexedDB navbatida saqlanib, aloqa tiklanganda yuboriladi (24 soat ichida). | R1 |
| FR-TE-16 | Ekran klaviaturasi (touch): Lite rejim; `inputDevice=TOUCH`, reytingga kirmaydi. | R3 |

## 7. Training Modes (`TM`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-TM-1 | **Practice:** tanlangan mavzu × darajadan bitta snippet; taymer ko'rinmaydi; stop-on-error (sozlamada o'chiriladi); pauza bor. | R1 |
| FR-TM-2 | **Speed Test:** 30/60/120 s; shu tilning ranked pool'idan uzluksiz snippet oqimi (mehmon va reytingli); vaqt tugashi bilan to'xtaydi. | R1 |
| FR-TM-3 | **Accuracy:** snippet oxirigacha; accuracy 97% dan pastga tushsa sessiya `COMPLETED` (to'liq emas, `completionPct < 100`) bo'lib tugaydi; "master" variantida birinchi xatoda. | R1 |
| FR-TM-4 | **Special Characters:** tanlangan yoki tavsiya etilgan belgi turkumi bo'yicha 150 belgilik mashq (`08` §3). | R1 |
| FR-TM-5 | **Challenge:** `ADVANCED`/`EXPERT` snippet + shaxsiy vaqt chegarasi; vaqt tugasa sessiya tugaydi, tugallanmagan qism hisobga kirmaydi. | R2 |
| FR-TM-6 | **Weakness Training:** zaiflik profili asosida 3 snippet (birinchisi `SPECIAL_CHARS` turidan); profil bo'sh bo'lsa rejim nofaol va sababi ko'rsatiladi (`BL-TM-4`). | R2 |
| FR-TM-7 | **Code Sprint:** bitta mavzudan 5 snippet ketma-ket (daraja o'sib boradi), orada 3 s; umumiy natija; to'liq tugatish bonusi. | R2 |
| FR-TM-8 | Har rejim serverda `POST /sessions` bilan ochiladi; snippet(lar) faqat shu javobda beriladi. | R1 |
| FR-TM-9 | Snippet tanlash: oxirgi 20 sessiyada ko'rilganlar chiqariladi; zaxira tugasa eng eski ko'rilgani (`BL-TM-1`). | R1 |
| FR-TM-10 | Natija ekrani: asosiy metrikalar, WPM grafigi, eng zaif 5 belgi, eng sekin 3 qator, rekord bilan farq, mukofotlar, Coach maslahati (R2), "keyingi" harakati. | R1 |

## 8. Special Characters Training (`SC`)

To'liq: `08-special-characters-virtual-keyboard.md` §1–3.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-SC-1 | Belgilar 10 turkumga bo'linadi (qavslar, tinish, tayinlash/taqqoslash, strelkalar, mantiqiy, arifmetik, identifikator, qo'shtirnoqlar, bo'shliq, registr). | R1 |
| FR-SC-2 | Har til o'z belgi to'plami va token pattern'lariga ega; mashq shu tilga xos. | R1 |
| FR-SC-3 | Mashq tarkibi: 60% — shu turkum zich bo'lgan real kod qatorlari (kontentdan), 40% — generatsiya qilingan qisqa ketma-ketliklar (`BL-SC-1`). | R1 |
| FR-SC-4 | Maxsus belgilar accuracy va tezligi har sessiyada va umumiy statistikada alohida ko'rsatkich. | R1 |
| FR-SC-5 | Chalkashlik juftliklari (kutilgan → bosilgan) kuzatiladi va ko'rsatiladi. | R2 |
| FR-SC-6 | Tizim eng zaif turkumni tavsiya qiladi (≥ 30 kuzatuv bo'lsa); aks holda til bo'yicha standart tartib. | R2 |

## 9. Virtual Keyboard (`VK`)

To'liq: `08` §4.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-VK-1 | Typing maydoni ostida ekrandagi klaviatura tasviri: keyingi bosiladigan klavish ajratiladi; Shift kerak bo'lsa qarama-qarshi qo'ldagi Shift ham. | R1 |
| FR-VK-2 | Layoutlar: ANSI US QWERTY (standart), ISO UK QWERTY, Dvorak, Colemak. Modifikator yorliqlari OS'ga mos (Ctrl/Alt/Win yoki Control/Option/Command). | R1 |
| FR-VK-3 | Barmoq zonalari rang bilan (8 barmoq + bosh barmoq); "barmoq yo'riqnomasi" yoqiladi/o'chiriladi. | R1 |
| FR-VK-4 | Bosilgan klavish qisqa yonadi: to'g'ri — accent, xato — error rang. | R1 |
| FR-VK-5 | Ko'rinish standarti: Practice (Beginner) va Special Characters — yoqiq; boshqa rejimlar — o'chiq; foydalanuvchi sozlamasi ustun. < 768 px da ko'rsatilmaydi. | R1 |
| FR-VK-6 | Klaviatura faqat tasvir: sichqoncha bilan bosish kiritish hisoblanmaydi. | R1 |
| FR-VK-7 | Analytics'da heatmap rejimi: klavish bo'yicha xato darajasi yoki tezlik; barmoq bo'yicha statistika. | R2 |
| FR-VK-8 | Layout mos kelmasligi aniqlansa (`BL-VK-2`) foydalanuvchiga layoutni almashtirish taklif qilinadi. | R1 |

## 10. Developer Code Editor (`CE`)

To'liq: `09-code-editor.md`.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-CE-1 | Typing yuzasi kod muharriri ko'rinishida: qator raqamlari, monospace shrift, syntax highlighting, joriy qator ajratilishi, indent yo'riqchilari. | R1 |
| FR-CE-2 | Syntax highlighting har til uchun (Shiki, TextMate grammatikalari), dark va light mavzularda; snippet yuklanganda bir marta tokenizatsiya. | R1 |
| FR-CE-3 | Belgi holati sintaksis rangidan mustaqil kanallar bilan ko'rsatiladi (yorqinlik, tagchiziq, fon). | R1 |
| FR-CE-4 | Ko'rinadigan oyna: 9 qator; joriy qator 3-o'rinda; silliq vertikal scroll; 80 belgidan uzun qator yo'q (kontent kafolati). | R1 |
| FR-CE-5 | Juft qavs ajratilishi: kursor qavs yonida bo'lsa uning jufti belgilanadi. | R1 |
| FR-CE-6 | Qavslar avtomatik yopilmaydi; avtoto'ldirish yo'q; ko'p kursor yo'q; matnni tanlash/nusxalash typing paytida o'chiq. | R1 |
| FR-CE-7 | Sozlamalar: shrift (JetBrains Mono, Fira Code, Source Code Pro, tizim), o'lcham (14–24), qator balandligi, kursor turi (line/block/underline), kursor silliqligi, ligaturalar (standart o'chiq), qator raqamlari, ko'rinmas belgilar (`·`, `↵`, `→`). | R1 |
| FR-CE-8 | Replay rejimi: xuddi shu yuzada keylog qayta ijro etiladi (1×/2×/4×, pauza, oldinga/orqaga 5 s). | R1 |
| FR-CE-9 | Admin snippet muharriri: CodeMirror 6 asosida haqiqiy tahrirlash, jonli validatsiya, hisoblangan metrikalar, "typing preview". | R1 |

## 11. Analytics Dashboard (`AN`)

To'liq: `10-analytics-ai-coach.md` §A.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-AN-1 | Umumiy ko'rsatkichlar: hozirgi WPM, o'rtacha WPM, maksimal WPM, o'rtacha CPM, accuracy, jami vaqt, jami belgilar, tugatilgan mashqlar, jami xatolar. | R1 |
| FR-AN-2 | Xuddi shu ko'rsatkichlar har til uchun alohida; tillar taqqoslash diagrammasi. | R1 |
| FR-AN-3 | Vaqt qatorlari: kunlik, haftalik, oylik; davrlar 7 / 30 / 90 kun / hammasi. | R1 |
| FR-AN-4 | Rejim filtri: standart — `PRACTICE` + `SPEED_TEST`; `SPECIAL_CHARS` va `WEAKNESS` alohida. | R1 |
| FR-AN-5 | Belgi, bigram, turkum statistikasi; klaviatura heatmap; chalkashlik juftliklari. | R2 |
| FR-AN-6 | Faollik kalendari (365 kun, kunlik faol vaqt). | R1 |
| FR-AN-7 | Eng zaif til va belgilar aniqlanadi; tavsiyalar (R2 dan Coach orqali). | R2 |
| FR-AN-8 | < 5 sessiyada grafik o'rniga bo'sh holat. Soxta/namunaviy ma'lumot ko'rsatilmaydi. | R1 |
| FR-AN-9 | Tarix: sessiyalar ro'yxati (filtr, cursor sahifalash), sessiya tafsiloti, replay. | R1 |

## 12. AI Personal Coach (`AI`)

To'liq: `10` §B.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-AI-1 | Coach ikki qatlamli: (a) **qoida/statistika dvigateli** — barcha qarorlarni (zaiflik, reja, maqsad prognozi) deterministik hisoblaydi; (b) **bayon qatlami** — shu tuzilgan natijani tabiiy tilda tushuntiradi. (a) LLM'siz to'liq ishlaydi. | R2 (a), R3 (b LLM) |
| FR-AI-2 | Kunlik reja: 3 ta vazifa (har biri bir bosishda sessiya ochadi); bajarilishi kuzatiladi (`BL-AI-1`). | R2 |
| FR-AI-3 | Sessiyadan keyingi maslahat: natija ekranida 1 ta, eng muhim kuzatuv (`BL-AI-2`). | R2 |
| FR-AI-4 | Maqsad: foydalanuvchi til uchun maqsad WPM va (ixtiyoriy) sana belgilaydi; Coach prognoz va haftalik kerakli sur'atni ko'rsatadi (`BL-AI-3`). | R2 |
| FR-AI-5 | Haftalik hisobot (yakshanba, foydalanuvchi vaqti): o'zgarishlar, yaxshilangan va zaif belgilar, keyingi hafta fokusi; ilovada va (ixtiyoriy) email. | R2 |
| FR-AI-6 | Bayon 4 lokalda. LLM yoqilmagan yoki xato bersa — shablon matnlar (i18n) ishlatiladi; foydalanuvchi farqni xato sifatida ko'rmaydi. | R2 |
| FR-AI-7 | LLM'ga faqat agregat statistika yuboriladi: email, username, IP, kod snippeti matni yuborilmaydi. | R3 |
| FR-AI-8 | LLM javobi JSON sxemaga tekshiriladi; uzunlik chegarasi; sxemaga mos kelmasa shablonga qaytiladi. Coach hech qachon snippet generatsiya qilmaydi. | R3 |
| FR-AI-9 | Foydalanuvchi Coach'ni o'chirishi mumkin (sozlama); o'chirilganda reja va maslahatlar ko'rsatilmaydi, statistika yig'ilishi davom etadi. | R2 |

## 13. Gamification (`GM`)

To'liq: `11-gamification-leaderboard.md`.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-GM-1 | XP har `COMPLETED` sessiya uchun formula bo'yicha; ledger (`xp_transactions`), idempotent. `REJECTED`/`ABANDONED` — 0 XP. | R1 |
| FR-GM-2 | Level umumiy XP'dan; level oshishi natija ekranida e'lon qilinadi. | R1 |
| FR-GM-3 | Til ranki (Bronze…Diamond) shu tilning hozirgi WPM'idan; ≥ 10 sessiyadan keyin beriladi. | R1 |
| FR-GM-4 | Streak foydalanuvchi timezone'ida; freeze; mukofotlar. | R1 |
| FR-GM-5 | Shaxsiy rekordlar: til × rejim × davomiylik; accuracy ≥ 95% shart. | R1 |
| FR-GM-6 | Achievement'lar: qoida (JSON) asosida, bosqichli, progress bilan; admin qo'shadi. | R2 |
| FR-GM-7 | Kunlik va haftalik challenge. | R2 |
| FR-GM-8 | Reytinglar: til × rejim × davomiylik × davr; global/mamlakat/kuzatayotganlarim (R3); o'z o'rni doim ko'rinadi. | R2 |
| FR-GM-9 | Global ranking: XP doskasi va DevScore. | R2 |
| FR-GM-10 | Asinxron musobaqa: vaqt oynasi, cheklangan urinish, umumiy snippet to'plami. | R2 |
| FR-GM-11 | Jonli poyga: 2–8 kishi, WebSocket. | R3 |

## 14. Exercise Management (`EM`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-EM-1 | Mashq maydonlari: til, mavzu, daraja, turi (`SNIPPET`, `SPECIAL_CHARS`, `CHALLENGE`), sarlavha (en), izoh (4 lokal), kod, teglar, kutilgan davomiylik (avtomatik, tahrirlanadi), ranked pool belgisi, manba/litsenziya. | R1 |
| FR-EM-2 | Hayot sikli: `DRAFT` → `IN_REVIEW` → `PUBLISHED` → `ARCHIVED`. `publish` ni muallifdan boshqa `EDITOR`/`ADMIN` bajaradi (to'rt ko'z tamoyili). | R1 |
| FR-EM-3 | Saqlashda avtomatik validatsiya (`BL-EM-1`): belgilar, qator oxirlari, indent, uzunlik, dublikat, til bo'yicha taqiqlangan konstruksiyalar. | R1 |
| FR-EM-4 | Tizim hisoblaydi: `char_count`, `line_count`, `special_ratio`, `char_freq`, `expected_seconds`, ichma-ichlik chuqurligi. | R1 |
| FR-EM-5 | Versiyalash: nashr qilingan mashq kodi o'zgarmaydi; tahrir → yangi versiya (`exercise_key` bir xil), eskisi `ARCHIVED`. Izoh/teg tahriri versiya yaratmaydi. | R1 |
| FR-EM-6 | Ommaviy import: `content/` YAML → validatsiya hisoboti → `DRAFT` yoki `IN_REVIEW` sifatida yuklash; idempotent (`code_hash` bo'yicha). | R1 |
| FR-EM-7 | Qamrov matritsasi: til × mavzu × daraja bo'yicha nashr qilingan mashqlar soni; lokallar bo'yicha tarjima to'liqligi. | R1 |
| FR-EM-8 | Foydalanuvchi mashq haqida shikoyat yuboradi (xato, noto'g'ri daraja, boshqa); admin navbatida ko'rinadi. | R2 |
| FR-EM-9 | Mashq statistikasi (admin): urinishlar, o'rtacha WPM/accuracy, tashlab ketish ulushi — daraja kalibrlash uchun. | R2 |

## 15. User Dashboard (`UD`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-UD-1 | Bitta so'rov (`GET /me/dashboard`) bilan yuklanadi. | R1 |
| FR-UD-2 | "Davom ettirish" kartasi: oxirgi tugallanmagan mavzu × daraja yoki asosiy tilning keyingi tavsiya etilgan mashqi (`BL-UD-1`). | R1 |
| FR-UD-3 | Streak holati (joriy, eng uzun, bugun bajarildimi, freeze qoldig'i); "xavf ostida" ogohlantirishi (mahalliy vaqt 20:00 dan keyin, bugun bajarilmagan bo'lsa). | R1 |
| FR-UD-4 | Til kartochkalari: hozirgi WPM, rank, progress foizi, 14 kunlik sparkline. | R1 |
| FR-UD-5 | XP va level paneli. | R1 |
| FR-UD-6 | Oxirgi 5 sessiya. | R1 |
| FR-UD-7 | Kunlik challenge va Coach rejasi (3 vazifa). | R2 |
| FR-UD-8 | Yangi foydalanuvchi (0 sessiya): faqat "Birinchi mashqni boshlash" va til kartochkalari; bo'sh grafiklar ko'rsatilmaydi. | R1 |

## 16. UI/UX Design (`UX`)

To'liq: `05-ui-ux-design-system.md`.

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-UX-1 | Dark (standart), Light va "tizim" temalari; almashtirish sahifani yuklamaydi; tanlov saqlanadi; birinchi chizishda miltillash (FOUC) yo'q. | R1 |
| FR-UX-2 | Responsive: 320 px – 3840 px; breakpointlar `05` §6. | R1 |
| FR-UX-3 | Navigatsiya: desktop — yon panel (yig'iladi); tablet — yig'ilgan yon panel; mobile — pastki tab bar. | R1 |
| FR-UX-4 | Har sahifada 4 holat: yuklanish (skeleton), bo'sh, xato (qayta urinish), ma'lumotli. | R1 |
| FR-UX-5 | Toast bildirishnomalar: muvaffaqiyat, xato, ma'lumot; 5 s; `aria-live`. | R1 |
| FR-UX-6 | Klaviatura bilan to'liq boshqaruv; ko'rinadigan fokus; modal fokus tuzog'i va `Esc`. | R1 |
| FR-UX-7 | Buyruqlar palitrasi (`Ctrl`+`K`). | R1 |
| FR-UX-8 | Animatsiyalar 120–180 ms; `prefers-reduced-motion` da o'chadi. | R1 |
| FR-UX-9 | Sozlamalar guruhlari: Ko'rinish, Typing, Muharrir, Klaviatura, Akkaunt, Maxfiylik, Bildirishnomalar (`05` §5.17). | R1 |
| FR-UX-10 | Barcha matn 4 lokalda (`22-i18n.md`). | R1 |

## 17. Admin Panel (`AD`)

| ID | Talab | Reliz |
| --- | --- | --- |
| FR-AD-1 | Kirish: `EDITOR` (kontent) va `ADMIN` (hammasi); 2FA majburiy. | R1 |
| FR-AD-2 | Tillar CRUD va nashr qilish (`ADMIN`). | R1 |
| FR-AD-3 | Mavzular va mashqlar CRUD, ko'rib chiqish navbati, import, qamrov matritsasi (`EDITOR`+). | R1 |
| FR-AD-4 | Foydalanuvchilar: qidirish, ko'rish, bloklash/blokdan chiqarish, rol berish (`ADMIN`). O'z rolini pasaytira olmaydi; oxirgi `ADMIN` ni o'chirib bo'lmaydi. | R1 |
| FR-AD-5 | Shubhali sessiyalar navbati: replay, qaror (`CLEAR` → `ranked=true`; `CONFIRM` → `REJECTED` + reyting yozuvlari qayta hisoblanadi; ixtiyoriy bloklash). | R2 |
| FR-AD-6 | Achievement, challenge shablonlari, musobaqalar CRUD. | R2 |
| FR-AD-7 | Mahsulot metrikalari paneli. | R2 |
| FR-AD-8 | Audit jurnali: barcha admin amallari (kim, nima, qachon, eski/yangi qiymat, IP); faqat o'qish. | R1 |
| FR-AD-9 | Mashq shikoyatlari navbati. | R2 |

## 18. Database & Backend (`BE`)

Batafsil: `12`, `13`, `14`. Talablar:

| ID | Talab |
| --- | --- |
| FR-BE-1 | Barcha foydalanuvchi ma'lumoti PostgreSQL'da; brauzerda faqat tokenlar, mehmon identifikatori, yuborilmagan natija navbati va UI keshi. |
| FR-BE-2 | Sxema faqat Flyway migratsiyalari orqali o'zgaradi; `ddl-auto=validate`. |
| FR-BE-3 | Modular monolith: 7 modul (`identity`, `content`, `training`, `analytics`, `gamification`, `coach`, `admin`); chegaralar build'da tekshiriladi. |
| FR-BE-4 | REST API `/api/v1`, OpenAPI 3.1 hujjati avtomatik generatsiya qilinadi va CI'da spetsifikatsiya bilan solishtiriladi. |
| FR-BE-5 | Xatolar RFC 9457 `ProblemDetail`; barqaror `code`. |
| FR-BE-6 | Sahifalash, filtr, saralash — `14` §1.4 qoidalari bo'yicha. |
| FR-BE-7 | Tranzaksiya chegarasi use-case darajasida; modullararo yon ta'sirlar hodisalar orqali (transactional outbox). |
| FR-BE-8 | Barcha hodisa ishlovchilari idempotent. |

## 19. Security & Performance (`SP`)

Batafsil: `16`, `17`. Talablar `FR-SP-1…` shu hujjatlarda jadval ko'rinishida; qabul mezonlari `AC-SP-*`.

## 20. Testing & Quality Assurance (`QA`)

Batafsil: `18`. Asosiy talab: har FR kamida bitta avtomatlashtirilgan test bilan qoplangan; bosqich mezonlari CI'da majburiy.

## 21. Platform Quality Standards (`QS`)

| ID | Talab |
| --- | --- |
| FR-QS-1 | Mock data yo'q: production kodida soxta ma'lumot, `TODO`/`FIXME` bilan qoldirilgan funksiya, placeholder sahifa bo'lmaydi. CI `main` kodida `TODO|FIXME|XXX|HACK` ni xato deb belgilaydi (ruxsat: `TODO(OQ-n)` — ochiq savolga havola). |
| FR-QS-2 | Kod sifati: backend — Checkstyle + Error Prone + ArchUnit; frontend — ESLint (strict) + Prettier + `tsc --noEmit`. Ogohlantirishlar xato hisoblanadi. |
| FR-QS-3 | Test qamrovi: backend domen paketlari ≥ 90% line, umumiy ≥ 80%; frontend `engine/` 100% branch, umumiy ≥ 75%. |
| FR-QS-4 | Accessibility: WCAG 2.1 AA. |
| FR-QS-5 | Lighthouse mo'ljallari (NFR-8). |
| FR-QS-6 | Brauzer va ekran qo'llab-quvvatlashi (NFR-5, NFR-6). |
| FR-QS-7 | Definition of Done: kod + testlar + 4 lokal + hujjat yangilangan + AC bajarilgan + ko'rib chiqilgan. |
| FR-QS-8 | Har ommaviy API o'zgarishi OpenAPI va `14` da aks etadi. |
| FR-QS-9 | Xato kuzatuvi: frontend va backend xatolari markaziy tizimga tushadi (`19`). |

## 22. Future Features (`FF`)

Ro'yxat va kengaytirish nuqtalari: `23-decisions-open-questions.md` §4. Bu funksiyalar **implementatsiya qilinmaydi**; arxitektura ularni to'smasligi kerak.

## 23. Acceptance Criteria (`AC`)

Barcha modullar uchun tekshiriladigan mezonlar: `18-testing-acceptance.md` §4.
