# 16. Authentication, Authorization va Security

Asos: OWASP ASVS 4.0 Level 2 va OWASP Top 10 (2021). Har talab `FR-SP-n` ID bilan; qabul mezonlari `18` da `AC-SP-*`.

## 1. Authentication

### 1.1 Parol

| ID | Talab |
| --- | --- |
| FR-SP-1 | Uzunlik 10–128; boshqa tarkib qoidasi yo'q. Buzilgan parollar HIBP k-anonymity API orqali tekshiriladi (SHA-1 ning birinchi 5 belgisi yuboriladi). |
| FR-SP-2 | Xesh: **Argon2id** (m = 19 MiB, t = 2, p = 1, 16 bayt tuz, 32 bayt natija) — `Argon2PasswordEncoder`. Parametrlar oshirilganda login paytida qayta xeshlanadi (`upgradeEncoding`). |
| FR-SP-3 | Login xatosi umumiy (`INVALID_CREDENTIALS`); mavjud bo'lmagan foydalanuvchi uchun ham soxta xesh tekshiruvi bajariladi (vaqt bo'yicha farq yo'q). |
| FR-SP-4 | Parol o'zgarganda/tiklanganda barcha refresh tokenlar bekor; foydalanuvchiga xabar xati. |

### 1.2 Tokenlar

| Token | Format | Muddat | Saqlash | Izoh |
| --- | --- | --- | --- | --- |
| Access | JWT, **RS256**, `kid` sarlavhasi | 15 daq | Klient xotirasi (JS o'zgaruvchi) | Claims: `sub` (userId), `role`, `tfa` (2FA o'tganmi), `iat`, `exp`, `jti`, `iss`, `aud` |
| Refresh | 256 bit tasodifiy, base64url | 30 kun | Cookie `dt_refresh`; bazada SHA-256 xeshi | Rotation + oila (`BL-UM-2`) |
| Mehmon | JWT HS256 (`gid`, `exp`) | 7 kun | `localStorage` | Faqat mehmon sessiyalari uchun; huquq bermaydi |
| `completeToken` | HMAC-SHA256(`sessionId|startedAt|exp`) | 24 soat | Klient xotirasi | Sessiyani boshqa natija bilan yakunlashni to'sadi |
| 2FA challenge | JWT HS256 (`sub`, `purpose=2fa`) | 5 daq | Klient xotirasi | Parol o'tgandan keyin TOTP kutish |
| Bir martalik (email, reset) | 256 bit tasodifiy | 24 soat / 1 soat | Bazada SHA-256 xeshi | Bir marta ishlatiladi |

