# 19. DevOps: Docker, CI/CD, Deployment

Boshlang'ich yuklama (1 000 bir vaqtdagi foydalanuvchi) uchun: bitta ilova serveri + alohida PostgreSQL, **Docker Compose**. Kubernetes bu bosqichda ishlatilmaydi (`D-25`).

## 1. Topologiya

```text
Internet ──HTTPS──► Nginx (TLS, statik SPA + prerender, /api va /ws proxy, rate limit)
                      │ HTTP (ichki tarmoq)
                      ├──► app-blue  ┐  Spring Boot 3 (Java 21) — bir vaqtda bittasi "faol",
                      └──► app-green ┘  deploy paytida ikkalasi
                                 │ JDBC (TLS)
                                 ▼
                           PostgreSQL 16 (alohida server yoki boshqariladigan xizmat)
                                 │ WAL arxiv + backup
                                 ▼
                           Obyekt xotirasi (S3-mos): backup'lar, avatarlar

Kuzatuv: Prometheus ← app, nginx-exporter, postgres-exporter, node-exporter
         Loki ← promtail (konteyner loglari)   Grafana   Alertmanager
```

## 2. Muhitlar

| Muhit | Maqsad | Infratuzilma | Ma'lumot |
| --- | --- | --- | --- |
| `local` | Ishlab chiqish | `deploy/docker-compose.local.yml`: PostgreSQL 16, Mailpit, MinIO, (ixtiyoriy) Oracle Free — kontent tekshiruvi uchun. Ilova IDE'dan, frontend `vite dev` (proxy `/api`) | Kontent fixture importi |
| `ci` | Testlar | Testcontainers; GitHub Actions runner | Fixture |
| `staging` | Qabul, E2E, yuklama, DAST | Production bilan bir xil Compose; kichikroq server | Anonimlashtirilgan/sintetik |
| `prod` | Foydalanuvchilar | Ilova: 4 vCPU / 8 GB; DB: 4 vCPU / 16 GB, SSD | Real |

> **OPEN QUESTION (OQ-11):** hosting provayderi va mintaqa. Standart: Yevropa mintaqasidagi VPS (auditoriya — Markaziy Osiyo + global), boshqariladigan PostgreSQL mavjud bo'lsa — undan foydalanish.

Konfiguratsiya farqi faqat muhit o'zgaruvchilari orqali (12-factor). Majburiy o'zgaruvchilar:

| O'zgaruvchi | Tavsif |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `local` / `staging` / `prod` |
| `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_MIGRATOR_USER`, `DB_MIGRATOR_PASSWORD` | Baza |
| `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEYS` (kid → PEM), `JWT_ISSUER`, `JWT_AUDIENCE` | Access token |
| `HMAC_COMPLETE_TOKEN_KEY`, `HMAC_GUEST_TOKEN_KEY`, `TOTP_ENCRYPTION_KEY` | Sirlar |
| `OAUTH_GITHUB_CLIENT_ID/SECRET`, `OAUTH_GOOGLE_CLIENT_ID/SECRET` | OAuth |
| `SMTP_HOST/PORT/USER/PASSWORD`, `MAIL_FROM` | Email (OQ-12) |
| `S3_ENDPOINT/BUCKET/ACCESS_KEY/SECRET_KEY`, `AVATAR_PUBLIC_BASE_URL` | Obyekt xotirasi |
| `LLM_ENABLED`, `LLM_API_KEY`, `LLM_MODEL`, `LLM_MONTHLY_BUDGET` | Coach (R3) |
| `APP_BASE_URL`, `TRUSTED_PROXIES` | URL, proksi IP'lari |
| `SENTRY_DSN`, `OTEL_EXPORTER_OTLP_ENDPOINT` | Kuzatuv |
| `DEVTYPE_MODES_*`, `DEVTYPE_FEATURES_*` | Feature flag'lar (reliz bo'yicha) |

Ilova ishga tushganda majburiy o'zgaruvchilar yo'q bo'lsa — aniq xabar bilan to'xtaydi (fail-fast).

## 3. Docker

### 3.1 Backend image (`backend/Dockerfile`)

