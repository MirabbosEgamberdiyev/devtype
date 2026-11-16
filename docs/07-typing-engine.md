# 07. Typing Engine — texnik spetsifikatsiya

Engine — **sof funksiya**: `reduce(state, event) → state`. Bir xil hodisalar ketma-ketligi har doim bir xil holat va metrikalarni beradi. Shu xususiyat tufayli: (1) server keylog'ni qayta ijro etib rasmiy natijani hisoblaydi; (2) replay ishlaydi; (3) engine brauzersiz testlanadi.

Ikki implementatsiya mavjud va ular **aynan bir xil** xulqqa ega bo'lishi shart:

| Joy | Til | Vazifa |
| --- | --- | --- |
| `frontend/src/engine/` | TypeScript, React'siz | Jonli holat, UI, keylog yozish |
| `backend/.../training/domain/` (`KeylogReplayer`, `MetricsCalculator`) | Java | Rasmiy metrikalar, validatsiya |

Moslik `engine-vectors/*.json` umumiy test vektorlari bilan kafolatlanadi (§11).

## 1. Maqsadni tayyorlash (target preparation)

Kirish: snippet matnlari ro'yxati (`code`), til konfiguratsiyasi (`indentStyle`, `indentSize`, `tokenPatterns`), `strictIndent`.

```ts
type TargetChar = {
  ch: string;        // bitta ASCII belgi yoki '\n' yoki '\t'
  line: number;      // 0 dan
  col: number;       // 0 dan (qator ichida)
  auto: boolean;     // avtomatik indent — foydalanuvchi termaydi
  wordId: number;    // so'z kuzatuvi (§3.4)
  patternId: number; // token pattern guruhi yoki -1
  exerciseSeq: number; // ko'p snippetli sessiyada qaysi snippet
};
```

Qoidalar:

1. Matn belgilarga bo'linadi; `\r` bo'lmaydi (kontent kafolati).
2. **Avtomatik indent:** har qatorda birinchi bo'shliq-bo'lmagan belgigacha bo'lgan barcha probel/tab → `auto = !strictIndent`. Faqat bo'shliqdan iborat qator kontentda bo'lmaydi (bo'sh qator — nol uzunlik).
3. Bo'sh qator: faqat `\n` belgisi (bitta Enter).
4. Oxirgi qatordan keyin `\n` yo'q — sessiya oxirgi ko'rinadigan belgi bilan tugaydi.
5. **Token pattern'lar:** har pozitsiyada til `tokenPatterns` ro'yxatidan eng uzun mos keluvchi topiladi (greedy, chapdan o'ngga, kesishmaydi); shu belgilarga bir xil `patternId` beriladi. Satr literallari va izohlar ichida ham qo'llanadi (soddalik uchun; statistikaga ta'siri ahamiyatsiz).
6. **So'z chegaralari:** `wordId` quyidagida oshadi: bo'shliq/`\n` dan keyin; `[A-Za-z0-9_]` va boshqa belgi o'rtasidagi chegarada. Ya'ni `t_ids.COUNT` = 3 so'z: `t_ids`, `.`, `COUNT`.
7. Ko'p snippetli sessiya: snippetlar orasiga hech narsa qo'shilmaydi; har snippet o'z `target` massiviga ega va `next` hodisasi bilan almashadi (§3.6).

`strictIndent=true` da: `indentStyle=SPACE` tilda indent probellari oddiy belgilar (`auto=false`); foydalanuvchi probel bilan ham, Tab bilan ham terishi mumkin (§3.3).

## 2. Holat modeli

```ts
type CharState = 'pending' | 'correct' | 'incorrect' | 'corrected';

type EngineState = {
  phase: 'idle' | 'running' | 'paused' | 'finished';
  mode: Mode;                       // 00 §4.4
  errorMode: 'stop' | 'free';
  targets: TargetChar[][];          // har snippet uchun
  seq: number;                      // joriy snippet
  states: CharState[];              // joriy snippet pozitsiyalari
  typed: (string | null)[];         // 'incorrect' pozitsiyada nima bosilgan
  everWrong: boolean[];             // shu pozitsiyada kamida bir marta xato bo'lganmi
  cursor: number;                   // keyingi teriladigan (auto bo'lmagan) pozitsiya
  consecutiveErrors: number;        // erkin rejim uchun
  // vaqt
  activeMs: number;                 // pauzalarsiz o'tgan vaqt
  lastEventAt: number;              // activeMs shkalasida
  limitMs: number | null;           // SPEED_TEST, CHALLENGE
  // hisoblagichlar (butun sessiya, barcha snippetlar)
  correctKeys: number;              // K✓
  incorrectKeys: number;            // K✗
  backspaces: number;
  totalTyped: number;               // T
  finishedCorrect: number;          // tugallangan snippetlardagi C
  perSecond: number[];              // soniyalik bosishlar (consistency uchun)
  log: KeyEvent[];
};
```

## 3. Hodisalar va o'tish qoidalari

```ts
type KeyEvent =
  | { t: number; k: 'char'; ch: string }      // bosiladigan ASCII belgi (probel ham)
  | { t: number; k: 'enter' }
  | { t: number; k: 'tab' }
  | { t: number; k: 'backspace' }
  | { t: number; k: 'wordDelete' }             // Ctrl/Option+Backspace
  | { t: number; k: 'pause' } | { t: number; k: 'resume' }
  | { t: number; k: 'next' }                   // keyingi snippet
  | { t: number; k: 'finishEarly' }            // PRACTICE: foydalanuvchi erta tugatdi
  | { t: number; k: 'timeout' };               // vaqt limiti tugadi
```

`t` — **faol vaqt** shkalasidagi millisekund (butun son), sessiya boshidan; birinchi hodisa `t = 0`.

### 3.1 Faza o'tishlari

| Joriy | Hodisa | Natija |
| --- | --- | --- |
| `idle` | `char`/`enter`/`tab` | `running`; taymer shu hodisadan (`t=0`) |
| `idle` | boshqa | e'tiborsiz (log'ga yozilmaydi) |
| `running` | `pause` | `paused` (faqat `errorMode='stop'` rejimlarda; aks holda e'tiborsiz) |
| `paused` | `resume` yoki har qanday kiritish hodisasi | `running` (kiritish hodisasi resume'dan keyin oddiy ishlanadi) |
| `running` | oxirgi snippetning oxirgi pozitsiyasi to'g'ri yozildi | `finished` |
| `running` | `timeout` / `finishEarly` | `finished` |
| `finished` | har qanday | e'tiborsiz |

Erkin rejimda oxirgi pozitsiya **xato** yozilsa sessiya tugamaydi: foydalanuvchi tuzatishi yoki (SPEED_TEST da) vaqt tugashini kutishi kerak. Bitta snippetli erkin rejimda (CHALLENGE) oxirgi pozitsiyaga yetilgach — xatolar bo'lsa ham — `Enter` bosilsa sessiya tugaydi (`finishEarly` sifatida yoziladi).

### 3.2 `char` hodisasi

```text
exp = targets[seq][cursor]
if errorMode == 'free' and consecutiveErrors >= 8: ignore (log'ga yoziladi, hisoblanmaydi)
totalTyped += 1
if ev.ch == exp.ch:
    correctKeys += 1
    states[cursor] = everWrong[cursor] ? 'corrected' : 'correct'
    consecutiveErrors = 0
    advance()
else:
    incorrectKeys += 1
    everWrong[cursor] = true
    confusion[exp.ch][ev.ch] += 1
    if errorMode == 'stop':
        # kursor siljimaydi; UI qisqa xato signali beradi
    else:
        states[cursor] = 'incorrect'; typed[cursor] = ev.ch
        consecutiveErrors += 1
        advance()

advance():
    cursor += 1
    while cursor < len and targets[seq][cursor].auto: cursor += 1    # avtomatik indentdan sakrash
```

- Kutilgan belgi `\n` bo'lganda `char` (masalan probel) kelsa — xato (yuqoridagi qoida bo'yicha). Erkin rejimda kursor keyingi qatorga o'tadi.
- Kutilgan belgi oddiy bo'lganda `enter` kelsa — xato; `typed = '\n'`.

### 3.3 `enter` va `tab`

- `enter` ≡ `char('\n')`.
- `tab`:
  - Standart rejim (`strictIndent=false`): e'tiborsiz, log'ga yozilmaydi (brauzer fokusini ham ko'chirmaydi).
  - Strict, kutilgan `\t`: ≡ `char('\t')`.
  - Strict, `indentStyle=SPACE`, kursor qator boshidagi indent zonasida va kutilgan belgi probel: keyingi `min(indentSize, zonadagi qolgan probellar)` ta pozitsiya `correct` bo'ladi; `correctKeys += 1`, `totalTyped += 1` (bitta bosish), lekin `C` ga shu pozitsiyalarning hammasi kiradi.
  - Boshqa holatda: xato (`char('\t')` kabi).

### 3.4 `backspace` va `wordDelete`

```text
backspace:
  if errorMode == 'stop': backspaces += 1; return            # hisoblanadi, ta'siri yo'q
  if cursor == firstTypablePosition(seq): return             # snippet boshi; oldingi snippetga qaytilmaydi
  backspaces += 1
  cursor -= 1
  while targets[seq][cursor].auto: cursor -= 1               # avtomatik indent va qator boshidan orqaga
  states[cursor] = 'pending'; typed[cursor] = null
  consecutiveErrors = max(0, consecutiveErrors − 1) agar o'chirilgan pozitsiya 'incorrect' bo'lgan bo'lsa

wordDelete:
  backspaces += 1 (bitta)
  kursor joriy pozitsiyadan chapga, wordId o'zgarguncha (kamida 1 pozitsiya) yuradi;
  o'tilgan barcha pozitsiyalar 'pending'; '\n' dan orqaga o'tmaydi (qator boshida bo'lsa — oldingi qator oxiridagi '\n' ni o'chiradi, xolos)
  consecutiveErrors = shu nuqtadan chapdagi ketma-ket 'incorrect' lar soni
```

`everWrong` hech qachon tozalanmaydi: tuzatilgan pozitsiya `corrected` bo'ladi va accuracy'da xato sifatida qoladi.

### 3.5 `pause` / `resume`

- Faqat `errorMode='stop'` rejimlarda (PRACTICE, ACCURACY, SPECIAL_CHARS, WEAKNESS).
- Avtomatik pauza: 15 s kiritish bo'lmasa — klient `pause` hodisasini oxirgi kiritish vaqtiga (`t = lastEventAt`) yozadi, ya'ni kutilgan 15 s faol vaqtga **kirmaydi**.
- Qo'lda pauza (`Esc`, fokus yo'qolishi): `pause` hozirgi `t` bilan.
- `resume` ning `t` si `pause` nikiga teng (faol vaqt to'xtab turgan).
- Vaqtli/erkin rejimlarda pauza yo'q; sahifa > 2 s yashirin bo'lsa klient sessiyani `abandon` qiladi.

### 3.6 `next`

Joriy snippet to'liq tugaganda (oxirgi pozitsiya o'tilgan) klient `next` yozadi:

```text
finishedCorrect += count(states ∈ {correct, corrected}, auto emas)   # joriy snippet
seq += 1; states/typed/everWrong yangi snippet uchun; cursor = firstTypablePosition
```

- SPEED_TEST: oqim uzluksiz, `next` bir zumda (tanaffus yo'q). Erkin rejimda snippet oxiriga xatolar bilan yetilsa ham `next` ruxsat (xato pozitsiyalar `C` ga kirmaydi).
- CODE_SPRINT, WEAKNESS: `next` dan oldin klient 3 s tanaffus ko'rsatadi; tanaffus faol vaqtga kirmaydi (`next.t` = oldingi hodisa `t` si).
- Server `next` ni faqat snippet oxiriga yetilganda qabul qiladi; aks holda `KEYLOG_INVALID`.

### 3.7 `timeout` va `finishEarly`

- `timeout`: `t = limitMs`. Undan keyingi hodisalar bo'lmaydi; bo'lsa server e'tiborsiz qoldiradi.
- `finishEarly`: faqat PRACTICE (va §3.1 dagi CHALLENGE holati). `completionPct < 100`.

## 4. Kiritishni ushlash (faqat frontend)

| Masala | Qoida |
| --- | --- |
| Manba | Ko'rinmas `<textarea>` (fokusda) ustida `keydown`; `autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"`; qiymati har hodisadan keyin tozalanadi |
| Belgini aniqlash | `event.key` — uzunligi 1 va kod nuqtasi 0x20–0x7E bo'lsa `char`. Layout'dan mustaqil: Dvorak'da ham to'g'ri belgi keladi |
| Maxsus klavishlar | `Enter`→`enter`; `Tab`→`tab` (`preventDefault`); `Backspace`→`backspace`; `Ctrl+Backspace` (Win/Linux) yoki `Option+Backspace` (macOS)→`wordDelete`; `Cmd+Backspace` (macOS)→e'tiborsiz |
| Modifikatorlar | `ctrlKey` yoki `metaKey` bosilgan `char` — e'tiborsiz (yorliqlar uchun), **bundan mustasno** `AltGr` (Windows'da `ctrlKey && altKey` yoki `getModifierState('AltGraph')`) — `event.key` bosiladigan ASCII bo'lsa qabul qilinadi |
| macOS Option | `Option+harf` ASCII bo'lmagan belgi beradi (`å`, `∂`) → ASCII emas qoidasi |
| Shift | Alohida hodisa emas; natijaviy `event.key` olinadi. Chap/o'ng Shift (`event.code`) virtual klaviatura uchun kuzatiladi, keylog'ga yozilmaydi |
| Caps Lock | `getModifierState('CapsLock')` → indikator. Kiritish o'zgarmaydi (`event.key` allaqachon natijaviy belgi) |
| Avtotakror | `event.repeat === true`: `char`/`enter`/`tab` — e'tiborsiz; `backspace`/`wordDelete` — qabul |
| Dead key | `event.key === 'Dead'` → e'tiborsiz; keyingi kompozitsiya natijasi ASCII bo'lsa ham `isComposing` tufayli qabul qilinmaydi |
| IME | `compositionstart` … `compositionend` oralig'ida (`event.isComposing` yoki `keyCode === 229`) hamma narsa e'tiborsiz; textarea tozalanadi; ogohlantirish lentasi |
| ASCII bo'lmagan belgi (kirill, `é`, emoji) | `char` sifatida **qabul qilinmaydi**, xato sanalmaydi, kursor siljimaydi, taymer boshlanmaydi. Birinchi shunday belgida "Klaviatura tilini lotin (EN) ga o'tkazing" lentasi. Emoji/surrogat juftliklar (`event.key.length > 1` yoki kod nuqtasi > 0x7E) shu qoidaga tushadi |
| Paste/drop/cut | `paste`, `drop`, `cut`, `beforeinput` (`insertFromPaste`, `insertFromDrop`) — `preventDefault`; kontekst menyu o'chiq |
| Ekran klaviaturasi (touch) | `keydown` da `key === 'Unidentified'` → `beforeinput` (`insertText`, `data` 1 belgi) va `deleteContentBackward` dan foydalaniladi; `inputDevice=TOUCH` |
| Vaqt | `performance.now()`; `t = round(now − startedAtPerf − pausedTotal)` |
| Fokus | `blur` → stop rejimda `pause`; vaqtli rejimda ogohlantirish, 2 s dan keyin `abandon` |

Platforma farqlari jadvali (testlanadi):

| Belgi | ANSI US | Eslatma |
| --- | --- | --- |
| `{ } \| : " < > ? ~ _ + ! @ # $ % ^ & * ( )` | Shift bilan | Virtual klaviatura qarama-qarshi Shift'ni ko'rsatadi |
| `` ` `` `\` | ISO layoutlarda boshqa joyda | Layout JSON'ida |
| `@ # { [ ] } \ \|` | Ba'zi Yevropa layoutlarida AltGr | AltGr qoidasi |

## 5. Xato rejimlari qisqacha

| | `stop` | `free` |
| --- | --- | --- |
| Xato belgida kursor | Siljimaydi | Siljiydi, pozitsiya `incorrect` |
| Backspace | Ta'sirsiz (sanaladi) | Ishlaydi |
| `incorrect` pozitsiya yakunda | Bo'lmaydi | Bo'lishi mumkin |
| Ketma-ket xato chegarasi | — | 8 |
| Pauza | Bor | Yo'q |

## 6. Metrikalar

Belgilanishlar (butun sessiya bo'yicha, barcha snippetlar):

- `C` — yakunda `correct` yoki `corrected` holatidagi, `auto` bo'lmagan pozitsiyalar soni (= `finishedCorrect` + joriy snippetdagilar). Vaqtli rejimda `timeout` paytida **tugallanmagan so'z** (`wordId` joriy, oxirigacha yozilmagan) pozitsiyalari `C` dan chiqariladi.
- `T` — `totalTyped` (qabul qilingan `char`/`enter`/`tab` bosishlari).
- `K✓`, `K✗` — `correctKeys`, `incorrectKeys`.
- `m` — faol vaqt daqiqada: `activeMs / 60000`. `SPEED_TEST` da `timeout` bo'lsa `m = limitMs / 60000` aniq.
- `E_uncorrected` — yakunda `incorrect` pozitsiyalar.

| Metrika | Formula | Chegara holatlari |
| --- | --- | --- |
| WPM (net) | `(C / 5) / m` | `m = 0` → 0 |
| Raw WPM | `(T / 5) / m` | |
| CPM | `C / m` | |
| Accuracy % | `K✓ / (K✓ + K✗) × 100` | `K✓ + K✗ = 0` → 0 |
| Xatolar | `K✗` (va alohida `E_uncorrected`) | |
| Backspace | `backspaces` | |
| Consistency % | soniyalik raw WPM qatori `r_i = bosishlar_i / 5 × 60` (to'liq soniyalar; oxirgi to'liqsiz soniya tashlanadi); `μ`, `σ` (populyatsiya); `clamp(100 × (1 − σ/μ), 0, 100)` | < 3 to'liq soniya yoki `μ = 0` → 0 |
| Progress % | `typedPositions / typable × 100`, `typable` = `auto` bo'lmagan pozitsiyalar (joriy snippet; ko'p snippetlida — jami) | |
| Completion % | Rejimga ko'ra (`BL-TM-3`) | |
| Maxsus belgilar accuracy | Yuqoridagi formula, faqat kutilgan belgi maxsus (harf/raqam/probel/`\n` emas) bo'lgan bosishlar | |

**Jonli ko'rsatkichlar** (faqat UI): jonli WPM — oxirgi 10 s oynasi bo'yicha `(shu oynadagi to'g'ri bosishlar / 5) / (oyna_daq)`, 500 ms da bir yangilanadi; birinchi 3 s da ko'rsatilmaydi. Yakuniy WPM — yuqoridagi formula. Jonli va yakuniy qiymat farq qilishi mumkin — bu kutilgan.

**Yaxlitlash:** saqlash va API — 2 kasr xona, `HALF_UP`. UI — `00` §4.5. Hech qanday boshqa yaxlitlash, "bonus" yoki silliqlash yo'q (`FR-TE-14`).

**Belgi vaqti** (analytics uchun): pozitsiya `p` uchun `dt(p)` = shu pozitsiyadagi **birinchi** bosish vaqti − oldingi qabul qilingan `char`/`enter`/`tab` hodisasi vaqti. Sessiyaning birinchi belgisi, pauzadan va `next` dan keyingi birinchi belgi hisobga olinmaydi. `dt > 5000 ms` — tashlab yuboriladi (chalg'ish). Birinchi urinishda xato bo'lsa `firstTryError(p) = 1`.

**Bigram/pattern vaqti:** `patternId ≥ 0` guruh uchun — guruhning birinchi belgisidan oldingi hodisadan guruhning oxirgi belgisi to'g'ri yozilguncha o'tgan vaqt / guruh uzunligi; guruhda kamida bitta `firstTryError` bo'lsa — xato.

## 7. Keylog formati (wire format, versiya 1)

Binar, keyin gzip, keyin base64 (standart, padding bilan).

```text
Header:
  magic        2 bayt   0x44 0x54 ("DT")
  version      1 bayt   0x01
  engineVer    varint   (masalan 10203 = 1.2.3)
  flags        1 bayt   bit0 = strictIndent, bit1 = inputDevice TOUCH
  eventCount   varint
Events (eventCount marta):
  dt           varint   oldingi hodisadan beri ms (birinchisi 0); pause/resume/next uchun 0 bo'lishi mumkin
  kind         1 bayt   0=char 1=enter 2=tab 3=backspace 4=wordDelete 5=pause 6=resume 7=next 8=finishEarly 9=timeout
  ch           1 bayt   faqat kind=0; 0x20–0x7E
```

`varint` — LEB128 unsigned. Chegaralar: `00` §4.6. `dt` > 3 600 000 (1 soat) → `KEYLOG_INVALID`.

## 8. Server replay (`KeylogReplayer`)

```text
replay(session, exercises[], langConfig, keylog):
  state = initial(targets = prepare(exercises, langConfig, session.strictIndent), mode, errorMode, limitMs)
  for ev in keylog.events:
      t += ev.dt
      if state.phase == finished: break                      # ortiqcha hodisalar e'tiborsiz
      if limitMs != null and t > limitMs and ev.kind != timeout:
           apply(timeout at limitMs); break
      validateStructural(ev, state)                          # §9.1
      state = reduce(state, ev at t)
      collectCharStats(ev, state)                            # §6 belgi vaqti, confusion
  if state.phase != finished: phase = finished (klient ulanishni yo'qotgan bo'lishi mumkin)
  return Result(metrics(state), charStats, bigramStats, confusion, perLine, perExerciseTyped)
```

Server metrikani faqat shu yerdan oladi. `clientMetrics` saqlanadi va solishtiriladi: `|wpm_client − wpm_server| > 0.5` yoki accuracy farqi > 0.1 bo'lsa `engine_mismatch_total` metrikasi oshadi va WARN log (keylog ID bilan) — bu engine versiyalari nomuvofiqligi belgisi.

## 9. Validatsiya

### 9.1 Strukturaviy (xato → `REJECTED`, `KEYLOG_INVALID`)

- Header yaroqsiz, versiya noma'lum, o'lcham chegaradan oshgan.
- `engineVer` qo'llab-quvvatlanmaydi (server oxirgi 2 minor versiyani qabul qiladi) → `ENGINE_VERSION_UNSUPPORTED` (klientga "sahifani yangilang").
- `flags` sessiya bilan mos emas (`strictIndent`, `inputDevice`).
- `next` snippet oxiriga yetilmasdan; `next` oxirgi snippetdan keyin.
- `pause`/`resume` erkin rejimda; ketma-ket ikki `pause`; `pause` siz `resume`. (`pause` dan keyin to'g'ridan-to'g'ri kiritish hodisasi kelishi ruxsat — u resume hisoblanadi.)
- `finishEarly` ruxsat etilmagan rejimda.
- `ch` 0x20–0x7E dan tashqarida.

### 9.2 Vaqt (xato → `REJECTED`, `TIMING_MISMATCH`)

- `ΣΔt ≤ (serverReceivedAt − session.startedAt) + 2000 ms`.
- Vaqtli rejim: `timeout.t == limitMs` (±0) yoki oxirgi hodisa `t ≤ limitMs + 500`.

### 9.3 Shubha balli (rad etilmaydi; `00` §4.6)

Intervallar — faqat ketma-ket `char`/`enter` hodisalari orasidagi `dt` (pauza va `next` atrofidagilar chiqariladi); ≥ 30 interval bo'lsa hisoblanadi.

| Qoida | Shart | Ball |
| --- | --- | --- |
| `SPEED` | WPM > 220 | +50 |
| `FAST_INTERVALS` | `dt < 20 ms` ulushi > 20% | +50 |
| `FLAT_RHYTHM` | CV (`σ/μ`) < 0.15 | +40 |
| `JUMP` | Foydalanuvchining shu tildagi median WPM'idan (≥ 10 sessiya) ≥ 60% yuqori | +20 |
| `NO_ERRORS_FAST` | `K✗ = 0` va WPM > 150 va `C ≥ 300` | +20 |

Ball ≥ 50 → `ranked=false`, `review_status=PENDING`. Sabablari `suspicion_reasons` (matn massivi) da saqlanadi.

## 10. Chekka holatlar

| Holat | Xulq |
| --- | --- |
| Bo'sh qator ketma-ketligi | Har biri bitta Enter; kursor keyingi qatorning indentidan sakraydi |
| Qator faqat `}` dan iborat, indent bilan | Indent avtomatik, `}` teriladi |
| Tab ichki pozitsiyada (qator o'rtasida) | Kontentda bo'lmaydi (validatsiya) |
| Probel qator oxirida | Kontentda bo'lmaydi |
| Foydalanuvchi probel o'rniga Enter bosdi (erkin) | Pozitsiya `incorrect`, kursor +1 (qator almashmaydi — ko'rinish maqsadga bog'liq) |
| Oxirgi belgi xato (erkin, bitta snippet) | §3.1 |
| Sahifa yangilandi | Klient `abandon` yubora olmasa — sessiya 24 soatdan keyin job bilan `ABANDONED`. PRACTICE: holat `sessionStorage` dan tiklanmaydi (soddalik; **D-17**) |
| Tarmoq yo'q | Sessiya davom etadi; `complete` IndexedDB navbatida, eksponensial qayta urinish (1 s, 2 s, 4 s … 60 s), 24 soatgacha |
| Ikki tabda bitta sessiya | Ikkinchi `complete` idempotent javob oladi |
| Tizim soati o'zgardi / uyqu | `performance.now()` monoton; uyqudan keyin vaqtli rejimda `dt` katta → `timeout` qoidasi |
| Brauzer `performance.now()` aniqligini pasaytirgan (Firefox 1 ms, Safari) | `t` butun ms — yetarli |
| 1800 belgili snippet | Render byudjeti `17` §2 |
| Klaviatura ghosting (bir vaqtda bosishlar) | Har `keydown` alohida hodisa; tartib brauzer bergandek |
| `blindMode` | Faqat UI: `incorrect` ko'rsatilmaydi; engine o'zgarmaydi |

## 11. Umumiy test vektorlari (`engine-vectors/`)

```json
{
  "name": "free-mode-backspace-across-newline",
  "language": { "indentStyle": "SPACE", "indentSize": 2, "tokenPatterns": [":=", "=>"] },
  "mode": "SPEED_TEST", "limitMs": 30000, "strictIndent": false,
  "exercises": ["BEGIN\n  v_x := 1;\nEND;"],
  "events": [ { "dt": 0, "k": "char", "ch": "B" }, { "dt": 120, "k": "char", "ch": "E" } ],
  "expect": {
    "phase": "running", "cursor": 2, "seq": 0,
    "states": "ccppppaapppppppppppppp",
    "metrics": { "wpm": 200.00, "rawWpm": 200.00, "accuracy": 100.00, "correctChars": 2, "errors": 0, "backspaces": 0 },
    "charStats": { "E": { "expected": 1, "errors": 0, "totalMs": 120 } }
  }
}
```

- `states` satri: `p` pending, `c` correct, `i` incorrect, `r` corrected, `a` auto.
- Majburiy qamrov (kamida): har hodisa turi × har xato rejimi; avtomatik indent sakrashi; strict indent + Tab; `wordDelete` chegaralari; 8 ketma-ket xato; `next`; `timeout` da tugallanmagan so'z; pauza va faol vaqt; consistency chegara holatlari; token pattern greedy tanlovi; yaxlitlash (`HALF_UP`) chegaralari; har rejim tugash qoidasi. Minimal **120 vektor**.
- Vektorlar ikkala tomonda parametrlangan test sifatida ishlaydi (Vitest va JUnit 5); birortasi o'tmasa CI to'xtaydi.

## 12. Engine API (frontend)

```ts
createEngine(config: EngineConfig): Engine
interface Engine {
  getState(): Readonly<EngineState>;
  dispatch(ev: KeyEvent): void;            // reduce + log
  subscribe(listener: (s: EngineState, ev: KeyEvent) => void): () => void;
  metrics(): Metrics;                       // §6
  liveWpm(nowT: number): number | null;
  exportKeylog(): string;                   // §7: base64(gzip(binary))
}
```

`engine/` React, DOM yoki `Date` ga murojaat qilmaydi (ESLint `no-restricted-imports`/`no-restricted-globals`). Vaqt tashqaridan (`t`) beriladi.
