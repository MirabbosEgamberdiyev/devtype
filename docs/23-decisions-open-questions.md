# 23. Qarorlar jurnali, ziddiyatlar yechimi, ochiq savollar va kelajak funksiyalar

Bu hujjat ustuvorlik bo'yicha `00` dan keyin ikkinchi (`00` §1). Yangi qaror yoki savol qo'shilganda raqam ketma-ket davom etadi; eski yozuv o'chirilmaydi — `Holat` o'zgaradi (`Qabul qilingan` / `Almashtirilgan → D-n`).

## 1. Talablardagi ziddiyatlar va yechimlar

Asl talablar (foydalanuvchi spetsifikatsiyasi, 23 modul ro'yxati, audit talablari) orasidagi ziddiyatlar.

| # | Ziddiyat | Yechim | Qaror |
| --- | --- | --- | --- |
| Z-1 | Engine frontendda ishlaydi, lekin natijalar "real va aniq" bo'lishi va reytingga kirishi kerak | Klient metrikani faqat ko'rsatadi; rasmiy natija server replay'idan | D-1 |
| Z-2 | "Mobile'da to'liq ishlashi" va kod terish (`{}`, `:=`, `=>`) | Mobile'da barcha sahifalar to'liq; typing — tashqi klaviatura bilan to'liq, ekran klaviaturasida Lite (R3), reytingsiz | D-2 |
| Z-3 | "O'quv moduli" (Spring Boot, Streams) va typing trainer | Mavzu — kontent taksonomiyasi; har mashq idiomatik kod + 1–2 qator izoh. Nazariy kurs yo'q | D-3 |
| Z-4 | 16 til × ~15 mavzu × 4 daraja va "mock data yo'q" | Til faqat kontenti to'lganda nashr qilinadi (`BL-PL-2`); 3 relizga bo'lingan | D-4 |
| Z-5 | Indentatsiya va Tab'ni terish talabi, lekin real IDE'da ular avtomatik | Standart: avtomatik indent (WPM'ga kirmaydi); strict rejim sozlamada, reytingsiz | D-5 |
| Z-6 | 30/60/120 s test va chekli uzunlikdagi snippetlar | Speed Test'da uzluksiz snippet oqimi | D-6 |
| Z-7 | "WebSocket faqat zarur joyda" va jonli musobaqalar | Hammasi REST; WebSocket faqat jonli poygalarda (R3) | D-7 |
| Z-8 | Har til uchun alohida Practice sahifasi (7–11) va "yangi til kod o'zgarishisiz" | Bitta parametrli route `/practice/:lang` | D-8 |
| Z-9 | Clean Architecture + modular monolith, kichik jamoa | Modul chegaralari qat'iy; to'liq port/adapter faqat hisob-kitob modullarida | D-9 |
| Z-10 | Kod uchun WPM ta'rifi yo'q | Standart: `(C/5)/m`; avtomatik indent kirmaydi; accuracy bosishlar bo'yicha | D-10 |
| Z-11 | "Barcha ma'lumot backend'da" va ro'yxatsiz sinov | Mehmon sessiyasi ham serverda (`guest_id`), brauzerda faqat token | D-11 |
| Z-12 | Global reyting va turli qiyinlikdagi snippetlar | Reytingli Speed Test faqat kalibrlangan ranked pool'dan | D-12 |
| Z-13 | "Developer Code Editor" (muharrir) va typing semantikasi (erkin tahrirlash yo'q) | Ikki komponent: `TypingEditor` (o'zimiz) va admin `SnippetEditor` (CodeMirror) | D-13 |
| Z-14 | "AI Personal Coach" va natijalar halolligi / maxfiylik / "AI snippet generatsiyasi yo'q" | Deterministik qaror dvigateli + ixtiyoriy LLM bayoni; LLM snippet yaratmaydi, faqat agregat ma'lumot oladi | D-14 |
| Z-15 | SQL va PL/SQL alohida modul, lekin PL/SQL ichida SQL bor | SQL — ANSI (dialekt teglari bilan); PL/SQL — faqat Oracle protsedura tili + Oracle SQL | D-15 |
| Z-16 | React SPA va global auditoriya uchun SEO (Lighthouse SEO ≥ 95) | Ommaviy route'lar prerender; ilova qismi SPA | D-16 |
| Z-17 | "Virtual Keyboard" va jismoniy klaviaturada yozish | Virtual klaviatura — faqat yo'riqnoma va heatmap; kiritish qurilmasi emas | D-19 |
| Z-18 | 4 tilli sayt va ~4 600 snippetli kontent | Interfeys va metama'lumot 4 tilda; kod va sarlavhalar inglizcha; uzc transliteratsiya | D-20, D-21 |
| Z-19 | Lighthouse Performance ≥ 90 va og'ir kod muharriri/highlight | Typing va admin alohida chunk; ommaviy sahifalar prerender; Shiki JS engine; Monaco yo'q | D-13, D-16 |
| Z-20 | "Typing natijalarini yaxshilab ko'rsatish mumkin emas" va jonli WPM (qisqa oyna — yuqoriroq ko'rinishi mumkin) | Jonli WPM aniq yorliqli va natijada faqat yakuniy server qiymati; yaxlitlash qoidasi qat'iy | D-10 |
| Z-21 | Ro'yxatdan o'tishda "email band" xabari (UX) va akkaunt mavjudligini oshkor qilmaslik | Ro'yxatdan o'tishda xabar beriladi (rate limit bilan); login va parol tiklashda — yo'q | D-18 |

## 2. Qarorlar jurnali

| ID | Qaror | Sabab | Muqobillar (rad etilgan) | Holat |
| --- | --- | --- | --- | --- |
| D-1 | Rasmiy metrika — serverda keylog replay'idan; klient metrikasi faqat ko'rsatish va diagnostika | Brauzer ishonchsiz; reyting halolligi | Klient metrikasini qabul qilish + heuristika | Qabul qilingan |
| D-2 | Typing to'liq — jismoniy klaviatura; touch — Lite (R3), reytingsiz | Ekran klaviaturasida maxsus belgilar 2–3 bosish; WPM solishtirilmaydi | Mobil uchun alohida virtual kiritish | Qabul qilingan |
| D-3 | Mavzu = kontent taksonomiyasi, nazariy dars yo'q | Fokus; kurs platformasi alohida mahsulot | Mini-darslar | Qabul qilingan |
| D-4 | Til nashri kontent to'liqligiga bog'liq; 3 reliz | Yarim bo'sh til ishonchni buzadi | Hammasini birdan | Qabul qilingan |
| D-5 | Avtomatik indent standart; strict ixtiyoriy va reytingsiz | Real IDE xulqi; Python/PL/SQL WPM sun'iy pasaymasligi | Har doim qo'lda | Qabul qilingan (OQ-10) |
| D-6 | Speed Test — snippet oqimi | Tez yozuvchi snippetni erta tugatadi | Bitta uzun snippet | Qabul qilingan |
| D-7 | REST asosiy; WebSocket faqat jonli poyga | Talab; soddalik | SSE / polling | Qabul qilingan |
| D-8 | `/practice/:lang` bitta sahifa | Yangi til kod o'zgarishisiz | Har til uchun sahifa | Qabul qilingan |
| D-9 | Modular monolith + Spring Modulith; to'liq hexagonal faqat `training`, `gamification` domenida | Kichik jamoa; CRUD uchun ortiqcha qatlamlar yo'q | Mikroservislar; hamma joyda hexagonal | Qabul qilingan |
| D-10 | WPM = `(C/5)/m`, accuracy = `K✓/(K✓+K✗)`, tuzatilgan xato ham xato; yaxlitlash `HALF_UP` 2 xona | Sanoat standarti (Monkeytype); tillararo solishtirish | Raw WPM asosiy; accuracy yakuniy belgilar bo'yicha | Qabul qilingan |
| D-11 | Mehmon sessiyalari serverda, 7 kun | "Barcha ma'lumot backend'da" talabi | `localStorage` | Qabul qilingan |
| D-12 | Ranked pool: INTERMEDIATE, 250–450 belgi, maxsus 15–22% | Adolatli reyting | Tasodifiy har qanday snippet | Qabul qilingan |
| D-13 | `TypingEditor` — o'z komponentimiz (Shiki); admin — CodeMirror 6; Monaco yo'q | Kiritishni to'liq nazorat; bundle hajmi | Monaco, CodeMirror typing uchun | Qabul qilingan |
| D-14 | Coach: deterministik qaror + shablon bayon; LLM faqat bayon, ixtiyoriy (R3) | Ishonchlilik, xarajat, maxfiylik, halollik | LLM to'liq coach | Qabul qilingan (OQ-5) |
| D-15 | SQL = ANSI + dialekt teglari; PL/SQL = faqat Oracle; taqiqlangan konstruksiyalar validatori | Talab; aralashtirmaslik | Bitta "SQL" moduli | Qabul qilingan |
| D-16 | Ommaviy sahifalar build vaqtida prerender (`vite-ssg`); SSR yo'q | SEO + soddalik | Next.js/SSR | Qabul qilingan |
| D-17 | Sahifa yangilanganda faol sessiya tiklanmaydi (`ABANDONED`) | Soddalik; keylog yaxlitligi | `sessionStorage` dan tiklash | Qabul qilingan |
| D-18 | Ro'yxatda "email/username band" xabari ko'rsatiladi | UX; rate limit bilan cheklangan | Har doim "xat yuborildi" | Qabul qilingan |
| D-19 | Virtual klaviatura — tasvir; layout statik JSON | Jismoniy klaviaturada yozish maqsadi | Bosiladigan ekran klaviaturasi | Qabul qilingan |
| D-20 | URL/API/baza — `uzl, uzc, ru, en`; tashqariga BCP 47 | Qulaylik + standartlar | Faqat BCP 47 | Qabul qilingan |
| D-21 | Tarjimalar `*_i18n JSONB` da | Kichik, barqaror tillar soni; JOIN yo'q | Alohida tarjima jadvali | Qabul qilingan |
| D-22 | `uzc` — `uzl` dan transliteratsiya + override | Tarjima hajmi; izchillik | Alohida qo'lda tarjima | Qabul qilingan |
| D-23 | Mavzular orasida qulf yo'q; qulf faqat daraja orasida | Tajribali foydalanuvchi erkinligi | Chiziqli dastur | Qabul qilingan |
| D-24 | XP tezlikka bog'liq emas (aniqlik va hajm); tezlik — reyting va rekord orqali | Shoshma-shosharlikni rag'batlantirmaslik | WPM koeffitsienti | Qabul qilingan |
| D-25 | Docker Compose + blue-green; Kubernetes yo'q | Bir server yuklamasi; operatsion soddalik | K8s | Qabul qilingan |
| D-26 | Kunlik XP chegarasi 3 000 | Farm qilishni cheklash | Chegarasiz | Qabul qilingan |
| D-27 | Accuracy rejimida 97% dan pastga tushish — sessiya tugaydi (`COMPLETED`, to'liq emas) | Rejim maqsadi; XP yozilgan qism uchun | `REJECTED` / `ABANDONED` | Qabul qilingan |
| D-28 | Bir foydalanuvchida ≤ 3 ochiq sessiya | Resurs himoyasi; ko'p tab | Cheksiz | Qabul qilingan |
| D-29 | Kontent manbasi — `content/` YAML (Git) + import; baza — ish nusxasi | Ko'rib chiqish, tarix, CI kompilyatsiyasi | Faqat admin panel | Qabul qilingan |
| D-30 | Erkin rejimda 8 ketma-ket xatodan keyin kiritish bloklanadi | Tasodifiy "mashing" | Chegarasiz | Qabul qilingan |

## 3. OPEN QUESTION ro'yxati

Har savolda **standart qiymat** bor — implementatsiya shu bilan davom etadi; mahsulot egasi boshqacha qaror qilsa, tegishli hujjat va kod yangilanadi.

| ID | Savol | Standart (hozir amalda) | Ta'sir qiladigan hujjatlar | Qachongacha |
| --- | --- | --- | --- | --- |
| OQ-1 | MVP qaysi 5 til bilan chiqadi? | `java`, `python`, `javascript`, `sql`, `plsql` | `00` §4.1, `06`, `20` | 0-bosqich oxiri |
| OQ-2 | Jamoa tarkibi va hajmi | 4 kishi (2 BE, 1 FE, 1 dizayn/kontent) → 28 hafta | `01` §8, `20` | 0-bosqich |
| OQ-3 | Mahsulot nomi va domen | Ishchi nom "DevType", domen `devtype.dev` (misollarda) | Barcha (brend matnlari, `type` URL'lari) | R1 dizayni |
| OQ-4 | Rank chegaralari tilga qarab kalibrlansinmi? | Hozir hamma tilda bir xil (`00` §4.7); R2 da foiz (percentile) asosiga o'tish taklif qilinadi | `00`, `10` §A.6, `11` §3 | R2 boshi (ma'lumot asosida) |
| OQ-5 | LLM provayderi, modeli va oylik byudjeti | Anthropic Claude API; byudjet konfiguratsiyada; foydalanuvchiga haftada 1 hisobot + kuniga 3 tushuntirish | `10` §B.6, `19` | R3 boshi |
| OQ-6 | Monetizatsiya (pullik tarif) | Yo'q; hamma funksiyalar bepul | `01` §5.2 | Ochiq |
| OQ-7 | Muvaffaqiyat metrikalari mo'ljallari | `01` §6 jadvali | `01` | Relizdan 4 hafta keyin |
| OQ-8 | Jonli poyga to'liq spetsifikatsiyasi | `11` §9.2 dagi qisqa tavsif | `11`, `14`, `13` | R3, 21-hafta |
| OQ-9 | CSP `style-src` uchun nonce/hash'ga o'tish | `'unsafe-inline'` faqat stillar uchun | `16` §3 | R2 |
| OQ-10 | Indentatsiya standart holda avtomatikmi yoki qo'lda? | Avtomatik (D-5) | `07` §1, `05` §5.17 | 0-bosqich |
| OQ-11 | Hosting provayderi va mintaqa | Yevropa mintaqasidagi VPS + (mavjud bo'lsa) boshqariladigan PostgreSQL | `19` §2 | 0-bosqich |
| OQ-12 | Email provayderi | SMTP orqali tranzaksion xat xizmati (masalan Postmark/SES ekvivalenti) | `19`, `13` | 1-bosqich, 3-hafta |
| OQ-13 | Kontent mualliflari: ichki jamoa yoki hamjamiyat hissasi (PR orqali)? | Ichki jamoa; hamjamiyat — R3 dan keyin, `content/` PR + ikki bosqichli ko'rib chiqish | `06` §5, `19` §4.3 | R2 |
| OQ-14 | Foydalanuvchining eng kam yoshi (ToS) | 13 yosh (huquqiy maslahat bilan tasdiqlanadi) | Huquqiy sahifalar | R1 relizdan oldin |

## 4. Future Features (modul 22)

Bu funksiyalar **implementatsiya qilinmaydi**. Arxitektura ularni to'smasligi kerak — har biri uchun kengaytirish nuqtasi ko'rsatilgan.

| # | Funksiya | Qisqa tavsif | Kengaytirish nuqtasi (hozir tayyor) |
| --- | --- | --- | --- |
| FF-1 | Jamoalar / tashkilotlar | Kompaniya ichki reytingi, mentor paneli | `board_key` formati (`scope` qo'shish); `identity` ga `organizations` jadvali |
| FF-2 | Hamjamiyat snippetlari | Foydalanuvchi taklif qiladi, moderatsiya | `FR-EM-2` hayot sikli (`DRAFT → IN_REVIEW`), `exercise_reports` |
| FF-3 | Foydalanuvchi o'z kodini yuklab mashq qilish | Shaxsiy snippetlar (reytingsiz) | `exercise_type` kengaytirish; `BL-EM-1` validatori qayta ishlatiladi |
| FF-4 | Native mobil ilovalar | iOS/Android | Holatsiz REST API; `inputDevice` |
| FF-5 | IDE plaginlari (VS Code, JetBrains) | Muharrir ichida mashq | `/api/v1` + engine TS paketini qayta ishlatish |
| FF-6 | Ochiq API va webhook'lar | Shaxsiy statistikani eksport, integratsiyalar | OpenAPI; R3 da faqat o'qish API'si boshlanadi |
| FF-7 | Sertifikatlar | Tasdiqlangan tezlik/aniqlik guvohnomasi | Server tasdiqlagan natijalar + keylog arxivi |
| FF-8 | Ovozli/ko'z bilan boshqaruvdan foydalanuvchilar uchun moslashuvlar | Kengaytirilgan accessibility | `blindMode`, a11y asosi |
| FF-9 | Kodni "o'qish" mashqlari (yozishdan oldin tushunish) | Snippetni tahlil qilish savollari | Mavzu taksonomiyasi |
| FF-10 | Qo'shimcha UI lokallari (tr, kk, de, …) | Yangi til | `22` §5 (CHECK + JSONB kaliti + fayllar) |
| FF-11 | Qo'shimcha dasturlash tillari (Swift, Dart, Scala, R, Lua, YAML, Dockerfile) | Yangi kontent | `FR-PL-5` — kod o'zgarishisiz |
| FF-12 | Moslashuvchan qiyinlik (adaptive) | Snippet tanlash foydalanuvchi darajasiga real vaqtda moslashadi | `ExercisePicker` strategiya interfeysi |
| FF-13 | Pullik tarif | Kengaytirilgan coach, eksport | Feature flag mexanizmi |

Yangi g'oya shu jadvalga qo'shiladi; relizga kiritish — qaror jurnali orqali.
