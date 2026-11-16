# 05. UI/UX va Design System

Yo'nalish: Monkeytype'ning chalg'itmaydigan typing ekrani + Linear/Vercel/GitHub'ning zich, sokin interfeysi. Typing ekranida kod — yagona qahramon.

## 1. Dizayn tokenlari

Tokenlar CSS o'zgaruvchilari sifatida (`:root[data-theme="dark"|"light"]`), Tailwind konfiguratsiyasi shularga ishora qiladi. Komponentlarda xom hex qiymat ishlatilmaydi.

### 1.1 Ranglar

| Token | Dark (standart) | Light | Qo'llanishi |
| --- | --- | --- | --- |
| `--bg` | `#0B0D10` | `#FAFAFA` | Sahifa foni |
| `--surface` | `#14171C` | `#FFFFFF` | Karta, panel |
| `--surface-2` | `#1B1F26` | `#F3F4F6` | Hover, ichki panel, joriy qator |
| `--border` | `#232830` | `#E4E6EA` | Chegaralar |
| `--border-strong` | `#343B46` | `#C9CDD4` | Input chegarasi, fokus bo'lmagan holat |
| `--text` | `#E6E8EB` | `#16181D` | Asosiy matn |
| `--text-muted` | `#9AA1AC` | `#5C6370` | Ikkilamchi matn |
| `--text-faint` | `#5C6370` | `#9AA1AC` | Yozilmagan kod, placeholder |
| `--accent` | `#4ADE80` | `#16A34A` | Kursor, asosiy tugma, progress |
| `--accent-fg` | `#052E16` | `#FFFFFF` | Accent ustidagi matn |
| `--error` | `#F87171` | `#DC2626` | Xato |
| `--warning` | `#FBBF24` | `#D97706` | Tuzatilgan xato, ogohlantirish |
| `--info` | `#60A5FA` | `#2563EB` | Ma'lumot, havola |
| `--focus-ring` | `#60A5FA` | `#2563EB` | Fokus halqasi (2 px, offset 2 px) |

Rank ranglari: Bronze `#B87333`, Silver `#A8B0BA`, Gold `#E5B93C`, Platinum `#7FD6D0`, Diamond `#8AB4F8` (ikkala temada, har doim matn yorlig'i bilan birga).

Barmoq zonalari (virtual klaviatura), chapdan o'ngga 8 barmoq: `#F87171`, `#FB923C`, `#FBBF24`, `#4ADE80`, `#4ADE80`, `#60A5FA`, `#A78BFA`, `#F472B6`; bosh barmoq `--surface-2`. Zona 20% shaffoflikda fon sifatida.

Kontrast: matn/fon ≥ 4.5:1; `--text-faint`/`--bg` ≥ 3:1 (yirik kod matni); "Yuqori kontrast" sozlamasi `--text-faint` ni `--text-muted` ga almashtiradi.

### 1.2 Tipografiya

| Rol | Shrift | O'lcham / qator | Vazn |
| --- | --- | --- | --- |
| UI | Inter (lotin + kirill) | — | 400, 500, 600 |
| Kod | JetBrains Mono (standart); Fira Code, Source Code Pro, `ui-monospace` | sozlanadi | 400, 700 |
| `display` | Inter | 36/44 (mobile 28/36) | 600 |
| `h1` | Inter | 24/32 | 600 |
| `h2` | Inter | 18/28 | 600 |
| `body` | Inter | 14/22 | 400 |
| `small` | Inter | 12/18 | 400 |
| `metric` | JetBrains Mono | 32/40 (natija ekranida 48/56) | 700, tabular-nums |
| `code` | Kod shrifti | 18/1.6 desktop · 16 tablet · 14 mobile | 400 |

Shriftlar o'z serverimizdan (`woff2`, `font-display: swap`, `unicode-range` bo'yicha lotin va kirill alohida). Ligaturalar standart o'chiq (`font-variant-ligatures: none`).

### 1.3 O'lchov tizimi

