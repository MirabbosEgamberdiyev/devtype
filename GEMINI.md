# CLAUDE.md — implementatsiya qoidalari

Bu fayl Claude Code uchun. Loyiha spetsifikatsiyasi `docs/` da; kod yozishdan oldin tegishli hujjatni o'qi.

## Majburiy qoidalar

1. **Manba — `docs/`.** Hujjatda yozilmagan xulqni o'ylab topma. Bo'shliq topsang: `docs/23-decisions-open-questions.md` ga `OPEN QUESTION` qo'sh, standart qiymat taklif qil va shu qiymat bilan davom et.
2. **Konstantalar** faqat `docs/00-conventions.md` §4 dan olinadi. Kodda ular bitta joyda (`backend: com.devtype.shared.Constants` / modul konfiguratsiyasi, `frontend: src/engine/constants.ts`) e'lon qilinadi.
3. **Mock data taqiqlangan.** Production kodida hardcode qilingan natija, statistika, reyting, foydalanuvchi bo'lmaydi. Test fixture'lari faqat `src/test` (backend) va `*.test.ts(x)`, `e2e/` (frontend) ichida.
4. **Typing engine ikki joyda bir xil:** `frontend/src/engine` (TypeScript) va `backend .../training/domain/KeylogReplayer` (Java). Har o'zgarish `engine-vectors/*.json` test vektorlarini yangilaydi; ikkala tomon ham shu vektorlardan o'tishi shart.
5. **i18n:** JSX ichida literal matn yozilmaydi. Har yangi kalit 4 tilda (`en`, `uzl`, `uzc`, `ru`) qo'shiladi; `uzc` transliterator bilan generatsiya qilinadi.
6. **SQL ≠ PL/SQL.** Ular ikki alohida til (`sql`, `plsql`). PL/SQL kontenti faqat Oracle sintaksisi — PL/pgSQL konstruksiyalari (`$$`, `LANGUAGE plpgsql`, `RAISE NOTICE`, `RETURNS`) taqiqlangan.
7. **Bosqichma-bosqich:** `docs/20-roadmap.md` tartibi. Bosqich mezoni (gate) bajarilmaguncha keyingisiga o'tilmaydi.
8. **Har PR:** testlar yashil, `docs/18-testing-acceptance.md` dagi tegishli AC ID'lari PR tavsifida ko'rsatiladi.
9. **Destruktiv migratsiya** (ustun/jadval o'chirish) faqat expand → migrate → contract tartibida, alohida relizda.
10. Hujjat va kod farq qilsa — avval hujjat yangilanadi (qaror jurnali bilan), keyin kod.

## Repozitoriy tuzilmasi (maqsad)

```text
/
├── README.md
├── CLAUDE.md
├── docs/                 # spetsifikatsiya
├── backend/              # Spring Boot (Gradle Kotlin DSL)
├── frontend/             # React + Vite
├── engine-vectors/       # umumiy test vektorlari (JSON)
├── content/              # snippetlar manbasi (YAML), import orqali bazaga
├── deploy/               # docker-compose, nginx, monitoring
└── .github/workflows/    # CI/CD
```

## Buyruqlar (maqsad holat)

| Amal | Buyruq |
| --- | --- |
| Backend testlar | `cd backend && ./gradlew check` |
| Backend ishga tushirish | `cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'` |
| Frontend dev | `cd frontend && npm run dev` |
| Frontend testlar | `cd frontend && npm run test && npm run lint && npm run typecheck` |
| E2E | `cd frontend && npm run e2e` |
| Lokal infra | `docker compose -f deploy/docker-compose.local.yml up -d` |
| Kontent validatsiya | `cd content && npm run validate` |
| i18n tekshiruv | `cd frontend && npm run i18n:check` |
