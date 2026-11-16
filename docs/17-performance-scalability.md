# 17. Performance va Scalability

Har byudjet o'lchanadigan va CI yoki rejalashtirilgan testda tekshiriladigan qiymat. "O'lchanmagan ko'rsatkich bajarildi deb belgilanmaydi" — `21` nazorat ro'yxati real o'lchov natijasini talab qiladi.

## 1. Maqsadlar (SLO)

| ID | Ko'rsatkich | Maqsad | O'lchash |
| --- | --- | --- | --- |
| FR-SP-60 | Klavish → ekran | p95 ≤ 16 ms; p99 ≤ 32 ms (1800 belgi, 4× CPU throttle) | Playwright + Event Timing API |
| FR-SP-61 | API javob (o'qish) | p95 ≤ 200 ms, p99 ≤ 500 ms | k6 + Prometheus histogram |
| FR-SP-62 | `POST /sessions` | p95 ≤ 250 ms | k6 |
| FR-SP-63 | `POST /sessions/{id}/complete` | p95 ≤ 400 ms (1800 belgili keylog) | k6 |
| FR-SP-64 | Reyting top-100 | p95 ≤ 100 ms (kesh), ≤ 300 ms (sovuq, 1 mln yozuvli doska) | k6 |
| FR-SP-65 | Analytics so'rovlari | p95 ≤ 300 ms (2 yillik ma'lumot) | k6 |
| FR-SP-66 | Yuklama | 1 000 bir vaqtdagi foydalanuvchi; xato < 0.1% | k6 (§5) |
| FR-SP-67 | Mavjudlik | 99.5% / oy | Tashqi uptime |
| FR-SP-68 | Lighthouse (ommaviy sahifalar, mobile) | Perf ≥ 90, A11y ≥ 95, BP ≥ 95, SEO ≥ 95 | Lighthouse CI (3 marta median) |
| FR-SP-69 | Core Web Vitals (ommaviy) | LCP ≤ 2.5 s, INP ≤ 200 ms, CLS ≤ 0.1 | Lighthouse CI + real foydalanuvchi o'lchovi (web-vitals → backend metrikasi) |

## 2. Frontend byudjetlari

| Ko'rsatkich | Byudjet |
| --- | --- |
| Boshlang'ich JS (`entry`) | ≤ 150 KB gzip |
| Boshlang'ich CSS | ≤ 25 KB gzip |
| Typing route qo'shimcha JS | ≤ 90 KB gzip + grammatika |
| Shriftlar (birinchi ekran) | ≤ 3 fayl, ≤ 90 KB |
| Landing: so'rovlar soni | ≤ 20 |
| Typing: bitta klavishda render qilinadigan qatorlar | ≤ 2 |
| Typing: bitta klavishda JS vaqti | ≤ 4 ms (desktop), ≤ 8 ms (4× throttle) |
| Snippet yuklash → yozishga tayyor | ≤ 300 ms (issiq), ≤ 800 ms (sovuq) |
| Xotira (typing sahifasi, 30 daqiqa) | Heap o'sishi ≤ 20 MB (sizish yo'q) |

Texnikalar: `09` §11 va `15` §9. CI: `size-limit` (bundle), Lighthouse CI (prerender sahifalar), Playwright perf testi (typing).

### Typing unumdorligi testi (majburiy)

```text
1. 1800 belgili EXPERT snippet bilan sessiya ochiladi (CDP: CPU throttling 4×).
2. 600 ta klavish 50 ms oraliq bilan yuboriladi.
3. Har klavish uchun: keydown vaqt belgisi → keyingi requestAnimationFrame callback vaqti.
4. Tekshiruv: p95 ≤ 16 ms, p99 ≤ 32 ms; uzun vazifalar (> 50 ms) soni = 0.
```

## 3. Backend byudjetlari

