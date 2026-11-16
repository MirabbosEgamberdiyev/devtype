# 21. Production Readiness Checklist

Production'ga chiqishdan oldin har band **dalil bilan** belgilanadi: CI havolasi, hisobot artefakti, skrinshot yoki imzo. Dalilsiz band — bajarilmagan. O'lchanmagan ko'rsatkich bajarildi deb belgilanmaydi.

Ustunlar: **Band** · **Dalil** (qanday isbotlanadi) · **Mas'ul**. Belgilash: `[ ]` → `[x]` + dalil havolasi.

Bloklovchi bandlar (`🔴`) bajarilmasa reliz chiqmaydi. Qolganlari (`🟡`) — sabab va muddat bilan qaror jurnaliga yozilgan holda kechiktirilishi mumkin.

## 1. Funksionallik

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] Reliz qamrovidagi barcha FR'lar uchun AC'lar yashil | `ac-coverage` hisoboti (0 yetishmovchilik, 0 qizil) | Tech lead |
| 🔴 | [ ] E2E ssenariylari (R1: E2E-1…18) uch brauzerda yashil | `main.yml` artefakti | QA |
| 🔴 | [ ] Har sahifaning har tugmasi/havolasi ishlaydi ("o'lik" element yo'q) | AC-UX-9 crawler testi | QA |
| 🔴 | [ ] Production kodida mock, placeholder, `TODO`/`FIXME` yo'q | AC-QS-1 CI qadami | Tech lead |
| 🔴 | [ ] Har sahifada yuklanish, bo'sh, xato holatlari | AC-UX-4 + vizual testlar | Frontend |
| 🔴 | [ ] Feature flag'lar reliz qamroviga mos (`/api/v1/meta` tekshirildi) | Prod `meta` javobi skrinshoti | Tech lead |
| 🟡 | [ ] Ochiq `OQ-*` lar standart qiymat bilan yopilgan yoki egasi qaror qilgan | `23` §3 | PM |

## 2. Typing Engine

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] Barcha umumiy vektorlar (≥ 120) TS va Java'da yashil | CI | Backend + Frontend |
| 🔴 | [ ] Property testlar (≥ 10 000 generatsiya) yashil | CI | Frontend |
| 🔴 | [ ] Staging E2E davomida `devtype_engine_mismatch_total = 0` | Grafana skrinshoti | Backend |
| 🔴 | [ ] Klavish → kadr p95 ≤ 16 ms (1800 belgi, 4× throttle) | Perf test hisoboti | Frontend |
| 🔴 | [ ] Kiritish adapteri: IME, dead key, AltGr, Option, kirill, emoji, paste, avtotakror | AC-TE-10…12 | Frontend |
| 🔴 | [ ] Formulalar 10 ta hujjatlashtirilgan qo'lda hisoblangan misolga mos | AC-TE-3 | Backend |
| 🟡 | [ ] Real qurilmalarda qo'lda sinov: Windows + Chrome, macOS + Safari, Linux + Firefox, ISO klaviatura | Sinov qaydi | QA |

