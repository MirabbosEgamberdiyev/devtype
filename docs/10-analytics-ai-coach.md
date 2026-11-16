# 10. Analytics va AI Personal Coach

# A. Analytics

## A.1 Tamoyil

Xom sessiyalar har so'rovda agregatsiya qilinmaydi. `SessionCompleted` hodisasida (commit'dan keyin, asinxron, idempotent) oldindan hisoblangan jadvallar yangilanadi; dashboard ulardan indeks bo'yicha o'qiydi. Agregatlar sessiyalardan to'liq qayta quriladigan bo'lishi shart (admin buyrug'i: `POST /admin/analytics/rebuild?userId=`).

Hisobga kiradigan sessiyalar: `status=COMPLETED`, `user_id IS NOT NULL`. Ranked bo'lishi shart emas. Idempotentlik: `analytics.processed_sessions(session_id)` jadvali — sessiya ikkinchi marta qayta ishlanmaydi.

## A.2 BL-AN-1 Agregatlarni yangilash

```text
onSessionCompleted(s, replayResult):
  if exists processed_sessions(s.id): return
  L = (user, language)

  # user_language_stats  (rejim guruhi bo'yicha: A.3)
  g = modeGroup(s.mode)
  upsert(L, g): sessions_cnt += 1; total_time_ms += s.elapsed_ms; total_chars += s.correct_chars
               sum_wpm += s.wpm; sum_cpm += s.cpm; max_wpm = max(max_wpm, s.wpm)   # max faqat accuracy ≥ 95
               correct_keys += K✓; total_keys += K✓+K✗; error_cnt += K✗
               special_correct += …; special_total += …
               recent_wpm = median(oxirgi 10 sessiya wpm, shu L va g)              # so'rov bilan

  # user_daily_stats  (kun = s.finished_at foydalanuvchi timezone'ida)
  upsert(user, day, language): yuqoridagi yig'indilar + xp (gamification'dan keyin alohida yangilanadi)

  # user_char_stats / user_bigram_stats   (faqat PHYSICAL input)
  for (ch, st) in replayResult.charStats:          # st: expected, firstTryErrors, totalMs, timedCount
      row = upsert(L, ch): expected_cnt += st.expected; error_cnt += st.firstTryErrors; total_ms += st.totalMs
      if st.expected ≥ 3:                          # shovqinni kamaytirish: kam kuzatuv EWMA'ga kirmaydi
         sessErr = st.firstTryErrors / st.expected
         sessMs  = st.totalMs / max(st.timedCount, 1)
         row.ewma_error = row.observed_sessions == 0 ? sessErr : 0.2*sessErr + 0.8*row.ewma_error
         row.ewma_ms    = xuddi shunday (timedCount > 0 bo'lsa)
         row.observed_sessions += 1
      merge confusions: top-5 saqlanadi
  bigramlar uchun xuddi shunday (user_bigram_stats)

  insert processed_sessions(s.id)
```

## A.3 Rejim guruhlari

| Guruh | Rejimlar | Sabab |
| --- | --- | --- |
| `CORE` | PRACTICE, SPEED_TEST, ACCURACY, CHALLENGE, CODE_SPRINT | Oddiy kod yozish — WPM solishtirsa bo'ladi |
| `DRILL` | SPECIAL_CHARS, WEAKNESS | Tabiatan past WPM |

Standart ko'rinish — `CORE`. "Hozirgi WPM", rank, rekord, reyting faqat `CORE` dan. Belgi/bigram statistikasi ikkala guruhdan birga yig'iladi.

## A.4 Ko'rsatkichlar

| Ko'rsatkich | Formula / manba |
| --- | --- |
| Hozirgi WPM | `recent_wpm` (oxirgi 10 `CORE` sessiya mediani); < 3 sessiya bo'lsa `null` |
| O'rtacha WPM / CPM | `sum_wpm / sessions_cnt`; umumiy (barcha tillar) — `Σsum_wpm / Σsessions_cnt` |
| Maksimal WPM | `max_wpm` (accuracy ≥ 95% sessiyalar) |
| Accuracy | `correct_keys / total_keys × 100` (bosishlar bo'yicha, sessiyalar o'rtachasi emas) |
| Maxsus belgilar accuracy | `special_correct / special_total × 100` |
| Jami vaqt / belgilar / mashqlar / xatolar | Yig'indilar; "mashqlar" = `sessions_cnt` |
| Vaqt qatori | `user_daily_stats`: kun; hafta (`date_trunc('week')`, dushanbadan); oy. Nuqta qiymati: `sum_wpm/sessions_cnt`, accuracy |
| Faollik kalendari | `user_daily_stats` da `Σtime_ms` kun bo'yicha, 365 kun; 5 intensivlik: 0, < 3 daq, 3–10, 10–30, > 30 daq |
| Belgi jadvali | `user_char_stats`: expected, error %, o'rtacha ms (`total_ms / timed`), EWMA, zaiflik balli |
| Turkum jadvali | Belgi/bigram qatorlarini `08` §2 turkumlari bo'yicha so'rov vaqtida yig'ish |
| Chalkashliklar | `confusions` JSONB: kutilgan → bosilgan, soni; top 10 (barcha belgilar bo'yicha) |
| Tillar taqqoslash | Har til: hozirgi WPM, accuracy, rank |
| Eng zaif til | A.6 |

Bo'sh holat: tanlangan filtr bo'yicha `sessions_cnt < 5` → grafik bloklari `EmptyState` ("Yana N ta sessiya kerak"); KPI raqamlari mavjud bo'lsa ko'rsatiladi.

## A.5 BL-AN-2 Zaiflik balli va profil

```text
weaknessProfile(user, lang):
  rows = user_char_stats ∪ user_bigram_stats  WHERE expected_cnt ≥ 30 AND observed_sessions ≥ 3
         AND turkum ∉ {ALNUM kichik harflar uchun: faqat error bo'yicha — pastga qarang}
  if |rows| < 8: return EMPTY                      # z-ball uchun tayanch yetarli emas
  μe, σe = mean/std(rows.ewma_error);  μm, σm = mean/std(rows.ewma_ms)
  for r in rows:
      ze = σe > 0 ? (r.ewma_error − μe)/σe : 0
      zm = σm > 0 ? (r.ewma_ms − μm)/σm : 0
      r.weakness = 0.6*ze + 0.4*zm
  weak = rows WHERE weakness > 1.0
  return { chars: top 5 (bitta belgilar), bigrams: top 5 (pattern/bigram), all: weak }
```

- Ball foydalanuvchining **o'z o'rtachasiga** nisbatan — hamma belgida sekin foydalanuvchida ham nisbatan eng zaiflari topiladi.
- Kichik harflar va raqamlar (`ALNUM`) profilga faqat `ewma_error ≥ 0.08` bo'lsa kiradi (aks holda eng ko'p uchraydigan harflar ro'yxatni egallab oladi).
- Profil `GET /analytics/recommendations` va Coach uchun; 10 daqiqa keshlanadi (foydalanuvchi × til).

## A.6 BL-AN-3 Eng zaif til

Foydalanuvchining ≥ 10 `CORE` sessiyali faol tillari ichida: `ratio(lang) = recent_wpm(lang) / median(recent_wpm barcha shunday tillar)`. Eng past `ratio < 0.75` bo'lgan til — "eng zaif". Bitta til yoki shart bajarilmasa — yo'q.

Nima uchun nisbiy: PL/SQL WPM'i tabiatan Java'dan past; **OQ-4** hal bo'lgach til bo'yicha kalibrlangan foiz (percentile) ga o'tiladi.

## A.7 Mahsulot metrikalari (admin)

Kechki job (02:00 UTC) `admin.product_daily_metrics` ni oldingi kun uchun to'ldiradi:

| Metrika | Ta'rif |
| --- | --- |
| `dau` | Kun davomida ≥ 1 `COMPLETED` sessiyali noyob foydalanuvchilar |
| `new_users` | Shu kuni ro'yxatdan o'tganlar |
| `activated_users` | Shu kuni ro'yxatdan o'tib, shu kuni (UTC) ≥ 3 sessiya tugatganlar |
| `sessions_total` / `sessions_rejected` | Holat bo'yicha |
| `sessions_by_mode`, `sessions_by_lang` | JSONB hisoblagichlar |

WAU/MAU, D7/D30 retention — so'rov vaqtida shu jadval va `user_daily_stats` dan.

---

# B. AI Personal Coach

## B.1 Arxitektura

```text
                 ┌──────────────── coach moduli ─────────────────┐
analytics ──────►│ CoachEngine (deterministik)                    │
(WeaknessProfile,│   • PlanGenerator      (BL-AI-1)               │
 stats, trends)  │   • TipSelector        (BL-AI-2)               │──► tuzilgan natija (JSON:
gamification ───►│   • GoalProjector      (BL-AI-3)               │     kodlar + raqamlar)
(streak, level)  │   • WeeklyReportBuilder(BL-AI-4)               │            │
                 │                                                │            ▼
                 │ CoachNarrator (port)                           │   matn (4 lokal)
                 │   • TemplateNarrator  — i18n shablonlar (har doim bor)
                 │   • LlmNarrator       — LLM (R3, feature flag) ─┼──► tashqi LLM API
                 └────────────────────────────────────────────────┘
```

**Asosiy qoida (D-14):** barcha **qarorlar** (nima zaif, nima mashq qilish, prognoz) deterministik `CoachEngine` da. LLM faqat tayyor tuzilgan natijani **so'z bilan ifodalaydi**. LLM o'chiq, sekin yoki xato bo'lsa — foydalanuvchi shablon matnni ko'radi; funksionallik yo'qolmaydi. Coach hech qachon snippet generatsiya qilmaydi va metrikalarni o'zgartirmaydi.

## B.2 BL-AI-1 Kunlik reja

Reja "lazy" yaratiladi: foydalanuvchi o'z vaqti bilan shu kun uchun birinchi marta `GET /coach/plan` (yoki dashboard) so'raganda. Kun davomida o'zgarmaydi. Til — asosiy til (foydalanuvchi `/coach` da boshqasini tanlashi mumkin; har til uchun alohida reja).

```text
generatePlan(user, lang, day):
  ctx = { profile: weaknessProfile, recentAcc: oxirgi 10 CORE sessiya accuracy (bosishlar bo'yicha),
          plateau: isPlateau(user, lang), nextLevel: BL-UD-1 natijasi,
          sessions: CORE sessiyalar soni, daysInactive }
  slots = []
  # 1-slot: eng muhim muammo
  if ctx.sessions < 5:                    slots += PRACTICE(nextLevel)            reason=GETTING_STARTED
  elif ctx.recentAcc < 93:                slots += ACCURACY(nextLevel topic)       reason=LOW_ACCURACY
  elif !ctx.profile.empty:                slots += WEAKNESS                        reason=WEAK_CHARS(chars)
  else:                                   slots += PRACTICE(nextLevel)            reason=CONTINUE_TOPIC
  # 2-slot: o'quv progressi (1-slot shu bo'lmagan bo'lsa), aks holda SPECIAL_CHARS(eng zaif turkum)
  # 3-slot: tezlik
  if ctx.plateau:                         slots += CHALLENGE                       reason=PLATEAU
  else:                                   slots += SPEED_TEST(60)                  reason=MEASURE_SPEED
  # mavjud bo'lmagan rejim (relizda yo'q, profil bo'sh) → PRACTICE(nextLevel) bilan almashtiriladi
  # bir xil (mode, params) ikki marta bo'lmaydi
  persist coach.plans(user, lang, day) + 3 × plan_items(seq, mode, params JSON, reason_code, reason_args)
```

- Vazifa bajarilishi: shu kuni (foydalanuvchi tz) shu `mode` (va, ko'rsatilgan bo'lsa, `topic`) bilan `COMPLETED` sessiya → birinchi mos kelgan bajarilmagan vaziga `completed_at`. `SessionCompleted` ishlovchisi orqali, idempotent.
- 3 ta vazifa bajarilsa: +20 XP (`source_type=COACH_PLAN`, `source_id=<planId>`).
- Reja elementi UI'da: rejim nomi, sabab matni (narrator), "Boshlash" (to'g'ridan-to'g'ri `POST /sessions`).

### Plato (`isPlateau`)

Oxirgi 15 `CORE` sessiya (≥ 15 bo'lsa) WPM qatoriga eng kichik kvadratlar chizig'i; qiyalik `< 0.1` WPM/sessiya **va** shu sessiyalar accuracy'si ≥ 93% → plato.

## B.3 BL-AI-2 Sessiyadan keyingi maslahat

Natija javobida bitta maslahat (`coachTip`), ustuvorlik tartibida birinchi mos kelgani:

| # | Shart | `code` | Argumentlar |
| --- | --- | --- | --- |
| 1 | Shaxsiy rekord | `NEW_RECORD` | wpm, oldingi |
| 2 | Accuracy < 90% | `SLOW_DOWN` | accuracy |
| 3 | Bitta belgi/pattern shu sessiyadagi xatolarning ≥ 25% ini bergan (≥ 4 xato) | `CHAR_TROUBLE` | belgi, xatolar |
| 4 | Backspace ≥ `T` ning 15% i (erkin rejim) | `TOO_MANY_CORRECTIONS` | ulush |
| 5 | Consistency < 50% va sessiya ≥ 30 s | `UNEVEN_RHYTHM` | consistency |
| 6 | WPM hozirgi WPM'dan ≥ 10% yuqori va accuracy ≥ 95% | `GOOD_PACE` | farq % |
| 7 | Eng sekin qator o'rtachadan 2× sekin | `SLOW_LINE` | qator raqami |
| 8 | Hech biri | `KEEP_GOING` | — |

Oxirgi 5 sessiyada ko'rsatilgan `code` (1-dan tashqari) takrorlanmaydi — keyingi mos kelgani olinadi. Maslahat 3-sessiyadan boshlab ko'rsatiladi.

## B.4 BL-AI-3 Maqsad va prognoz

Foydalanuvchi: `{language, targetWpm, targetDate?}`. Har tilda bitta faol maqsad. Tekshiruv: `targetWpm` ∈ [hozirgi+1, 250]; `targetDate` ≥ bugun + 7 kun.

```text
project(goal):
  pts = oxirgi 30 CORE sessiya (≥ 10 kerak; aks holda status=INSUFFICIENT_DATA): (kun raqami, wpm)
  kunlik median wpm qatoriga chiziqli regressiya → slope (WPM/kun), current = recent_wpm
  if slope ≤ 0.02: status = STALLED; eta = null
  else: etaDays = ceil((targetWpm − current) / slope); eta = today + etaDays
        status = (targetDate == null || eta ≤ targetDate) ? ON_TRACK : BEHIND
  requiredPerWeek (targetDate bo'lsa) = (targetWpm − current) / haftalar soni
  if current ≥ targetWpm (3 ketma-ket kun recent_wpm ≥ target): status = ACHIEVED → +100 XP, achievement hodisasi
```

Prognoz kuniga bir marta (yoki maqsad o'zgarganda) qayta hisoblanadi va `coach.goals` da saqlanadi. UI: progress chizig'i (boshlang'ich → hozirgi → maqsad), ETA, holat, "haftasiga +N WPM kerak".

## B.5 BL-AI-4 Haftalik hisobot

Har foydalanuvchi uchun yakshanba 18:00 (o'z vaqti; soatlik job timezone bo'yicha tanlaydi), agar shu haftada ≥ 3 sessiya bo'lsa. Tuzilgan mazmun (`coach.reports.payload` JSON):

```json
{
  "week": "2026-W46",
  "sessions": 14, "activeMinutes": 62, "streak": 12,
  "languages": [{ "slug": "plsql", "wpmNow": 49.6, "wpmPrev": 46.1, "accNow": 96.4, "accPrev": 95.0 }],
  "improved": [{ "token": ":=", "errFrom": 0.14, "errTo": 0.06 }],
  "weakest": [{ "token": "%", "weakness": 1.8 }, { "token": "=>", "weakness": 1.3 }],
  "levelsCompleted": [{ "lang": "plsql", "topic": "cursor", "difficulty": "INTERMEDIATE" }],
  "goal": { "status": "ON_TRACK", "eta": "2026-12-20" },
  "focus": { "code": "WEAK_CHARS", "args": { "tokens": ["%", "=>"] } }
}
```

- `improved`: hafta boshiga nisbatan `ewma_error` eng ko'p kamaygan 3 ta token (haftalik surat: `coach.char_snapshots` emas — hisobot yaratilganda joriy qiymat `payload` ga yoziladi va keyingi hafta shu bilan solishtiriladi).
- `focus` — B.2 dagi 1-slot mantig'i.
- Ilovada `/coach` da ko'rinadi; `emailWeeklyReport=true` bo'lsa email.

## B.6 Bayon qatlami

### TemplateNarrator (R2, har doim mavjud)

Har `reason_code`/`tip code`/hisobot bo'limi uchun i18n kaliti (`coach.tip.CHAR_TROUBLE`: "`{char}` belgisida {count} ta xato — Special Characters'da mashq qiling"). 4 lokal. Har kod uchun 2–3 variant; tanlov `hash(userId, date) mod n` (deterministik).

### LlmNarrator (R3, feature flag `coach.llm.enabled`)

| Masala | Qoida |
| --- | --- |
| Qachon | Faqat haftalik hisobot xulosasi va `/coach` dagi "Batafsil tushuntir" tugmasi. Sessiya maslahati va kunlik reja har doim shablon (kechikishga sezgir) |
| Kirish | B.5 `payload` (yoki reja + profil) + lokal + uslub ko'rsatmasi. **Yuborilmaydi:** email, username, IP, snippet matni, keylog |
| Chiqish | JSON: `{ "summary": string ≤ 600 belgi, "tips": [string ≤ 200] (≤ 3) }`; sxema tekshiruvi; HTML/Markdown olib tashlanadi |
| Rad etish → shablon | Sxema xatosi, vaqt tugashi (8 s), provayder xatosi, uzunlik oshgan, matnda URL bor |
| Limit | Foydalanuvchiga: haftada 1 hisobot + kuniga 3 "tushuntir"; global oylik byudjet chegarasi (cfg) — oshsa flag avtomatik o'chadi va alert |
| Kesh | `(payload hash, locale)` bo'yicha 7 kun |
| Provayder | Port `LlmClient` — implementatsiya almashtiriladi. **OPEN QUESTION (OQ-5):** provayder va model; standart: Anthropic Claude API |
| Ochiqlik | LLM matni yonida "AI yordamida yozilgan" belgisi |

Prompt qoidalari (tizim ko'rsatmasi mazmuni): faqat berilgan raqamlardan foydalan; yangi raqam o'ylab topma; tibbiy/shaxsiy maslahat berma; javob tili = `locale`; ohang — qisqa, aniq, rag'batlantiruvchi; kod yozma.

## B.7 Maxfiylik va boshqaruv

- `coachEnabled=false`: reja, maslahat, hisobot yaratilmaydi va ko'rsatilmaydi; analytics yig'ilishi davom etadi.
- LLM'ga yuborilgan payload va javob `coach.llm_calls` da 30 kun saqlanadi (audit, xarajat): `user_id`, `purpose`, `tokens_in/out`, `status`, `latency_ms` — matn saqlanmaydi, faqat hash.
- Akkaunt o'chirilganda `coach.*` qatorlari o'chiriladi.