| Masala | Qoida |
| --- | --- |
| So'rovlar soni | Har endpoint uchun yuqori chegara (testda Hibernate `Statistics` bilan): `GET /me/dashboard` ≤ 8; `GET /languages/{slug}/topics` ≤ 3; `POST /sessions` ≤ 8; `complete` ≤ 25; tarix sahifasi ≤ 2 |
| N+1 | Taqiqlangan; ro'yxat endpoint'lari DTO projeksiya yoki `JOIN FETCH` |
| Replay murakkabligi | `O(hodisalar)`; 50 000 hodisa ≤ 50 ms |
| Ulanishlar puli | HikariCP: `maximumPoolSize=20` (har instans), `connectionTimeout=5s`, `maxLifetime=30m`; PostgreSQL `max_connections=100` |
| Virtual threads | Yoqilgan; `synchronized` ichida bloklovchi I/O yo'q (pinning) — JFR bilan tekshiriladi |
| JVM | `-XX:MaxRAMPercentage=75`, G1 (standart); konteyner 2 GB; maqsad heap ≤ 1.2 GB barqaror |
| Serializatsiya | Jackson; katta javoblar (replay) oqimli |
| Siqish | Nginx: brotli/gzip (JSON > 1 KB) |
| Timeout'lar | Nginx → ilova 30 s; tashqi HTTP (LLM, HIBP, OAuth) 8 s / 3 s / 5 s |

### Indekslar va so'rovlar

Har "issiq" so'rov uchun indeks `12` da e'lon qilingan. Tekshiruv: integratsiya testida 1 mln sessiyali ma'lumot to'plamida `EXPLAIN (ANALYZE)` — `Seq Scan` katta jadvalda bo'lmasligi kerak:

| So'rov | Indeks |
| --- | --- |
| Tarix (user, vaqt DESC, filtr) | `ix_sessions_user_time`, `ix_sessions_user_lang` |
| Oxirgi 20 sessiyada ko'rilgan mashqlar | `ix_sessions_user_time` + `session_exercises` PK |
| Snippet tanlash | `ix_exercises_pick`, `ix_exercises_ranked` (+ Caffeine kesh) |
| Reyting top va o'rin | `ix_lb_rank` |
| "Hozirgi WPM" (oxirgi 10 sessiya) | `ix_sessions_user_lang` |
| Kunlik qatorlar | `user_daily_stats` PK |
| Shubhali navbat | `ix_sessions_review` |