- Spacing shkalasi (px): 4, 8, 12, 16, 24, 32, 48, 64.
- Radius: 6 (input, tugma), 8 (karta), 12 (modal), 999 (badge).
- Soya: faqat suzuvchi elementlarda (dropdown, modal, toast): `0 8px 24px rgb(0 0 0 / 0.24)`.
- Kontent kengligi: maks. 1200 px; typing yuzasi 80 belgi (`80ch`) + qator raqamlari.
- Z-index: sidebar 20, header 30, dropdown 40, modal 50, toast 60.
- Animatsiya: 120 ms (hover, fokus), 180 ms (ochilish), `cubic-bezier(0.2, 0, 0, 1)`; `prefers-reduced-motion: reduce` → 0 ms.

## 2. Komponentlar

Har komponent: variantlar, holatlar (default, hover, focus-visible, active, disabled, loading), a11y talabi. Asos: Radix UI primitives + Tailwind.

| Komponent | Variantlar | Muhim talablar |
| --- | --- | --- |
| **Button** | `primary`, `secondary`, `ghost`, `danger`; o'lcham `sm` (28), `md` (36), `lg` (44) | `loading` da spinner + `aria-busy`, matn o'zgarmaydi; min. bosish maydoni 44×44 (mobile) |
| **Input / Textarea** | matn, parol (ko'rsatish tugmasi), qidiruv | Label doim ko'rinadi; xato matni input ostida, `aria-describedby`; xato chegarasi `--error` |
| **Select / Combobox** | oddiy, qidiruvli (til, mamlakat) | Klaviatura: ↑↓, Enter, Esc, harf bilan qidirish |
| **Checkbox / Switch / Radio** | — | Switch sozlamalarda; holat matni bilan |
| **Tabs** | chiziqli, segmentli | ←→ bilan o'tish; `role="tablist"` |
| **Card** | oddiy, bosiladigan | Bosiladigan karta — bitta fokuslanadigan element |
| **Dialog (modal)** | tasdiq, forma | Fokus tuzog'i; `Esc` va fon bosilishi yopadi (xavfli tasdiqda fon yopmaydi); fokus ochgan elementga qaytadi |
| **Dropdown menu** | — | Ekrandan chiqmaydi (collision detection); `Esc` yopadi |
| **Tooltip** | — | 400 ms kechikish; fokusda ham chiqadi; mobile'da yo'q |
| **Toast** | `success`, `error`, `info` | O'ng pastda (mobile: tepada); 5 s; `error` qo'lda yopilguncha; `role="status"`/`alert`; ko'pi 3 ta |
| **Table** | saralanadigan, sahifalanadigan | Sarlavha yopishadi; mobile'da karta ro'yxatiga aylanadi; bo'sh holat qatori |
| **Skeleton** | matn, karta, grafik, jadval qatori | Spinner o'rniga; 150 ms dan tez yuklansa ko'rsatilmaydi |
| **EmptyState** | — | Ikonka, sarlavha, izoh, bitta harakat tugmasi |
| **ErrorState** | — | Xabar + "Qayta urinish"; texnik tafsilot yo'q, `traceId` nusxalanadi |
| **Badge** | holat, rank, daraja | Rang + matn (faqat rang emas) |
| **ProgressBar / ProgressRing** | — | `role="progressbar"`, `aria-valuenow` |
| **CommandPalette** | — | `Ctrl`/`Cmd`+`K`; til, mavzu, rejim, sahifa, sozlama qidiruvi |

Mahsulotga xos komponentlar: `TypingEditor` (`09`), `VirtualKeyboard` (`08`), `ResultSummary`, `WpmChart`, `KeyboardHeatmap`, `ActivityCalendar`, `LanguageCard`, `TopicRow`, `LevelDots`, `StreakBadge`, `XpBar`, `RankBadge`, `AchievementCard`, `LeaderboardRow`, `CoachPlanCard`, `CoachTipCard`, `GoalCard`, `LanguageSwitcher`, `ThemeToggle`.

## 3. Layout

| Zona | Tuzilma |
| --- | --- |
| **Ommaviy** | Header (64 px) + kontent + footer |
| **Ilova** | Yon panel (240 px, yig'ilganda 64 px) + header (56 px: sarlavha, qidiruv/palitra, XP, streak, avatar) + kontent |
| **Typing (focus)** | Yon panel yashirin; faqat kontekst paneli, muharrir, (ixtiyoriy) virtual klaviatura, yorliqlar |
| **Admin** | Ilova layouti + admin yon menyusi |
| **Auth** | Markazda 400 px karta |

## 4. Typing ekrani (asosiy ekran)

```text
┌──────────────────────────────────────────────────────────────────────────┐
│ PL/SQL · CURSOR · Advanced        Challenge                   42%  ⏱ 01:12│  kontekst paneli (40 px)
│ ▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔│  progress chizig'i (2 px)
│  12 │ ··OPEN c_orders;                                                    │
│  13 │ ··LOOP                                                              │
│▌ 14 │ ····FETCH c_ord|ers BULK COLLECT          ← joriy qator (3-o'rin)   │  muharrir: 9 qator
│  15 │ ······INTO t_ids LIMIT c_limit;                                     │
│  16 │ ····EXIT WHEN t_ids.COUNT = 0;                                      │
│  …                                                                       │
│                     [ virtual klaviatura — ixtiyoriy ]                   │
│  Tab+Enter qayta boshlash     Esc pauza     Ctrl+→ keyingi                │  yorliqlar
└──────────────────────────────────────────────────────────────────────────┘
```

- **Kontekst paneli:** til · mavzu · daraja; rejim; o'ngda progress foizi yoki (vaqtli rejimda) taymer. Jonli WPM/accuracy standart yashirin (sozlama).
- **Focus mode:** birinchi belgi bosilgach kontekst paneli va yorliqlar 30% shaffoflikka o'tadi; sichqoncha harakatida 2 s ga qaytadi.
- **Fokus yo'q holati:** muharrir ustida xira qatlam va "Yozish uchun bosing yoki istalgan klavishni bosing".
- **Pauza:** muharrir xiralashadi (blur 4 px — kodni ko'rib tayyorlanishni to'sadi), markazda "Pauza — davom ettirish uchun istalgan klavish".
- **Ogohlantirish lentalari** (muharrir tepasida, bittasi): Caps Lock yoqiq · klaviatura tili lotin emas · aloqa yo'q (natija keyin yuboriladi).
- **Natija ko'rinishi** (shu sahifada, muharrir o'rnida): asosiy 4 raqam (WPM, accuracy, vaqt, xatolar) → ikkilamchi (raw WPM, CPM, consistency, backspace) → WPM grafigi → zaif belgilar va eng sekin qatorlar → mukofotlar (XP, level, streak, rekord, achievement) → Coach maslahati → tugmalar: Keyingisi (asosiy), Qayta, Replay, Mavzuga qaytish.
- `REJECTED` natija: metrikalar ko'rsatilmaydi; sabab (inson tilida) va "Qayta urinish".

Belgi holatlari va muharrir tafsilotlari — `09-code-editor.md`.

## 5. Sahifalar spetsifikatsiyasi

Har sahifa: bloklar (yuqoridan pastga), sahifaga xos holatlar, asosiy harakat. Umumiy holatlar (yuklanish/bo'sh/xato) §2 komponentlari bilan.

### 5.1 Landing (`/{locale}`)

- Hero: sarlavha (1 qator), kichik izoh, **ishlaydigan typing demo** (til tanlagich: 1-reliz tillari; 30 s; avtofokus desktop'da).
- Demo natijasi: WPM, accuracy, 3 ta eng sekin belgi + "Natijani saqlash va davom etish" (→ `/register`).
- 3 afzallik (til bo'yicha mashq · zaif belgi tahlili · reyting va Coach).
- Tillar to'ri (16 til; nashr qilinmaganlari "tez orada").
- Bugungi top-10 (R2 dan; R1 da blok yo'q).
- Footer: huquqiy havolalar, til almashtirgich, tema.
- Holatlar: demo uchun server javob bermasa — "Demo hozir ishlamayapti" + ro'yxatdan o'tish tugmasi (soxta natija yo'q).

### 5.2 Login / Register / parol sahifalari

- Karta: GitHub, Google tugmalari → ajratgich → forma.
- Register: email, username (jonli band/bo'shligi, 400 ms debounce), parol (kuch indikatori, talablar ro'yxati), shartlarga rozilik havolasi.
- Xatolar maydon ostida; umumiy xato karta tepasida; `429` da qolgan soniya ko'rsatiladi.
- "Email yuborildi" ekrani: qayta yuborish (60 s taymer).
- Reset: token yaroqsiz/muddati o'tgan → aniq xabar + "yangi havola so'rash".

### 5.3 Onboarding

Qadam indikatori (1/3); `04` §1.2. Placement'da typing ekrani to'liq focus rejimida; qadamlar orasida natija mini-kartasi.

### 5.4 Til tanlash (`/languages`)

- "Mening tillarim": `LanguageCard` (nom, rank, hozirgi WPM, progress halqasi, "asosiy" belgisi; menyu: asosiy qilish, olib tashlash).
- "Mavjud tillar": qo'shish tugmasi bilan; placement taklifi modalda.
- "Tez orada": xira, nofaol.
- Holatlar: oxirgi tilni olib tashlash — tugma nofaol + tooltip; asosiyni olib tashlash — avval boshqasini asosiy qilish so'raladi.

### 5.5 Dashboard

2 ustunli (desktop), 1 ustunli (mobile). Chapda: Davom ettirish kartasi (katta, asosiy tugma) → Coach rejasi (3 vazifa, R2) → til kartochkalari. O'ngda: streak + XP/level → kunlik challenge (R2) → oxirgi 5 sessiya. Holatlar: `FR-UD-8`; streak xavf ostida lentasi.

### 5.6 Typing Test (`/test`) va sessiya sahifalari

§4. `/test` — sozlash qatori (til, davomiylik segmentlari) + muharrir; mehmon uchun natijada ro'yxatdan o'tish taklifi.

### 5.7–5.11 Til Practice (`/practice/:lang`) — SQL, PL/SQL, Java, Python va boshqalar

- Sarlavha: til nomi, rank, hozirgi WPM, progress; til almashtirgich (foydalanuvchi tillari).
- Rejimlar qatori (7 karta): nom, 1 qator izoh, holat (mavjud / "R2" emas — nashr qilinmagan rejim ko'rsatilmaydi / nofaol + sabab).
- Mavzular ro'yxati: `TopicRow` — tartib raqami, nom (lokal), `LevelDots` (4 nuqta: qulf, bo'sh, jarayonda, tugagan, mastered), tugatilgan/jami.
- Mavzu sahifasi: izoh; daraja tablari (qulflangan tabda shart matni); mashqlar jadvali (sarlavha, uzunlik, eng yaxshi WPM, accuracy, holat); "Boshlash" keyingi tugatilmaganini ochadi.
- SQL va PL/SQL sahifalari tuzilishi bir xil; farq — kontent va belgi turkumlari. PL/SQL sarlavhasida "Oracle" belgisi.

### 5.12 Tarix

Filtrlar (til, rejim, sana oralig'i, holat); jadval (sana, til, rejim, mavzu, WPM, accuracy, vaqt, XP, holat belgisi); cheksiz scroll (cursor). Tafsilot: metrikalar, grafik, xatolar belgilangan kod, replay boshqaruvi; keylog muddati o'tgan bo'lsa replay o'rniga izoh.

### 5.13 Analytics

Til tanlagich ("Hammasi" + tillar), davr segmentlari, rejim filtri → KPI qatori (9 ko'rsatkich, `FR-AN-1`) → WPM va accuracy grafigi (2 o'q) → tillar taqqoslash (gorizontal ustunlar) → klaviatura heatmap (rejim: xato / tezlik / barmoq) → belgi turkumlari jadvali → chalkashlik juftliklari → faollik kalendari. < 5 sessiya: har blok o'rnida `EmptyState`.

### 5.14 Achievements

Umumiy progress (ochilgan/jami), kategoriya tablari, kartochkalar: ochilgan (rangli, sana), jarayonda (progress chizig'i), qulflangan (xira, shart ko'rinadi; "yashirin" achievement'lar — `???`).

### 5.15 Leaderboard

Filtr qatori (til, rejim/davomiylik, davr, ko'rinish) — URL parametrlarida; top-3 podium; jadval (o'rin, avatar+username, mamlakat bayrog'i+nomi, WPM, accuracy, sana); pastda yopishgan "Sizning o'rningiz" qatori; reytingga kirmagan bo'lsa — sabab va harakat (masalan "Emailni tasdiqlang").

### 5.16 Profil

Ommaviy: avatar, username, level, streak, qo'shilgan sana; tillar (rank, rekord); achievement vitrinasi (6 ta); faollik kalendari; follow tugmasi (R3). Yopiq profil: "Bu profil yopiq". Tahrirlash: forma + avatar yuklash (kesish, oldindan ko'rish).

### 5.17 Sozlamalar

| Bo'lim | Sozlamalar (kalit → qiymatlar, standart) |
| --- | --- |
| Ko'rinish | `locale` (uzl/uzc/ru/en) · `theme` (dark/light/system, **dark**) · `highContrast` (false) · `reducedMotion` (system/on/off, **system**) |
| Typing | `strictIndent` (false) · `practiceStopOnError` (true) · `showLiveStats` (false) · `keySound` (off/click/typewriter, **off**) · `soundVolume` (0–100, 50) · `blindMode` (false — xatolar ko'rsatilmaydi, natijada ko'rinadi) |
| Muharrir | `fontFamily` (**JetBrains Mono**) · `fontSize` (14–24, **18**) · `lineHeight` (1.4–2.0, **1.6**) · `caretStyle` (line/block/underline, **line**) · `smoothCaret` (true) · `ligatures` (false) · `lineNumbers` (true) · `showInvisibles` (true) · `bracketMatching` (true) |
| Klaviatura | `layout` (ansi-us/iso-uk/dvorak/colemak, **ansi-us**) · `showVirtualKeyboard` (auto/always/never, **auto**) · `fingerGuide` (true) |
| Akkaunt | email, parol, OAuth bog'lanishlari, 2FA, faol kirishlar |
| Maxfiylik | `profilePublic` (true) · `showOnLeaderboard` (true) · `coachEnabled` (true) · eksport · o'chirish (xavfli zona) |
| Bildirishnomalar | `emailStreakReminder` (false) · `emailWeeklyReport` (false) · `emailAchievements` (false) · `emailCompetitions` (false) |

O'zgarish darhol qo'llanadi va 500 ms debounce bilan saqlanadi ("Saqlandi" belgisi). Shrift/kursor o'zgarishi jonli preview muharririda ko'rinadi.

### 5.18 Admin

- Bosh sahifa: KPI kartalari, ko'rib chiqish navbati soni, shubhali sessiyalar soni, qamrov ogohlantirishlari.
- Mashqlar: filtrli jadval (til, mavzu, daraja, holat, muallif, teg); mashq muharriri — chapda `SnippetEditor`, o'ngda: validatsiya natijalari (qator:ustun bilan, bosilsa o'sha joyga o'tadi), hisoblangan metrikalar, lokal tablari (izoh), "Typing preview" tugmasi; pastda holat tugmalari.
- Import: fayl tanlash → validatsiya hisoboti jadvali (qabul qilindi / rad etildi + sabab) → tasdiqlash.
- Qamrov: til tanlagich; mavzu × daraja matritsasi (hujayra rangi: < 5 qizil, 5–9 sariq, ≥ 10 yashil); lokal to'liqligi ustunlari.
- Shubhali sessiya: metrikalar, shubha sabablari, intervallar gistogrammasi, replay, `CLEAR`/`CONFIRM`.
- Barcha destruktiv amallar tasdiq modalida sabab maydoni bilan (audit uchun).

## 6. Responsive

| Breakpoint | Kenglik | Layout | Typing |
| --- | --- | --- | --- |
| `xs` | 320–479 | 1 ustun; pastki tab bar; jadvallar karta ro'yxati | Lite (R3) yoki tashqi klaviatura; kod 14 px; virtual klaviatura yo'q |
| `sm` | 480–767 | `xs` kabi, kengroq kartalar | `xs` kabi |
| `md` | 768–1279 | Yig'ilgan yon panel (64 px); 2 ustun | To'liq; 16 px; virtual klaviatura kichraytirilgan |
| `lg` | 1280–1919 | To'liq yon panel | To'liq; 18 px |
| `xl` | 1920–2559 | Kontent maks. 1200 px, markazda | To'liq |
| `2xl` (4K) | ≥ 2560 | Kontent maks. 1440 px; asosiy `rem` 18 px ga ko'tariladi | Kod o'lchami ×1.25 |

Qoidalar: gorizontal scroll sahifa darajasida hech qachon yo'q (320 px da ham); typing muharririda uzun qator bo'lmaydi (≤ 80 belgi), tor ekranda muharrir ichida gorizontal scroll joriy ustunni kuzatadi; bosish maydonlari ≥ 44 px (`xs`–`md`); modallar `xs` da to'liq ekran.

## 7. Accessibility

- Klaviatura: barcha interaktiv element `Tab` bilan; mantiqiy tartib; skip-link ("Kontentga o'tish").
- Typing maydonida `Tab` ushlanadi → `Esc` fokusni bo'shatadi (WCAG 2.1.2); bu yorliq doim ko'rinadi.
- Fokus halqasi faqat `:focus-visible` da, doim ko'rinadi.
- Holat faqat rang bilan emas: xato — tagchiziq; daraja holati — ikonka; reyting o'zgarishi — strelka.
- Ekran o'quvchi: sahifa sarlavhasi route o'zgarganda e'lon qilinadi; natija `aria-live="polite"`; yozish paytida har belgi e'lon qilinmaydi; grafiklar uchun jadval ko'rinishidagi muqobil (`<details>`).
- Formalar: label, xato matni bog'langan; majburiy maydon `aria-required`.
- `lang` atributi lokalga mos; kod bloklari `lang="en"` va `translate="no"`.

## 8. Klaviatura yorliqlari

| Yorliq | Qayerda | Amal |
| --- | --- | --- |
| `Tab` + `Enter` | Typing, natija | Qayta boshlash |
| `Esc` | Typing | Pauza (stop-on-error rejimlar) / fokusni bo'shatish; ikkinchi marta — chiqish tasdig'i |
| `Ctrl` + `→` | Natija | Keyingi mashq |
| `Ctrl` + `←` | Natija | Mavzuga qaytish |
| `Ctrl` + `Enter` | Practice typing | Erta tugatish (`finishEarly`) — tasdiq bilan |
| `Ctrl` + Backspace | Typing | Joriy so'zni o'chirish |
| `Ctrl` + `K` | Hamma joyda (typing'dan tashqari) | Buyruqlar palitrasi |
| `Ctrl` + `Shift` + `L` | Hamma joyda | Tema |
| `G` → `D`/`P`/`A`/`L`/`C` | Typing'dan tashqari | Dashboard / Practice / Analytics / Leaderboard / Coach |
| `?` | Typing'dan tashqari | Yorliqlar oynasi |

macOS: `Ctrl` → `Cmd` (Backspace uchun `Option`). Brauzer band qilgan kombinatsiyalar ishlatilmaydi.

## 9. Mikro-matn qoidalari

- Tugma — fe'l ("Boshlash", "Saqlash"), holat emas.
- Xato xabari: nima bo'ldi + nima qilish kerak; texnik kod yo'q (faqat `traceId` nusxalash).
- Raqamlar lokal formatida; WPM/CPM/XP qisqartmalari tarjima qilinmaydi.
- Bo'sh holat hech qachon ayblamaydi: "Hali sessiya yo'q. Birinchisini boshlang."
