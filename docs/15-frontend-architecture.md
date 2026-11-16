# 15. Frontend Architecture

React 18 + TypeScript (`strict`, `noUncheckedIndexedAccess`) + Vite + Tailwind CSS. Feature-based tuzilma. Typing engine — React'dan mustaqil sof TypeScript moduli.

## 1. Texnologiyalar va sabablar

| Masala | Tanlov | Sabab |
| --- | --- | --- |
| Build | Vite | Tez dev server, ESM, kod bo'lish |
| Routing | React Router 6 (data router), route darajasida `lazy` | Admin, analytics, coach alohida chunk |
| Server state | TanStack Query v5 | Kesh, qayta so'rov, dedup, optimistic update |
| Klient state | Zustand (auth, sozlamalar, UI) | Minimal; Redux bu hajmda ortiqcha |
| Typing state | `engine` + `useSyncExternalStore`; global store'dan tashqarida | Har klavishda global yangilanish keraksiz render beradi |
| Stil | Tailwind + CSS o'zgaruvchilari (tokenlar) | Tema almashtirish qayta render'siz |
| Primitivlar | Radix UI | A11y tayyor (dialog, menu, tabs, tooltip) |
| Formalar | React Hook Form + Zod | Sxema asosida validatsiya; xato xabarlari i18n kalitlari |
| API klient | `openapi-typescript` (tiplar) + `openapi-fetch` | Tiplar OpenAPI'dan generatsiya qilinadi, qo'lda yozilmaydi |
| i18n | i18next + `i18next-icu` | 4 lokal, ko'plik shakllari (`22`) |
| Highlight | Shiki (JS engine) | `09` §3 |
| Grafiklar | Recharts; heatmap va kalendar — o'z SVG komponentlari | Yengil, deklarativ |
| Admin muharrir | CodeMirror 6 | Faqat admin chunk'ida |
| Prerender | `vite-ssg` (yoki ekvivalent) — faqat ommaviy route'lar | SEO (`D-16`) |
| Test | Vitest, Testing Library, Playwright, axe-core | `18` |
| Xato kuzatuvi | Sentry SDK (yoki GlitchTip) | `19` |

## 2. Katalog tuzilmasi

```text
frontend/
  index.html
  vite.config.ts, tailwind.config.ts, tsconfig.json, .eslintrc.cjs
  public/                      # favicon, robots.txt, shriftlar (woff2)
  src/
    main.tsx                   # kirish nuqtasi
    app/
      router.tsx               # route daraxti, guard'lar, lazy import
      providers.tsx            # QueryClient, i18n, Theme, Toast
      layouts/                 # PublicLayout, AppLayout, TypingLayout, AuthLayout, AdminLayout
      guards/                  # RequireAuth, RequireOnboarded, RequireRole, GuestOnly
      error/                   # ErrorBoundary, NotFoundPage, ErrorPage, OfflinePage
    engine/                    # SOF TS — React/DOM/Date import qilmaydi (07)
      types.ts  constants.ts  prepare.ts  reducer.ts  metrics.ts  stats.ts
      keylog.ts  categories.ts  index.ts
      __tests__/               # unit + property + umumiy vektorlar
    features/
      auth/            pages/ components/ api.ts hooks.ts schemas.ts
      onboarding/
      languages/
      dashboard/
      typing/          TypingPage, ResultView, useTypingSession, inputAdapter.ts, resultQueue.ts (IndexedDB)
      editor/          TypingEditor/ (09), ReplayPlayer/, highlight/ (Shiki yuklovchi, mavzular)
      keyboard/        VirtualKeyboard/, layouts/*.json, charMap.ts, layoutDetector.ts
      practice/        LanguagePracticePage, TopicPage, mode start pages
      history/
      analytics/       charts/, KeyboardHeatmap, ActivityCalendar
      coach/
      gamification/    achievements/, leaderboard/, competitions/, XpBar, StreakBadge, RankBadge
      profile/
      settings/
      landing/         # prerender qilinadi
      admin/           exercises/ (SnippetEditor), languages/, users/, sessions/, coverage/, …
    shared/
      api/             client.ts (fetch + auth + refresh), schema.d.ts (generatsiya), queryKeys.ts, problem.ts
      ui/              Button, Input, Select, Dialog, Toast, Table, Tabs, Skeleton, EmptyState, …
      hooks/           useMediaQuery, useHotkeys, useDebounce, usePageTitle
      lib/             format.ts (Intl), date.ts, storage.ts, uuid.ts, cn.ts
      stores/          authStore.ts, settingsStore.ts, uiStore.ts
      i18n/            index.ts, locales/{en,uzl,uzc,ru}/*.json, translit.ts
    styles/            tokens.css, base.css, syntax.css
  e2e/                 # Playwright
  scripts/             # i18n-check, translit-generate, openapi-generate
```

Qoidalar (ESLint bilan majburlanadi):