O'rin hisoblash (`COUNT` indeks bo'yicha) katta doskada sekinlashsa (> 300 ms): o'rin 60 s keshlanadi yoki taxminiy foiz ko'rsatiladi (10 000-o'rindan keyin "top N%").

## 4. Kesh strategiyasi

| Qatlam | Nima | TTL / invalidatsiya |
| --- | --- | --- |
| CDN/Nginx | Statik fayllar (hash'li nom) | `Cache-Control: public, max-age=31536000, immutable` |
| Nginx | `index.html`, prerender HTML | `no-cache` (ETag bilan) |
| HTTP | `GET /languages*` (anonim) | `max-age=300`, `ETag` |
| Ilova (Caffeine) | Tillar, mavzular, ranked pool, top-100, zaiflik profili, level jadvali | `13` §3 |
| Klient (TanStack Query) | `15` §5 | `staleTime` |

Bir nechta instansga o'tilganda: kontent keshi invalidatsiyasi PostgreSQL `LISTEN/NOTIFY` orqali (Redis'siz) yoki qisqa TTL; rate limit va reyting keshi instans-lokal bo'lib qolishi mumkin (chegaralar × instanslar soni — qabul qilinadi) yoki Redis'ga ko'chiriladi.

## 5. Yuklama testi ssenariysi (k6)

1 000 virtual foydalanuvchi, 15 daqiqa (2 daq ko'tarilish, 10 daq barqaror, 3 daq tushish):

| Ulush | Ssenariy | Tsikl |
| --- | --- | --- |
| 60% | Mashq qiluvchi | `GET /me/dashboard` → `POST /sessions` → 45 s kutish → `POST /complete` (oldindan yozilgan haqiqiy keylog'lar to'plamidan) → 15 s |
| 15% | Speed Test | `POST /sessions` → `next` × 2 → 60 s → `complete` → `GET /leaderboards` |
| 15% | Ko'ruvchi | `GET /analytics/*`, `/history`, `/achievements` — 5 s oraliq |
| 5% | Mehmon | `POST /auth/guest` → `POST /sessions` → `complete` |
| 5% | Reyting | `GET /leaderboards` (turli doskalar) — 3 s oraliq |

O'tish mezoni: SLO jadvalidagi p95 qiymatlar; xato < 0.1%; DB CPU < 70%; ilova CPU < 70%; `event_publication` navbati test oxirida 60 s ichida bo'shaydi. Ma'lumot to'plami: 50 000 foydalanuvchi, 2 mln sessiya (generator skripti `backend/src/loadtest/` da — bu test ma'lumoti, production kodi emas).

## 6. Masshtablash yo'li

| Bosqich | Belgi | Qadam |
| --- | --- | --- |
| 0 (boshlang'ich) | — | 1 ilova serveri (4 vCPU / 8 GB, blue-green 2 konteyner), 1 PostgreSQL (4 vCPU / 16 GB) |
| 1 | Ilova CPU > 70% barqaror yoki p95 SLO buzilishi | Ikkinchi ilova serveri + Nginx upstream; ShedLock allaqachon bor; kesh invalidatsiyasi (§4) |
| 2 | DB o'qish yuklamasi (reyting, analytics) | Read replica; `@Transactional(readOnly)` so'rovlar replikaga (routing datasource); replikatsiya kechikishi ≤ 1 s — natija ekranidan keyingi o'qishlar primary'dan |
| 3 | Rate limit / kesh izchilligi muammosi | Redis (rate limit, top-100, sessiya snippet keshi) |
| 4 | `sessions` hajmi | Eski partitsiyalarni arzon xotiraga (tablespace) yoki arxivga; `session_char_stats` muddatini qisqartirish |
| 5 | Jonli poygalar yuklamasi | WebSocket'ni alohida servisga ajratish (modul chegarasi tayyor); xona holati Redis'da |
| 6 | Global kechikish | CDN (statik + prerender); API uchun mintaqaviy replika — **qamrovdan tashqari** |

Holatsizlik: ilova instansida sessiya holati yo'q (JWT, baza) — gorizontal masshtablash uchun to'siq yo'q; istisnolar: Caffeine keshlar, Bucket4j hisoblagichlari, (R3) WebSocket xonalari.

## 7. Resurs chegaralari va himoya

- So'rov tanasi 256 KB; multipart 5 MB; URL 8 KB; sarlavhalar 16 KB.
- Sahifalash `limit` ≤ 100; reyting chuqurligi ≤ 10 000.
- Bitta foydalanuvchining ochiq sessiyalari ≤ 3; sessiyada snippetlar ≤ 40.
- Uzoq so'rovlar: `statement_timeout = 10s` (ilova roli); migratsiyalar uchun alohida.
- Tashqi chaqiruvlar: timeout + circuit breaker + bulkhead (LLM: ≤ 10 parallel).
- Fon job'lari batch (1 000) + oraliq pauza — foydalanuvchi so'rovlariga ta'sir qilmasligi uchun.

## 8. Kuzatish (unumdorlik)

Metrikalar (`19` §6): `http_server_requests_seconds` (endpoint, status), `devtype_session_complete_seconds`, `devtype_replay_seconds`, `devtype_engine_mismatch_total`, `hikaricp_connections_*`, `jvm_*`, `devtype_event_publication_incomplete`, `devtype_cache_*`, frontend `web_vitals_{lcp,inp,cls}` va `devtype_keypress_latency_ms` (namunaviy 1% sessiyadan, p95 — natija bilan birga yuboriladi).
