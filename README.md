# DevType — Developer Typing Trainer

> Holat: **faqat spetsifikatsiya**. Bu repozitoriyda hali source code yo'q. `docs/` katalogi — loyihani noldan qurish uchun yagona texnik manba (Single Source of Truth).

DevType (ishchi nom) — dasturchi o'zi ishlatadigan dasturlash tilida haqiqiy kod yozib, tezlik, aniqlik va maxsus belgilar mahoratini oshiradigan veb-platforma. 16 til, SQL va Oracle PL/SQL alohida yo'nalish, sayt 4 tilda (`uzl`, `uzc`, `ru`, `en`).

## Hujjatlarni o'qish tartibi

Claude Code uchun ish qoidalari: [CLAUDE.md](CLAUDE.md) — kod yozishdan oldin o'qiladi.

Implementatsiya qiluvchi (inson yoki Claude Code) hujjatlarni shu tartibda o'qiydi. Har bir hujjat mustaqil, lekin konstantalar va ID'lar hamma joyda bir xil.

| # | Hujjat | Nima beradi |
| --- | --- | --- |
| 00 | [docs/00-conventions.md](docs/00-conventions.md) | Atamalar, ID tizimi, kanonik konstantalar jadvali, hujjatlar ustuvorligi |
| 01 | [docs/01-prd.md](docs/01-prd.md) | Mahsulot talablari: muammo, auditoriya, qamrov, metrikalar, NFR |
| 02 | [docs/02-functional-requirements.md](docs/02-functional-requirements.md) | 23 modulning funksional talablari (FR ID bilan) |
| 03 | [docs/03-business-logic.md](docs/03-business-logic.md) | Har modulning algoritmi, holat mashinalari, psevdokod |
| 04 | [docs/04-user-journey-sitemap.md](docs/04-user-journey-sitemap.md) | Foydalanuvchi yo'li, to'liq sitemap, route'lar |
| 05 | [docs/05-ui-ux-design-system.md](docs/05-ui-ux-design-system.md) | Design system, barcha sahifalar spetsifikatsiyasi |
| 06 | [docs/06-curriculum.md](docs/06-curriculum.md) | 16 til o'quv dasturi; SQL va Oracle PL/SQL batafsil |
| 07 | [docs/07-typing-engine.md](docs/07-typing-engine.md) | Typing engine: holat modeli, qoidalar, formulalar, keylog |
| 08 | [docs/08-special-characters-virtual-keyboard.md](docs/08-special-characters-virtual-keyboard.md) | Maxsus belgilar mashg'uloti, virtual klaviatura |
| 09 | [docs/09-code-editor.md](docs/09-code-editor.md) | Developer Code Editor (typing yuzasi) texnik talablari |
| 10 | [docs/10-analytics-ai-coach.md](docs/10-analytics-ai-coach.md) | Analytics agregatlari, AI Personal Coach algoritmlari |
| 11 | [docs/11-gamification-leaderboard.md](docs/11-gamification-leaderboard.md) | XP, level, streak, achievement, challenge, reyting qoidalari |
| 12 | [docs/12-database.md](docs/12-database.md) | ERD, to'liq DDL, indekslar, migratsiya strategiyasi |
| 13 | [docs/13-backend-architecture.md](docs/13-backend-architecture.md) | Spring Boot modular monolith, modullar, oqimlar |
| 14 | [docs/14-api-specification.md](docs/14-api-specification.md) | REST API: endpoint'lar, kontraktlar, xato kodlari |
| 15 | [docs/15-frontend-architecture.md](docs/15-frontend-architecture.md) | React/TS tuzilmasi, komponentlar, state |
| 16 | [docs/16-security.md](docs/16-security.md) | Authentication, authorization, xavfsizlik talablari |
| 17 | [docs/17-performance-scalability.md](docs/17-performance-scalability.md) | Unumdorlik byudjetlari, masshtablash |
| 18 | [docs/18-testing-acceptance.md](docs/18-testing-acceptance.md) | Test strategiyasi, 23 modul uchun Acceptance Criteria |
| 19 | [docs/19-devops-deployment.md](docs/19-devops-deployment.md) | Docker, CI/CD, muhitlar, monitoring, backup |
| 20 | [docs/20-roadmap.md](docs/20-roadmap.md) | Bosqichlar, implementatsiya tartibi, xavflar |
| 21 | [docs/21-production-readiness-checklist.md](docs/21-production-readiness-checklist.md) | Production'ga chiqish nazorat ro'yxati |
| 22 | [docs/22-i18n.md](docs/22-i18n.md) | Lokalizatsiya: uzl, uzc, ru, en |
| 23 | [docs/23-decisions-open-questions.md](docs/23-decisions-open-questions.md) | Qarorlar jurnali, ziddiyatlar yechimi, OPEN QUESTION ro'yxati, kelajak funksiyalar |
| 24 | [docs/24-documentation-audit.md](docs/24-documentation-audit.md) | Hujjatlashtirish auditi: qamrov va izchillik tekshiruvi |