## 3. SQL va PL/SQL

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] `sql` va `plsql` alohida til yozuvlari; umumiy mashq/statistika yo'q | AC-PL-6 | Backend |
| 🔴 | [ ] Har `sql` snippet PostgreSQL 16 da (tegli — Oracle'da) tekshirilgan | Kontent CI | Kontent |
| 🔴 | [ ] Har `plsql` snippet Oracle Database Free'da kompilyatsiya qilingan | Kontent CI | Kontent |
| 🔴 | [ ] PL/pgSQL/T-SQL/MySQL konstruksiyalari 0 ta | AC-PLS-2 + to'liq skan | Kontent |
| 🔴 | [ ] Reliz qamrovidagi har mavzu × 4 daraja × ≥ 5 nashr qilingan mashq | Qamrov matritsasi | Kontent |
| 🟡 | [ ] DB mutaxassisi namunaviy 10% snippetni idiomatiklik bo'yicha ko'rib chiqdi | Imzolangan ro'yxat | Kontent |

## 4. UI/UX va Accessibility

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] 320 / 768 / 1280 / 1920 / 3840 px — gorizontal scroll yo'q, kesilish yo'q | AC-UX-2 vizual hisobot | Frontend |
| 🔴 | [ ] Dark va light temada barcha asosiy ekranlar; FOUC yo'q | AC-UX-1 | Frontend |
| 🔴 | [ ] axe: 0 `critical`/`serious` barcha route'larda | CI | Frontend |
| 🔴 | [ ] Sichqonchasiz to'liq sikl | E2E-18 | QA |
| 🔴 | [ ] Lighthouse (mobile, ommaviy sahifalar): Perf ≥ 90, A11y ≥ 95, BP ≥ 95, SEO ≥ 95 | Lighthouse CI hisoboti (3 o'lchov mediani) | Frontend |
| 🟡 | [ ] Ekran o'quvchi bilan qo'lda sinov (NVDA + Firefox, VoiceOver + Safari) — login, dashboard, natija | Sinov qaydi | QA |

## 5. Lokalizatsiya

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] `uzl`, `uzc`, `ru`, `en` — barcha kalitlar mavjud, ICU o'zgaruvchilari mos | `i18n:check` | Frontend |
| 🔴 | [ ] Email shablonlari va API xato matnlari 4 tilda | Backend integratsiya testi | Backend |
| 🔴 | [ ] Reliz kontentining mavzu nomlari va mashq izohlari 4 tilda | Qamrov matritsasi (lokal ustunlari) | Kontent |
| 🔴 | [ ] `hreflang` × 4 + `x-default` har ommaviy sahifada | SEO testi | Frontend |
| 🟡 | [ ] Har lokal ona tilida so'zlashuvchi tomonidan ko'rib chiqilgan | Imzo | PM |

## 6. Backend va Database

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] Modulith `verify()` va ArchUnit yashil | CI | Backend |
| 🔴 | [ ] Migratsiyalar bo'sh bazada va oldingi sxemadan o'tadi; `validate` yashil | CI | Backend |
| 🔴 | [ ] Staging'da (prod hajmiga yaqin ma'lumotda) migratsiya davomiyligi < 30 s | O'lchov qaydi | Backend |
| 🔴 | [ ] OpenAPI generatsiyasi `14` bilan mos | `openapi-diff` | Backend |
| 🔴 | [ ] So'rovlar soni chegaralari (`17` §3) | Test | Backend |
| 🔴 | [ ] 1 mln sessiyali to'plamda issiq so'rovlarda katta jadvalda `Seq Scan` yo'q | `EXPLAIN` hisoboti | Backend |
| 🔴 | [ ] Hodisa ishlovchilari idempotent; tugallanmagan hodisa qayta nashr qilinadi | AC-BE-6 | Backend |
| 🔴 | [ ] Partitsiyalar 3 oy oldindan yaratilgan; saqlash job'lari ishlaydi | Baza so'rovi + job loglari | Backend |
| 🔴 | [ ] Ilova roli DDL huquqisiz; `audit_log` ga faqat `INSERT/SELECT` | `\dp` chiqishi | Backend |

## 7. Xavfsizlik

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] Xavfsizlik testlari to'plami (`18` §3.3) yashil | CI | Backend |
| 🔴 | [ ] IDOR: barcha `{id}` li endpoint'lar boshqa foydalanuvchi uchun 404 | Parametrlangan test | Backend |
| 🔴 | [ ] ZAP baseline: yuqori darajali topilma yo'q | Hisobot | Security |
| 🔴 | [ ] Dependency-Check / `npm audit` / Trivy: `CRITICAL`/`HIGH` yo'q (yoki muddatli istisno) | Hisobot | Security |
| 🔴 | [ ] gitleaks: repozitoriy tarixida sir yo'q | CI | Security |
| 🔴 | [ ] Prod sarlavhalari: HSTS, CSP, nosniff, frame-ancestors, Referrer-Policy | `curl -I` chiqishi | DevOps |
| 🔴 | [ ] TLS: A baholash (SSL Labs yoki `testssl.sh`) | Hisobot | DevOps |
| 🔴 | [ ] `/actuator`, `/swagger-ui` prod'da tashqaridan yopiq | `curl` chiqishi | DevOps |
| 🔴 | [ ] Barcha `ADMIN`/`EDITOR` akkauntlarda 2FA yoqilgan | Baza so'rovi | Admin |
| 🔴 | [ ] Prod sirlari test/staging sirlaridan farqli; repozitoriyda yo'q | Tekshiruv ro'yxati | DevOps |
| 🟡 | [ ] Mustaqil xavfsizlik ko'rigi (ichki yoki tashqi) | Hisobot | Security |