| ID | Talab |
| --- | --- |
| FR-SP-5 | JWT tekshiruvi: imzo, `exp`, `iss`, `aud`, `alg` oq ro'yxati (`RS256` — `none` va HS* rad etiladi). Soat farqi bag'ri 30 s. |
| FR-SP-6 | Kalit rotatsiyasi: 2 ta faol ochiq kalit (`kid` bo'yicha); yangi kalit bilan imzolash, eskisi 15 daqiqa + bag'r davomida tekshiruv uchun qoladi. Maxfiy kalit muhit siridan. |
| FR-SP-7 | Access token bekor qilinmaydi (qisqa muddat); bloklash/rol o'zgarishi ≤ 15 daqiqada kuchga kiradi. Admin endpoint'lari har so'rovda foydalanuvchi `status` va `role` ni bazadan tekshiradi (darhol kuchga kiradi). |
| FR-SP-8 | Refresh cookie: `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth; Max-Age=2592000`. |

### 1.3 OAuth2 (GitHub, Google)

| ID | Talab |
| --- | --- |
| FR-SP-9 | Authorization Code + **PKCE**; `state` (CSRF) va `nonce` (Google OIDC) tekshiriladi; `redirect_uri` aniq oq ro'yxatda. |
| FR-SP-10 | Faqat provayder tasdiqlagan email qabul qilinadi (GitHub: `/user/emails` dan `verified && primary`). Tasdiqlanmagan → `OAUTH_EMAIL_UNVERIFIED`. |
| FR-SP-11 | Mavjud akkauntga avtomatik bog'lash faqat ikkala tomonda ham email tasdiqlangan bo'lsa; aks holda foydalanuvchi parol bilan kirib, sozlamalardan bog'laydi. |
| FR-SP-12 | Provayder tokenlari saqlanmaydi (faqat `provider_uid`). |

### 1.4 2FA (TOTP)

| ID | Talab |
| --- | --- |
| FR-SP-13 | RFC 6238, SHA-1, 6 raqam, 30 s, ±1 qadam bag'r. Sir AES-256-GCM bilan shifrlangan holda saqlanadi (kalit muhit siridan). |
| FR-SP-14 | 10 ta bir martalik zaxira kod (xeshlangan). Ishlatilgan TOTP kodi shu oynada qayta qabul qilinmaydi. |
| FR-SP-15 | `EDITOR`/`ADMIN` uchun majburiy; `USER` uchun ixtiyoriy. 2FA yoqilgan foydalanuvchi OAuth bilan kirganda ham TOTP so'raladi. |

## 2. Authorization

### 2.1 Rollar matritsasi

| Resurs / amal | Guest | USER | EDITOR | ADMIN |
| --- | --- | --- | --- | --- |
| Katalog, reyting, ommaviy profil (o'qish) | ✓ | ✓ | ✓ | ✓ |
| `SPEED_TEST` sessiyasi | ✓ | ✓ | ✓ | ✓ |
| Boshqa rejimlar, progress, analytics, coach | — | ✓ (o'ziniki) | ✓ | ✓ |
| Kontent CRUD, import, qamrov, shikoyatlar | — | — | ✓ | ✓ |
| Mashqni nashr qilish | — | — | ✓ (o'ziniki emas) | ✓ (o'ziniki emas) |
| Tillar CRUD/nashr | — | — | — | ✓ |
| Foydalanuvchilar, rollar, bloklash | — | — | — | ✓ |
| Shubhali sessiyalar, audit, metrikalar | — | — | — | ✓ |
| Achievement/challenge/musobaqa boshqaruvi | — | — | — | ✓ |

### 2.2 Qoidalar

| ID | Talab |
| --- | --- |
| FR-SP-16 | Standart — rad etish: `SecurityFilterChain` da `anyRequest().authenticated()`; ommaviy yo'llar aniq ro'yxatda. |
| FR-SP-17 | Rol tekshiruvi `@PreAuthorize` (metod darajasida) + URL darajasida (`/admin/**`). Ikkalasi ham. |
| FR-SP-18 | **Egalik (IDOR himoyasi):** har resurs so'rovi `WHERE … AND user_id = :currentUser` bilan; topilmasa `404`. `sessions/{id}`, `coach/goals/{id}`, `me/auth-sessions/{id}`, `history` — hammasi. Mehmon: `guest_id` mosligi. |
| FR-SP-19 | Admin amallari: 2FA o'tgan token (`tfa=true`) + bazadan joriy rol/status tekshiruvi. |
| FR-SP-20 | Mass assignment himoyasi: kirish DTO'lari faqat ruxsat etilgan maydonlarni o'z ichiga oladi (`role`, `status`, `emailVerified`, `xp` hech qachon foydalanuvchi DTO'sida emas); noma'lum JSON maydonlari rad etiladi (`FAIL_ON_UNKNOWN_PROPERTIES=true`). |
| FR-SP-21 | Yopiq profil ma'lumoti hech bir ommaviy endpoint'da chiqmaydi; email hech qachon boshqa foydalanuvchiga qaytmaydi. |

## 3. Kiritish va chiqarish xavfsizligi

| ID | Tahdid | Talab |
| --- | --- | --- |
| FR-SP-22 | SQL Injection | Faqat parametrlangan so'rovlar (JPA, `JdbcClient` bind). Dinamik `ORDER BY` — oq ro'yxatdan. Satr birlashtirish bilan SQL yozish taqiqlangan (kod ko'rigi + Semgrep qoidasi). |
| FR-SP-23 | XSS | React avtomatik escaping; `dangerouslySetInnerHTML` taqiqlangan (ESLint). Foydalanuvchi matni (bio, displayName) oddiy matn sifatida. Kod snippetlari matn tugunlari sifatida render qilinadi. Email shablonlarida Thymeleaf `th:text`. |
| FR-SP-24 | CSP | `default-src 'self'; script-src 'self' 'sha256-<tema skripti>'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https://<avatar-host>; font-src 'self'; connect-src 'self' https://<sentry-host>; frame-ancestors 'none'; base-uri 'self'; form-action 'self'; object-src 'none'`. `style-src 'unsafe-inline'` — Radix/inline stillar uchun (**OQ-9**: nonce'ga o'tish). |
| FR-SP-25 | CSRF | API Bearer token bilan — CSRF'ga moyil emas. Cookie ishlatadigan `/auth/refresh`, `/auth/logout`: `SameSite=Strict` + `Origin`/`Referer` sarlavhasi oq ro'yxat tekshiruvi + `Content-Type: application/json` talabi. |
| FR-SP-26 | Validatsiya | Barcha DTO'da Bean Validation (uzunlik, format, oraliq). So'rov tanasi ≤ 256 KB (Nginx va Spring). JSON chuqurligi ≤ 20. |
| FR-SP-27 | Keylog | Base64 → gzip ochishda oqimli o'qish, 1 MB chegarasi (zip bomb); hodisalar ≤ 50 000. |
| FR-SP-28 | Open redirect | `next` parametri faqat `/` bilan boshlanadigan nisbiy yo'l (`//` va `\` rad etiladi). OAuth `redirect_uri` oq ro'yxat. |
| FR-SP-29 | SSRF | Server tashqi URL'ni foydalanuvchi kiritmasidan chaqirmaydi (avatar faqat yuklash orqali). LLM/HIBP/OAuth host'lari konfiguratsiyada qat'iy. |

## 4. Fayl yuklash

| ID | Talab |
| --- | --- |
| FR-SP-30 | **Avatar:** ≤ 2 MB; turi magic bytes bo'yicha (PNG/JPEG/WebP) — kengaytma va `Content-Type` ga ishonilmaydi; server tomonda dekodlanib 256×256 WebP ga **qayta kodlanadi** (metadata olib tashlanadi, polyglot fayllar zararsizlanadi); o'lchamlar ≤ 4096×4096 (dekompressiya bombasi). |
| FR-SP-31 | Saqlash: obyekt xotirasida tasodifiy kalit (`avatars/<uuid>.webp`); asl fayl nomi ishlatilmaydi; ilova domenidan **alohida** host/yo'ldan `Content-Type: image/webp`, `X-Content-Type-Options: nosniff`, `Content-Disposition: inline` bilan beriladi. |
| FR-SP-32 | **Import (admin):** ≤ 5 MB; JSON yoki YAML; YAML — xavfsiz yuklovchi (`SafeConstructor`, alias chegarasi 50); ZIP qabul qilinmaydi; har element `BL-EM-1` dan o'tadi. |
| FR-SP-33 | Yuklash endpoint'lari rate limit: avatar 5/soat; import 20/soat. |

## 5. Rate limiting va suiiste'mol

| Kalit | Chegara | Javob |
| --- | --- | --- |
| `/auth/login`, `/auth/login/2fa` — IP + login | 5 / daq; 5 ketma-ket xatodan keyin shu login uchun ortib boruvchi kechikish (1, 2, 4 … 60 s) | 429 |
| `/auth/register`, `/auth/forgot-password`, `/auth/resend-verification` — IP | 5 / daq; 20 / soat | 429 |
| `/auth/guest` — IP | 10 / soat | 429 |
| `POST /sessions` — user | 30 / daq | 429 |
| `POST /sessions` — mehmon (IP) | 20 / daq | 429 |
| Umumiy API — user | 300 / daq | 429 |
| Umumiy API — anonim (IP) | 60 / daq | 429 |
| `/coach/explain` — user | 3 / kun | 429 `COACH_LIMIT_REACHED` |

- IP: Nginx `X-Forwarded-For` ning ishonchli proksidan keyingi birinchi manzili; ilova faqat ishonchli proksi IP'laridan sarlavhani qabul qiladi.
- Nginx darajasida qo'pol chegara (`limit_req` 20 r/s IP bo'yicha) — ilovaga yetmasdan.
- Account enumeration: ro'yxatdan o'tishda band email/username xabari saqlanadi (UX uchun zarur — `D-18`), lekin rate limit bilan cheklangan; `forgot-password` va `login` hech narsa oshkor qilmaydi.

## 6. Natijalar yaxlitligi (anti-cheat)

Tahdid modeli: foydalanuvchi API'ni to'g'ridan-to'g'ri chaqirib yoki skript bilan soxta natija kiritadi.

1. Klient metrikalariga ishonilmaydi — rasmiy natija faqat server replay'idan (`07` §8).
2. Sessiya serverda ochiladi; snippet faqat shunda beriladi; vaqt server soati bo'yicha chegaralanadi.
3. `completeToken` sessiya ↔ natija bog'lanishini himoya qiladi.
4. Ritm tahlili va shubha balli (`07` §9.3) → reytingdan chetlatish + admin ko'rigi.
5. Reyting: faqat tasdiqlangan email; top-50 ga kirgan har yangi natija admin panelida "yangi top natija" ro'yxatida.
6. Bloklangan foydalanuvchi yozuvlari so'rovda filtrlangan.

Chegarasi: inson ritmini taqlid qiluvchi murakkab botni to'liq to'sib bo'lmaydi. Maqsad — aldashni qimmat va foydasiz qilish.

## 7. Transport va sarlavhalar

| ID | Talab |
| --- | --- |
| FR-SP-34 | Faqat HTTPS; HTTP → 301. TLS 1.2+ (1.3 afzal); zaif shifrlar o'chiq. |
| FR-SP-35 | `Strict-Transport-Security: max-age=31536000; includeSubDomains` (preload — barqarorlashgach). |
| FR-SP-36 | `X-Content-Type-Options: nosniff` · `Referrer-Policy: strict-origin-when-cross-origin` · `Permissions-Policy: camera=(), microphone=(), geolocation=(), payment=()` · `Cross-Origin-Opener-Policy: same-origin`. |
| FR-SP-37 | CORS: API va SPA bitta origin'da — CORS o'chiq (hech qanday `Access-Control-Allow-Origin`). Alohida origin kerak bo'lsa: aniq ro'yxat, `credentials` bilan wildcard taqiqlangan. |
| FR-SP-38 | Xato javoblarida stack trace, SQL, ichki yo'llar yo'q; `Server` sarlavhasi yashirin. |

## 8. Sirlar va ma'lumotlarni himoya qilish

| ID | Talab |
| --- | --- |
| FR-SP-39 | Sirlar (DB paroli, JWT kaliti, OAuth sirlari, SMTP, LLM kaliti, TOTP shifrlash kaliti, HMAC kalitlari) faqat muhit o'zgaruvchilari / Docker secrets; repozitoriyda yo'q (`gitleaks` CI'da). |
| FR-SP-40 | Loglarda: parol, tokenlar, email (xeshlangan), keylog, TOTP — yozilmaydi. Log filtri `Authorization`, `Cookie`, `X-Guest-Token` sarlavhalarini maskalaydi. |
| FR-SP-41 | Shaxsiy ma'lumot minimal: email, username, ixtiyoriy mamlakat/bio/avatar, IP (refresh tokenlar va audit'da). |
| FR-SP-42 | GDPR: eksport (`GET /me/export`), o'chirish (`BL-UM-4`), maxfiylik siyosati 4 lokalda, analitika cookie'lari yo'q (faqat zarur cookie — banner shart emas). |
| FR-SP-43 | Baza: ilova roli DDL huquqisiz; backup'lar shifrlangan; ulanish TLS bilan (alohida host bo'lsa). |
| FR-SP-44 | LLM'ga yuboriladigan ma'lumot: faqat agregat statistika (`10` §B.6). |

## 9. Audit va monitoring

| ID | Talab |
| --- | --- |
| FR-SP-45 | `admin.audit_log`: barcha admin yozuvchi amallari; faqat qo'shiladi. |
| FR-SP-46 | Xavfsizlik hodisalari logi (alohida logger `SECURITY`): muvaffaqiyatsiz login, token reuse, 2FA xatosi, rate limit, rol o'zgarishi, bloklash. |
| FR-SP-47 | Alertlar: 5 daqiqada > 50 `TOKEN_REUSE_DETECTED`; login xatolari keskin o'sishi; `REJECTED` sessiyalar > 3%. |
| FR-SP-48 | Foydalanuvchiga xavfsizlik xatlari: yangi qurilmadan kirish, parol o'zgarishi, 2FA o'zgarishi, token reuse. |

## 10. Bog'liqliklar va ta'minot zanjiri

| ID | Talab |
| --- | --- |
| FR-SP-49 | Dependabot (haftalik); OWASP Dependency-Check (backend) va `npm audit --omit=dev` (frontend) CI'da; `CRITICAL`/`HIGH` — build to'xtaydi (istisno ro'yxati muddat bilan). |
| FR-SP-50 | Docker image: Trivy skan; root bo'lmagan foydalanuvchi; `distroless` yoki minimal JRE; faqat kerakli portlar. |
| FR-SP-51 | Lock fayllar majburiy (`gradle.lockfile`/versions catalog, `package-lock.json`); `npm ci`. |
| FR-SP-52 | SAST: Semgrep (Java, TS qoidalar to'plami) CI'da. DAST: OWASP ZAP baseline staging'da (haftalik). |

## 11. Xavfsizlik testlari cheklovi

Destruktiv yoki real foydalanuvchi ma'lumotiga tegadigan testlar **faqat** `local`, CI va `staging` muhitlarida. Production'da faqat o'qiydigan tekshiruvlar (sarlavhalar, TLS, ochiq portlar).
