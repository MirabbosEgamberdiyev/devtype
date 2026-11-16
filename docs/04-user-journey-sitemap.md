# 04. User Journey va Sitemap

## 1. User Journey

Asosiy tamoyil: foydalanuvchi ro'yxatdan o'tishdan **oldin** qiymatni his qiladi — landing sahifaning o'zida 30 soniyalik test ishlaydi.

### 1.1 Asosiy yo'l

| # | Bosqich | Foydalanuvchi | Tizim | Muvaffaqiyat belgisi |
| --- | --- | --- | --- | --- |
| 1 | Kashf etish | Landing'ga keladi, til tanlab demo'da yozadi | `POST /auth/guest` → `POST /sessions` (SPEED_TEST 30 s) → `complete` | Natija kartasi: WPM, accuracy, eng sekin 3 belgi |
| 2 | Ro'yxatdan o'tish | "Natijani saqlash" → GitHub/Google yoki email | `register` + `claim` | Mehmon sessiyasi akkauntda |
| 3 | Onboarding | 3 qadam (≤ 2 daqiqa) | Tillar saqlanadi; placement natijasi darajalarni ochadi | `onboarded=true` |
| 4 | Birinchi sessiya | Dashboard'da "Birinchi mashqni boshlash" | `BL-UD-1` | 1-kun ≥ 3 mashq (aktivatsiya) |
| 5 | Kundalik sikl (5–10 daq) | Dashboard → reja/challenge → mashq → natija → "keyingisi" | XP, streak, progress yangilanadi | Streak davom etadi |
| 6 | Haftalik sikl | Yakshanba hisobotini o'qiydi | Coach haftalik hisobot | Weakness Training boshlanadi |
| 7 | Chuqurlashish | Darajalarni tugatadi, ikkinchi tilni qo'shadi | Daraja ochiladi; rank beriladi | `COMPLETED` darajalar o'sadi |
| 8 | Raqobat | Speed Test → reyting, musobaqa | Reyting yangilanadi | Reytingda o'rin |

### 1.2 Onboarding qadamlari