## 23 modul → hujjat xaritasi

| # | Modul | FR prefiksi | Asosiy hujjat(lar) |
| --- | --- | --- | --- |
| 1 | User Management | `FR-UM` | 02, 14, 16 |
| 2 | Programming Languages | `FR-PL` | 02, 06, 12 |
| 3 | Language Learning Structure | `FR-LS` | 02, 03, 06 |
| 4 | SQL Training | `FR-SQL` | 02, 06 |
| 5 | PL/SQL Training | `FR-PLS` | 02, 06 |
| 6 | Typing Engine | `FR-TE` | 02, 07 |
| 7 | Training Modes | `FR-TM` | 02, 03 |
| 8 | Special Characters Training | `FR-SC` | 02, 08 |
| 9 | Virtual Keyboard | `FR-VK` | 02, 08 |
| 10 | Developer Code Editor | `FR-CE` | 02, 09 |
| 11 | Analytics Dashboard | `FR-AN` | 02, 10 |
| 12 | AI Personal Coach | `FR-AI` | 02, 10 |
| 13 | Gamification | `FR-GM` | 02, 11 |
| 14 | Exercise Management | `FR-EM` | 02, 03, 14 |
| 15 | User Dashboard | `FR-UD` | 02, 05 |
| 16 | UI/UX Design | `FR-UX` | 02, 05 |
| 17 | Admin Panel | `FR-AD` | 02, 05, 14 |
| 18 | Database & Backend | `FR-BE` | 12, 13, 14 |
| 19 | Security & Performance | `FR-SP` | 16, 17 |
| 20 | Testing & Quality Assurance | `FR-QA` | 18 |
| 21 | Platform Quality Standards | `FR-QS` | 02, 21 |
| 22 | Future Features | `FR-FF` | 23 |
| 23 | Acceptance Criteria | `AC-*` | 18 |

## Texnologiyalar (qat'iy)

- **Frontend:** React 18, TypeScript (strict), Vite, Tailwind CSS, TanStack Query, Zustand, React Router 6, i18next, Shiki, Recharts.
- **Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Spring Modulith, Flyway, REST; WebSocket faqat jonli poygalarda (3-reliz).
- **Database:** PostgreSQL 16.
- **Infra:** Docker, Docker Compose, Nginx, GitHub Actions, Prometheus, Grafana, Loki.

## Implementatsiyani boshlash

1. `docs/00-conventions.md` va `docs/23-decisions-open-questions.md` ni o'qing — ochiq savollar uchun standart qiymatlar belgilangan, ularni kutmasdan ishlash mumkin.
2. `docs/20-roadmap.md` dagi 0-bosqichdan boshlang.
3. Har bosqich oxirida `docs/18-testing-acceptance.md` dagi tegishli AC'lar va bosqich mezoni (gate) bajarilishi shart.

Ziddiyat topilsa: `docs/00-conventions.md` dagi ustuvorlik qoidasi bo'yicha hal qilinadi va `docs/23-decisions-open-questions.md` ga yoziladi.