## 8. Unumdorlik va yuklama

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] k6: 1 000 VU, 15 daq — `17` §1 SLO'lari bajarilgan, xato < 0.1% | k6 hisoboti (staging, prod o'lchamidagi server) | Backend |
| 🔴 | [ ] Bundle byudjetlari | `size-limit` | Frontend |
| 🔴 | [ ] 30 daqiqalik typing sessiyasida xotira sizishi yo'q (heap o'sishi ≤ 20 MB) | Perf test | Frontend |
| 🟡 | [ ] Real foydalanuvchi vitals yig'ilishi yoqilgan | Grafana dashboard | Frontend |

## 9. Operatsiya (DevOps)

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] Blue-green deploy va rollback staging'da sinovdan o'tgan (rollback < 1 daq) | Sinov qaydi + vaqt | DevOps |
| 🔴 | [ ] Backup ishlaydi; **staging'da to'liq tiklash (PITR) mashqi bajarilgan**, RTO ≤ 1 soat | Mashq qaydi | DevOps |
| 🔴 | [ ] Monitoring dashboard'lari va `19` §6.2 alertlari ishlaydi (har biri sun'iy ravishda ishga tushirib tekshirilgan) | Alert test qaydi | DevOps |
| 🔴 | [ ] Kritik alertlar uchun runbook'lar mavjud | `docs/runbooks/` | DevOps |
| 🔴 | [ ] Tashqi uptime monitoring | Xizmat konfiguratsiyasi | DevOps |
| 🔴 | [ ] TLS sertifikat avtomatik yangilanishi sinovdan o'tgan | `certbot renew --dry-run` | DevOps |
| 🔴 | [ ] Log'larda maxfiy ma'lumot yo'q (namunaviy 1 soatlik log skani) | Skan natijasi | Security |
| 🔴 | [ ] SMTP: SPF, DKIM, DMARC sozlangan; xatlar spam'ga tushmaydi | mail-tester natijasi | DevOps |
| 🟡 | [ ] Sentry/GlitchTip frontend va backend xatolarini qabul qiladi | Sinov xatosi | DevOps |

## 10. Huquqiy va kontent

| | Band | Dalil | Mas'ul |
| --- | --- | --- | --- |
| 🔴 | [ ] Maxfiylik siyosati va foydalanish shartlari 4 tilda e'lon qilingan | URL'lar | PM |
| 🔴 | [ ] Har snippetda `source` (original yoki ruxsat beruvchi litsenziya) | Kontent skani | Kontent |
| 🔴 | [ ] Uchinchi tomon kutubxonalari litsenziyalari mos (GPL/AGPL bundle'da yo'q) | `license-checker` / Gradle license report | Tech lead |
| 🟡 | [ ] Shriftlar litsenziyasi (OFL) — `public/fonts/LICENSE` | Fayl | Frontend |

## 11. Reliz kuni

| | Band |
| --- | --- |
| 🔴 | [ ] Reliz qaydlari tayyor |
| 🔴 | [ ] Deploy oynasi kelishilgan; mas'ul shaxs navbatchilikda (kamida 4 soat) |
| 🔴 | [ ] Deploy'dan keyin: smoke testlar, 1 soat davomida 5xx < 0.5%, p95 SLO ichida |
| 🔴 | [ ] Birinchi `ADMIN` akkaunt yaratilgan (CLI buyrug'i bilan, 2FA bilan), seed foydalanuvchilar yo'q |
| 🟡 | [ ] Reliz retrospektivasi 1 hafta ichida |

## 12. Yakuniy hukm

Faqat dalilga asoslanganini tanlash:

- **READY FOR PRODUCTION** — barcha 🔴 bajarilgan, 🟡 lar ham bajarilgan yoki muddat bilan kechiktirilgan.
- **READY WITH MINOR ISSUES** — barcha 🔴 bajarilgan; ≥ 1 🟡 ochiq (ro'yxat va muddat bilan).
- **NOT READY FOR PRODUCTION** — ≥ 1 🔴 bajarilmagan.

Hukm, sana, imzolar va ochiq bandlar ro'yxati shu fayl oxiriga reliz versiyasi bilan qo'shiladi.