1. **Tillar** (majburiy): ≥ 1 til, bittasi asosiy. "Davom etish" til tanlanmaguncha nofaol.
2. **Profil sozlamasi** (o'tkazib yuboriladi): tajriba (yangi / 1–3 yil / 3+ yil — faqat placement taklifi uchun, saqlanadi), klaviatura layouti, virtual klaviatura ko'rsatilsinmi.
3. **Placement test** (o'tkazib yuboriladi): asosiy tildan 3 snippet (`BL-LS-3`). O'tkazib yuborilsa — hammasi `BEGINNER` dan.

Onboarding tugamaguncha ilova route'lari `/onboarding` ga yo'naltiradi (`RequireOnboarded`). Sahifa yangilansa — qolgan qadamdan davom etadi (`users.onboarding_step`).

### 1.3 Mashq sikli (mikro-yo'l)

```text
Mavzu sahifasi ──Enter──► Typing (fokus avtomatik) ──oxirgi belgi──► Natija
      ▲                        │ Tab+Enter: qayta                      │
      │                        │ Esc: pauza / chiqish                  │ Ctrl+→ : keyingi mashq
      └──────── Ctrl+← ────────┴───────────────────────────────────────┘ Tab+Enter: qayta
```

"Keyingi mashq" — shu darajadagi keyingi tugatilmagan mashq; daraja tugagan bo'lsa natija ekranida "Daraja tugadi → keyingi daraja ochildi" va tugma keyingi darajaga olib boradi.

### 1.4 Tashlab ketish nuqtalari va yechimlar

| Nuqta | Xavf | Yechim |
| --- | --- | --- |
| Landing | Demo'ni topmaydi | Typing maydoni birinchi ekranda, avtofokus; "yozishni boshlang" ko'rsatmasi |
| Ro'yxat | Uzun forma | OAuth birinchi; email formasi 3 maydon |
| Onboarding | Majburiy qadamlar ko'p | Faqat 1-qadam majburiy |
| Birinchi natija | Past WPM ruhni tushiradi | Natija ekranida: "Kod WPM oddiy matn WPM'dan 30–40% past bo'lishi normal" (bir marta, birinchi 3 sessiyada) |
| Klaviatura tili | Kirill layoutida yozadi | Birinchi ASCII bo'lmagan belgida ogohlantirish, taymer boshlanmaydi |
| 3–7-kun | Streak uziladi | Freeze; ixtiyoriy email eslatma; dashboard ogohlantirishi |
| Plato | WPM o'smaydi | Coach: Accuracy/Weakness tavsiyasi, yangi daraja |
| Qulflangan daraja | Nima qilishni bilmaydi | Qulf ustida aniq shart: "Yana 2 ta mashqni ≥ 95% bilan tugating" |

### 1.5 Rol bo'yicha yo'llar

- **EDITOR:** login → 2FA → `/admin/exercises` → "Yangi mashq" → kod kiritish → jonli validatsiya → `IN_REVIEW` → boshqa editor `publish` → qamrov matritsasida ko'rinadi.
- **ADMIN:** `/admin/sessions/flagged` → replay → `CLEAR`/`CONFIRM`; `/admin/users` → bloklash; `/admin/languages` → tilni nashr qilish.

---

## 2. Sitemap

### 2.1 Route'lar

Ommaviy sahifalar til prefiksi bilan ham ochiladi: `/{locale}/…` (`uzl|uzc|ru|en`). Prefikssiz ommaviy URL aniqlangan lokalga `302` qiladi. Ilova (autentifikatsiyadan keyingi) sahifalari prefikssiz.

| # | Sahifa | Route | Kirish | Reliz |
| --- | --- | --- | --- | --- |
| 1 | Landing | `/{locale}` | Ommaviy (prerender) | R1 |
| 2 | Login | `/login` | Ommaviy (kirganlar `/dashboard` ga) | R1 |
| 2 | Register | `/register` | Ommaviy | R1 |
| 2 | Email tasdiqlash | `/verify-email?token=` | Ommaviy | R1 |
| 2 | Parolni unutdim / tiklash | `/forgot-password`, `/reset-password?token=` | Ommaviy | R1 |
| 2 | OAuth qaytish | `/oauth/callback` | Ommaviy | R1 |
| 3 | Onboarding | `/onboarding` | User | R1 |
| 4 | Til tanlash | `/languages` | User | R1 |
| 4 | Tillar katalogi (ommaviy) | `/{locale}/languages`, `/{locale}/languages/:lang` | Ommaviy (prerender) | R1 |
| 5 | Dashboard | `/dashboard` | User | R1 |
| 6 | Tezkor test | `/test?lang=&duration=` | Ommaviy + User | R1 |
| 6 | Faol sessiya | `/session/:sessionId` | Egasi (yoki mehmon egasi) | R1 |
| 6 | Natija | `/session/:sessionId/result` | Egasi | R1 |
| 7–11 | Til practice | `/practice/:lang` | User | R1 |
| 7–11 | Mavzu | `/practice/:lang/:topic` | User | R1 |
| 7–11 | Rejim start ekranlari | `/practice/:lang/special`, `/accuracy`, `/weakness`, `/sprint`, `/challenge` | User | R1/R2 |
| 12 | Tarix | `/history` | User | R1 |
| 12 | Sessiya tafsiloti + replay | `/history/:sessionId` | Egasi | R1 |
| 13 | Analytics | `/analytics`, `/analytics/:lang` | User | R1 |
| — | Coach | `/coach` (reja, maqsadlar, hisobotlar) | User | R2 |
| 14 | Achievements | `/achievements` | User | R2 |
| 15 | Leaderboard | `/{locale}/leaderboard?lang=&mode=&duration=&period=&scope=` | Ommaviy | R2 |
| 15 | Musobaqalar | `/competitions`, `/competitions/:id` | User | R2 |
| 15 | Jonli poyga | `/race/:roomId` | User | R3 |
| 16 | Ommaviy profil | `/{locale}/u/:username` | Ommaviy (profil ochiq bo'lsa) | R3 |
| 16 | Profilni tahrirlash | `/profile/edit` | User | R1 |
| 17 | Sozlamalar | `/settings/:section` — `appearance`, `typing`, `editor`, `keyboard`, `account`, `privacy`, `notifications` | User | R1 |
| 18 | Admin bosh sahifa | `/admin` | Editor, Admin | R1 |
| 18 | Admin: kontent | `/admin/languages`, `/admin/topics`, `/admin/exercises`, `/admin/exercises/:id`, `/admin/exercises/import`, `/admin/coverage` | Editor, Admin | R1 |
| 18 | Admin: foydalanuvchilar | `/admin/users`, `/admin/users/:id` | Admin | R1 |
| 18 | Admin: sessiyalar | `/admin/sessions/flagged`, `/admin/sessions/:id` | Admin | R2 |
| 18 | Admin: gamification | `/admin/achievements`, `/admin/challenge-templates`, `/admin/competitions` | Admin | R2 |
| 18 | Admin: shikoyatlar | `/admin/reports` | Editor, Admin | R2 |
| 18 | Admin: metrikalar, audit | `/admin/metrics`, `/admin/audit-log` | Admin | R1/R2 |
| — | Huquqiy | `/{locale}/privacy`, `/{locale}/terms` | Ommaviy (prerender) | R1 |
| — | Haqida | `/{locale}/about` | Ommaviy (prerender) | R1 |
| — | Xato sahifalari | `*` → 404; xato chegarasi → 500; `/offline` | — | R1 |

"7–11" — asl talabdagi SQL, PL/SQL, Java, Python va boshqa Practice sahifalari; ular bitta parametrli route bilan yopiladi (`D-8`). `/practice/sql` va `/practice/plsql` — ikki alohida til.

### 2.2 Navigatsiya tuzilmasi

**Asosiy navigatsiya (yon panel / pastki tab bar):** Dashboard · Practice · Analytics · Leaderboard · Profil.
**Ikkilamchi (yon panel pastida / profil menyusida):** Coach · Achievements · Tarix · Sozlamalar · Admin (rolga ko'ra) · Chiqish.
**Mobile tab bar (5):** Home (`/dashboard`) · Practice · Stats (`/analytics`) · Rank (`/leaderboard`) · Profile (menyu: Coach, Achievements, Tarix, Sozlamalar).
**Mehmon header:** Logo · Tillar · Reyting · Til almashtirgich · Tema · Kirish · Ro'yxatdan o'tish.

### 2.3 Route guard'lar

| Guard | Shart | Bajarilmasa |
| --- | --- | --- |
| `RequireAuth` | Access token bor yoki refresh muvaffaqiyatli | `/login?next=<path>` |
| `RequireOnboarded` | `user.onboarded` | `/onboarding` |
| `RequireRole(role)` | Rol mos | 404 sahifasi (admin mavjudligi oshkor qilinmaydi) |
| `RequireTwoFactor` | Admin route'larda 2FA o'tgan | `/settings/account#2fa` |
| `GuestOnly` | Kirmagan | `/dashboard` |
| `SessionOwner` | Sessiya foydalanuvchiga/mehmonga tegishli | 404 |

### 2.4 Yo'naltirishlar

- `/` → `/{aniqlangan-locale}`; kirgan foydalanuvchi uchun → `/dashboard`.
- Login'dan keyin → `next` (faqat nisbiy yo'l, boshqa domenga yo'naltirish taqiqlangan) yoki `/dashboard`.
- `/practice` → `/practice/{asosiy-til}`.
- `/analytics` — umumiy ko'rinish; til tanlanganda `/analytics/:lang`.
- Nashr qilinmagan yoki mavjud bo'lmagan `:lang` → 404.