- `features/a` `features/b` ning ichki fayllarini import qilmaydi — faqat `features/b/index.ts` (ommaviy API) orqali. `eslint-plugin-boundaries`.
- `engine/` → hech narsa (faqat o'zi). `shared/` → `features/` ni import qilmaydi.
- JSX ichida literal matn taqiqlangan (`react/jsx-no-literals`), istisno: belgilar va raqamlar.
- `any` taqiqlangan; `// eslint-disable` sabab izohi bilan.

## 3. Komponentlar ierarxiyasi (asosiy sahifalar)

```text
App
└─ Providers (Query, I18n, Theme, Toast, Hotkeys)
   └─ Router
      ├─ PublicLayout ─ LandingPage (DemoTyping) · LanguagesCatalogPage · LeaderboardPage · LegalPages
      ├─ AuthLayout ─ LoginPage · RegisterPage · VerifyEmailPage · ForgotPasswordPage · ResetPasswordPage
      ├─ AppLayout (Sidebar, Header[XpBar, StreakBadge, UserMenu], CommandPalette)
      │   ├─ DashboardPage ─ ResumeCard · CoachPlanCard · LanguageCard[] · StreakCard · ChallengeCard · RecentSessions
      │   ├─ LanguagesPage
      │   ├─ LanguagePracticePage ─ ModeCards · TopicRow[] (LevelDots)
      │   ├─ TopicPage ─ LevelTabs · ExerciseTable
      │   ├─ HistoryPage · SessionDetailPage (ReplayPlayer)
      │   ├─ AnalyticsPage ─ KpiRow · WpmChart · LanguageCompareChart · KeyboardHeatmap · CategoryTable · ConfusionList · ActivityCalendar
      │   ├─ CoachPage ─ PlanList · GoalCard[] · WeeklyReport[]
      │   ├─ AchievementsPage · CompetitionsPage · CompetitionPage
      │   ├─ ProfileEditPage · SettingsPage (7 bo'lim)
      │   └─ AdminLayout ─ AdminHome · ExercisesPage · ExerciseEditorPage (SnippetEditor) · ImportPage · CoveragePage · UsersPage · FlaggedSessionsPage · …
      └─ TypingLayout
          └─ TypingPage
              ├─ ContextBar
              ├─ WarningBanner (CapsLock | layout | offline)
              ├─ TypingEditor (HiddenInput, Gutter, CodeViewport[CodeLine…], Caret, Overlay)
              ├─ VirtualKeyboard (shartli)
              ├─ ShortcutHints
              └─ ResultView (tugagach) ─ MetricsGrid · WpmChart · WeakTokens · SlowLines · RewardsPanel · CoachTipCard · ResultActions
```

## 4. Typing sessiyasi oqimi (`useTypingSession`)

```text
1. mount: POST /sessions (yoki route state'dan tayyor sessiya)         → loading skeleton
2. shrift tayyor + Shiki tokenizatsiya (parallel)                       → muharrir ko'rinadi
3. engine = createEngine(config); inputAdapter HiddenInput'ga ulanadi   (07 §4)
4. har keydown → adapter → KeyEvent → engine.dispatch → subscribe → faqat o'zgargan qator render
5. SPEED_TEST: seq oxirgi yuklangan snippetga yetganda POST /sessions/{id}/next
6. phase == finished → keylog = engine.exportKeylog()
7. resultQueue.enqueue({sessionId, body}) → POST /complete
      ok        → ResultView (server metrikalari)        # klient metrikalari faqat kutish paytida ko'rsatiladi,
      tarmoq xatosi → "Natija saqlanmoqda…" + klient metrikalari "dastlabki" belgisi bilan; fon qayta urinish
      422       → rad etilgan natija ko'rinishi
8. unmount / route o'zgarishi phase==running da → POST /abandon (sendBeacon)
```

- **Natija ko'rsatish qoidasi:** server javobi kelguncha klient hisoblagan metrikalar "dastlabki" yorlig'i bilan ko'rsatiladi; server javobi kelgach almashtiriladi (odatda bir xil — NFR-11). XP, streak, rekord faqat serverdan.
- `resultQueue`: IndexedDB (`devtype-results`); ilova yuklanganda va `online` hodisasida navbat bo'shatiladi; 24 soatdan eski yozuv o'chiriladi va foydalanuvchiga "natija saqlanmadi" xabari.
- Sahifani yopish/yangilashda (`beforeunload`) `running` bo'lsa brauzerning standart ogohlantirishi.

## 5. Ma'lumot qatlami

- `shared/api/client.ts`: `openapi-fetch` + interceptor'lar: `Authorization`, `X-Guest-Token`, `Accept-Language`, `X-Engine-Version`, `X-Request-Id`.
- **401 ishlovi:** bitta umumiy refresh promise (parallel so'rovlar navbatga turadi) → muvaffaqiyatli bo'lsa asl so'rov takrorlanadi; muvaffaqiyatsiz → `authStore.logout()` + `/login?next=`.
- Access token faqat xotirada (Zustand, persist yo'q). Sahifa yuklanganda `POST /auth/refresh` (cookie) orqali tiklanadi; bu vaqtda `AppLayout` skeleton ko'rsatadi.
- Xatolar: `ProblemError` klassi (`code`, `status`, `meta`, `traceId`); global `QueryCache.onError` → kutilmagan xatolar toast; kutilgan kodlar komponentda ishlanadi.
- Query kalitlari `queryKeys.ts` da markazlashgan. `staleTime`: katalog 5 daq; dashboard 30 s; analytics 60 s; reyting 30 s; `me` 5 daq.
- Mutatsiyadan keyin invalidatsiya: `complete` → `dashboard`, `gamification/me`, `languages/{slug}/topics`, `analytics/*`, `sessions`.
- `GET /meta` ilova boshida — feature flag'lar va engine versiyasi; `426` da "Yangi versiya mavjud — sahifani yangilang" modal.

## 6. State

| Store | Mazmun | Persist |
| --- | --- | --- |
| `authStore` | `accessToken`, `user`, `guestToken` | faqat `guestToken` (localStorage) |
| `settingsStore` | Server sozlamalari nusxasi + lokal override (mehmon) | localStorage (mehmon: tema, lokal); user — serverdan |
| `uiStore` | Sidebar holati, palitra ochiqmi | localStorage (sidebar) |

Tema va lokal sahifa chizilishidan **oldin** qo'llanadi: `index.html` dagi kichik inline skript `localStorage` dan `data-theme` va `lang` ni o'rnatadi (FOUC yo'q). Bu yagona inline skript — CSP'da hash bilan ruxsat etiladi.

## 7. Routing va kod bo'lish

| Chunk | Tarkib | Byudjet (gzip) |
| --- | --- | --- |
| `entry` | React, router, query, i18n yadrosi, shared/ui asosi, auth | ≤ 150 KB |
| `typing` | engine, TypingEditor, VirtualKeyboard, Shiki yadro | ≤ 90 KB (+ grammatika 5–40 KB/til, lazy) |
| `analytics` | Recharts, heatmap | ≤ 110 KB |
| `admin` | CodeMirror 6, admin sahifalar | ≤ 250 KB (faqat admin) |
| har lokal namespace | JSON | ≤ 15 KB |

CI: `vite-bundle-visualizer` + `size-limit` — byudjet oshsa build to'xtaydi.

## 8. Prerender va SEO

- Prerender qilinadigan route'lar: `/{locale}`, `/{locale}/languages`, `/{locale}/languages/:lang` (nashr qilingan tillar ro'yxati build paytida API'dan), `/{locale}/leaderboard` (qobiq + top-10 build paytidagi surat; klientda yangilanadi), `/{locale}/about`, `/privacy`, `/terms`.
- Har sahifada: `<title>`, `meta description`, kanonik URL, `hreflang` × 4 + `x-default`, Open Graph, JSON-LD (`WebApplication`).
- `sitemap.xml` va `robots.txt` build paytida generatsiya qilinadi; ilova route'lari (`/dashboard`, …) `noindex`.
- Kontent (tillar) o'zgarganda prerender — keyingi deploy'da; kunlik rejalashtirilgan rebuild (CI cron).

## 9. Unumdorlik qoidalari

`17` §2 da byudjetlar. Amaliy qoidalar:

- Klavish ishlovchisida: DOM o'qish yo'q, `setState` yo'q (engine tashqi store), tarmoq yo'q.
- `CodeLine` — `memo`; props primitiv (satrlar), obyekt yaratilmaydi.
- Jonli statistika 500 ms intervalda, alohida komponent.
- Rasmlar: avatar `loading="lazy"`, `width/height` berilgan; SVG ikonalar sprite.
- Shriftlar: `preload` faqat Inter 400/600 lotin va tanlangan kod shrifti 400.
- Recharts faqat ko'rinish zonasiga kirganda yuklanadi (`IntersectionObserver`).

## 10. Xatolar va chekka holatlar

| Holat | Xulq |
| --- | --- |
| Render xatosi | Route darajasidagi `ErrorBoundary` → xato sahifasi + "Qayta yuklash"; Sentry'ga yuboriladi |
| Chunk yuklanmadi (deploy'dan keyin eski HTML) | `import()` xatosi → bir marta avtomatik `location.reload()`; takrorlansa xato sahifasi |
| Offline | Banner; o'qish so'rovlari keshdan; typing davom etadi |
| Sessiya muddati tugadi (refresh ham) | Typing paytida: natija navbatga, login'dan keyin yuboriladi |
| `localStorage`/IndexedDB mavjud emas (private rejim) | Xotiradagi zaxira; navbat faqat sahifa hayoti davomida; ogohlantirish |
| JavaScript o'chiq | Prerender sahifalar o'qiladi; `<noscript>` xabari |
| Juda eski brauzer | `browserslist` dan tashqari — "Brauzeringizni yangilang" statik sahifa |

## 11. Buyruqlar (`package.json`)

`dev`, `build` (tsc + vite build + prerender), `preview`, `lint`, `typecheck`, `test` (Vitest), `test:coverage`, `e2e`, `e2e:ui`, `i18n:check`, `i18n:translit`, `api:generate`, `size`.