- Multi-stage: (1) `eclipse-temurin:21-jdk` — `./gradlew bootJar`; (2) layered jar ajratish (`java -Djarmode=tools extract --layers`); (3) `eclipse-temurin:21-jre` (yoki distroless java21) — qatlamlar alohida `COPY` (bog'liqliklar keshi).
- Root bo'lmagan foydalanuvchi (`uid 10001`); faqat o'qish fayl tizimi (`read_only: true`, `/tmp` tmpfs).
- `ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "org.springframework.boot.loader.launch.JarLauncher"]`.
- `HEALTHCHECK`: `/actuator/health/liveness`.
- Port 8080 (ilova), 8081 (actuator — faqat ichki tarmoq).
- Teglar: `<registry>/devtype-api:<git-sha>` va `:<semver>`; `latest` ishlatilmaydi.

### 3.2 Frontend + Nginx image (`frontend/Dockerfile`)

- (1) `node:22-alpine` — `npm ci && npm run build` (prerender shu yerda; build-arg `VITE_API_BASE=/api/v1`, `PRERENDER_API_URL`); (2) `nginx:1.27-alpine` (unprivileged variant) — `dist/` va `nginx.conf`.
- Bitta image ham statik fayllar, ham reverse proxy vazifasini bajaradi.

### 3.3 Nginx konfiguratsiyasi (talablar)

| Masala | Qoida |
| --- | --- |
| TLS | Let's Encrypt (certbot yoki acme.sh, avtomatik yangilanish); TLS 1.2/1.3; OCSP stapling; HTTP → HTTPS 301 |
| Statik | `/assets/*` (hash'li) — `Cache-Control: public, max-age=31536000, immutable`; `*.html` — `no-cache`; brotli + gzip |
| SPA fallback | Prerender fayli mavjud bo'lsa — o'sha; aks holda `/index.html`. Noma'lum `/api/*` — 404 JSON (index.html emas) |
| Lokal yo'naltirish | `/` → `Accept-Language` bo'yicha `/{locale}/` (302); cookie `dt_locale` ustun |
| Proxy | `/api/` → `upstream app` (faol rang); `/ws` → WebSocket upgrade (R3); `proxy_read_timeout 30s`; `client_max_body_size 6m` (`/api/v1/admin/exercises/import`, avatar), qolgan `/api/` 256k |
| Sarlavhalar | `16` §7 (HSTS, CSP, nosniff, Referrer-Policy, Permissions-Policy, COOP); `X-Forwarded-For/Proto`, `X-Request-Id` (yo'q bo'lsa yaratiladi) |
| Rate limit | `limit_req_zone $binary_remote_addr` 20 r/s (burst 40) `/api/` ga; `/api/v1/auth/` 5 r/s |
| Yopiq yo'llar | `/actuator/*` tashqaridan 404 |
| Loglar | JSON format: vaqt, `request_id`, metod, yo'l, status, `request_time`, `upstream_response_time` (query string va cookie'siz) |

### 3.4 Compose xizmatlari (`deploy/docker-compose.prod.yml`)

`nginx`, `app-blue`, `app-green`, `prometheus`, `grafana`, `loki`, `promtail`, `alertmanager`, `node-exporter`, `nginx-exporter`, `postgres-exporter`. PostgreSQL alohida host/xizmat (bir serverda bo'lsa — alohida compose fayl va volume). Har xizmatda: `restart: unless-stopped`, resurs chegaralari (`mem_limit`, `cpus`), `healthcheck`, log rotatsiyasi (`max-size: 50m`, `max-file: 5`). Tarmoqlar: `edge` (nginx), `internal` (app, monitoring) — baza porti tashqariga ochilmaydi.

## 4. CI/CD (GitHub Actions)

### 4.1 Workflow'lar

| Fayl | Trigger | Ishlar |
| --- | --- | --- |
| `pr.yml` | `pull_request` | `backend` (check: unit, arxitektura, integratsiya, API, qamrov), `frontend` (lint, typecheck, test, size, i18n:check), `vectors` (ikkala tomonda), `e2e` (Chromium, docker compose bilan), `content` (o'zgargan snippetlar validatsiyasi + kompilyatsiya), `security` (Semgrep, gitleaks, dependency review), `openapi-diff`, `ac-coverage` |
| `main.yml` | `push` → `main` | `pr.yml` ishlari + E2E 3 brauzer + vizual + Lighthouse CI → image build → Trivy → registry push → **staging deploy** → smoke test → kontent importi (staging) |
| `release.yml` | Teg `v*` | Staging'dagi shu SHA image'i → **qo'lda tasdiq** (GitHub Environment `production`, 1 tasdiqlovchi) → production deploy → smoke → kontent importi → reliz qaydlari |
| `nightly.yml` | Cron 01:00 UTC | k6 (staging), ZAP baseline, migratsiya yangilanish testi, to'liq vizual matritsa, to'liq kontent kompilyatsiyasi, prerender rebuild |
| `deps.yml` | Haftalik | Dependabot + OWASP Dependency-Check hisoboti |

Qoidalar: `main` himoyalangan (PR + 1 ko'rib chiqish + yashil CI; to'g'ridan-to'g'ri push yo'q); Conventional Commits; versiya — SemVer; har merge'da staging yangilanadi (trunk-based).

### 4.2 Deploy tartibi (blue-green, nol uzilish)

```text
deploy(sha):
  1. backup nuqtasi: pgBackRest (prod: incremental)                 # migratsiyadan oldin
  2. Flyway migrate (devtype_migrator roli) — alohida bir martalik konteyner
       muvaffaqiyatsiz → to'xtash; ilova o'zgarmaydi (expand migratsiyalari eski versiya bilan mos)
  3. idle = faol bo'lmagan rang (masalan green); docker compose up -d app-green (image :sha)
  4. kutish: app-green /actuator/health/readiness == UP (timeout 120 s)   aks holda → to'xtatish, alert
  5. smoke (ichki): GET /api/v1/meta, GET /api/v1/languages, login test akkaunti (staging) / faqat o'qish (prod)
  6. nginx upstream → green (include fayl almashtiriladi) ; nginx -t && nginx -s reload
  7. tashqi smoke: 3 ta ommaviy URL + /api/v1/meta
  8. 5 daqiqa kuzatuv: 5xx ulushi va p95 chegaradan oshsa → avtomatik rollback (6-qadam teskari)
  9. app-blue graceful stop (SIGTERM; server.shutdown=graceful, 30 s)
```

- **Rollback:** upstream'ni oldingi rangga qaytarish (< 1 daq) — eski konteyner 9-qadamgacha ishlab turadi; undan keyin — oldingi image bilan qayta deploy.
- Graceful shutdown: yangi so'rovlar qabul qilinmaydi, joriylari tugatiladi; scheduler job'lari ShedLock tufayli xavfsiz; tugallanmagan hodisalar keyingi instansda qayta nashr qilinadi.
- Frontend va backend bir vaqtda deploy qilinadi; eski frontend (ochiq tab) yangi API bilan ishlashi uchun API orqaga mos (`14` §1.5); chunk yuklanmasa — avtomatik reload (`15` §10); engine versiyasi mos kelmasa — `426`.

### 4.3 Kontent pipeline

`content/` o'zgarsa: validatsiya (`BL-EM-1`) → til bo'yicha kompilyatsiya (Oracle Free, PostgreSQL, `javac`, `tsc`, `python -m py_compile`, …) → `uzc` transliteratsiya → merge'dan keyin `POST /admin/exercises/import` (servis akkaunti, `EDITOR` roli, 2FA o'rniga IP cheklangan API kaliti — **faqat** import endpoint'i uchun) → mashqlar `IN_REVIEW` holatida; nashr qilish — inson tomonidan admin panelda.

## 5. Backup va tiklash

| Masala | Qoida |
| --- | --- |
| Vosita | pgBackRest → obyekt xotirasi (AES-256 shifrlangan, alohida kalit) |
| Jadval | To'liq — haftalik (yakshanba 02:00 UTC); differensial — kunlik; WAL arxiv — uzluksiz |
| Saqlash | 4 ta to'liq backup; WAL — oxirgi 2 to'liq backup oralig'i |
| Maqsad | RPO ≤ 5 daq, RTO ≤ 1 soat |
| Tekshirish | Har oy staging'da to'liq tiklash mashqi (PITR bilan) — natija `docs/runbooks/` da qayd; tiklanmagan backup — backup emas |
| Avatarlar | Obyekt xotirasi versiyalash bilan; yo'qolishi kritik emas |
| Konfiguratsiya | Repozitoriyda (sirlarsiz); sirlar — parol menejeri/secret store'da, alohida zaxira |

## 6. Monitoring va alertlar

### 6.1 Metrikalar

| Guruh | Metrikalar |
| --- | --- |
| HTTP | `http_server_requests_seconds{uri,method,status}`; Nginx so'rovlar/5xx |
| Biznes | `devtype_sessions_started_total{mode,lang}`, `devtype_sessions_completed_total{status}`, `devtype_session_complete_seconds`, `devtype_engine_mismatch_total`, `devtype_suspicious_sessions_total`, `devtype_registrations_total`, `devtype_xp_awarded_total` |
| Hodisalar | `devtype_event_publication_incomplete`, eng eski tugallanmagan hodisa yoshi |
| Baza | HikariCP (`active`, `pending`), `pg_stat_statements` top so'rovlar, replikatsiya/WAL, disk |
| JVM | Heap, GC pauzalari, threadlar |
| Tashqi | LLM chaqiruvlari/xatolar/xarajat; SMTP xatolar; circuit breaker holati |
| Frontend | `web_vitals_*`, `devtype_keypress_latency_ms`, JS xatolar (Sentry) |
| Host | CPU, RAM, disk, tarmoq (node-exporter) |

### 6.2 Alertlar

| Alert | Shart | Darajasi |
| --- | --- | --- |
| API sekin | p95 > 500 ms, 5 daq | Ogohlantirish |
| 5xx ko'p | > 1%, 5 daq | Kritik |
| Ilova ishlamayapti | readiness `DOWN` 1 daq; tashqi uptime 2 ketma-ket xato | Kritik |
| Rad etilgan sessiyalar | `REJECTED` > 3%, 15 daq (engine nomuvofiqligi belgisi) | Kritik |
| Engine mismatch | `devtype_engine_mismatch_total` o'sishi > 0, 15 daq | Ogohlantirish |
| Hodisalar tiqilishi | Tugallanmagan > 100 yoki eng eskisi > 10 daq | Ogohlantirish |
| DB puli | Band > 80%, 5 daq; `pending` > 0 | Ogohlantirish |
| Sekin so'rov | O'rtacha > 200 ms (`pg_stat_statements`) | Ogohlantirish |
| Disk | > 80% | Ogohlantirish; > 90% kritik |
| Backup | Oxirgi muvaffaqiyatli backup > 26 soat; WAL arxiv kechikishi > 10 daq | Kritik |
| Sertifikat | Muddati < 14 kun | Ogohlantirish |
| Xavfsizlik | `16` §9 (token reuse, login xatolari) | Ogohlantirish |
| LLM byudjeti | Oylik 80% | Ogohlantirish |

Kanal: email + messenjer (Alertmanager webhook). Har kritik alert uchun runbook (`docs/runbooks/<alert>.md` — implementatsiya bosqichida yoziladi; shablon: belgilar, tekshirish, tuzatish, eskalatsiya).

### 6.3 Loglar va trace

- Ilova: JSON (logstash encoder) stdout'ga → promtail → Loki. Maydonlar: `timestamp`, `level`, `logger`, `message`, `traceId`, `spanId`, `userIdHash`, `requestId`. Maxfiy ma'lumot — `16` FR-SP-40.
- Darajalar: prod `INFO` (ilova), `WARN` (kutubxonalar); `SECURITY` logger alohida.
- Saqlash: Loki 30 kun; metrikalar 90 kun.
- Trace: OpenTelemetry (HTTP, JDBC, tashqi chaqiruvlar), namuna 10% (xatolar 100%).
- Grafana dashboard'lar (provisioning bilan, repozitoriyda): Umumiy holat · API · Sessiyalar va engine · Baza · JVM · Biznes metrikalari · Frontend vitals.

## 7. Operatsion tartiblar

| Tartib | Qisqa mazmun |
| --- | --- |
| Reliz | Teg → tasdiq → §4.2 → `21` nazorat ro'yxati (birinchi reliz to'liq, keyingilari — qisqartirilgan qism) |
| Hotfix | `main` dan tarmoq → PR → tezlashtirilgan CI (to'liq) → teg |
| Sir rotatsiyasi | JWT kaliti — yangi `kid` qo'shish → imzolashni almashtirish → 30 daqiqadan keyin eskisini olib tashlash; DB paroli — ikki rol bilan |
| Feature flag | `DEVTYPE_MODES_*`, `DEVTYPE_FEATURES_*` — restart bilan; `/api/v1/meta` orqali klientga |
| Baza texnik ishlari | `pg_partman` maintenance (soatlik), `VACUUM`/`ANALYZE` avtomatik; indeks shishishi oylik tekshiriladi |
| Hodisa (incident) | Kritik alert → 15 daq ichida javob; post-mortem 3 ish kuni ichida |
