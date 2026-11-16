# 09. Developer Code Editor — texnik talablar

## 1. Ta'rif va chegaralar

"Developer Code Editor" — foydalanuvchi kod yozadigan yuza. Bu loyihada u **ikki xil komponent**:

| Komponent | Kim uchun | Tabiati | Asos |
| --- | --- | --- | --- |
| `TypingEditor` | Barcha foydalanuvchilar | Muharrir **ko'rinishidagi** typing yuzasi: berilgan kod ustidan yoziladi, erkin tahrirlash yo'q | O'z komponentimiz (React + Shiki) |
| `SnippetEditor` | `EDITOR`, `ADMIN` | Haqiqiy kod muharriri — mashq kodini yozish/tahrirlash | CodeMirror 6 |

**Qaror (D-13):** `TypingEditor` uchun Monaco/CodeMirror ishlatilmaydi. Sabab: "maqsad matn ustidan yozish" semantikasi tayyor muharrirlarning erkin tahrirlash modeliga zid; kiritishni to'liq nazorat qilish (paste, IME, avtoto'ldirish, kursor) kerak; Monaco ~2 MB — unumdorlik byudjetiga sig'maydi.

`TypingEditor` **qilmaydi**: avtomatik qavs yopish, avtoto'ldirish, IntelliSense, ko'p kursor, qidirish/almashtirish, undo/redo, kodni bajarish. Bular ataylab yo'q (`FR-CE-6`) — maqsad har belgini qo'lda terish.

## 2. `TypingEditor` tuzilmasi

```text
<TypingEditor>
 ├─ <HiddenInput/>            ko'rinmas textarea — fokus va hodisalar (07 §4)
 ├─ <Gutter/>                 qator raqamlari
 ├─ <CodeViewport>            9 qatorli oyna, vertikal scroll (transform)
 │    ├─ <IndentGuides/>
 │    ├─ <CurrentLineHighlight/>
 │    ├─ <CodeLine/> × N      har qator memo; ichida token span'lar
 │    ├─ <BracketMatch/>
 │    └─ <Caret/>             absolyut, transform bilan siljiydi
 └─ <Overlay/>                fokus yo'q / pauza / ogohlantirish
```

Props: `engine` (07 §12), `tokens` (Shiki natijasi), `settings` (muharrir sozlamalari), `mode: 'live' | 'replay'`, `onFinish`.

## 3. Syntax highlighting

- Kutubxona: **Shiki** (TextMate grammatikalari). `languages.grammar_id` → Shiki til ID: `java`, `python`, `javascript`, `typescript`, `sql`, `plsql`, `c`, `cpp`, `csharp`, `php`, `go`, `kotlin`, `rust`, `html`, `css`, `shellscript`.
- Grammatikalar lazy yuklanadi (faqat kerakli til); Shiki WASM o'rniga JavaScript RegExp engine (`shiki/engine/javascript`) — bundle kichikroq.
- Mavzular: ikki o'z mavzumiz — `devtype-dark`, `devtype-light` — design tokenlariga bog'langan CSS o'zgaruvchilari bilan (`--syn-keyword`, `--syn-string`, `--syn-number`, `--syn-comment`, `--syn-function`, `--syn-type`, `--syn-operator`, `--syn-punctuation`, `--syn-variable`). Tema almashganda qayta tokenizatsiya **kerak emas**.

| Token | Dark | Light |
| --- | --- | --- |
| keyword | `#C792EA` | `#7C3AED` |
| string | `#C3E88D` | `#15803D` |
| number | `#F78C6C` | `#C2410C` |
| comment | `#676E95` | `#6B7280` (kursiv emas) |
| function | `#82AAFF` | `#1D4ED8` |
| type | `#FFCB6B` | `#B45309` |
| operator, punctuation | `#89DDFF` | `#0E7490` |
| variable / default | `--text` | `--text` |

- Tokenizatsiya snippet yuklanganda **bir marta**; natija `Array<Array<{start, end, tokenClass}>>` (qator bo'yicha). Har klavishda qayta hisoblanmaydi.
- Shiki grammatikasi yuklanmasa (tarmoq xatosi): highlight'siz (bitta rang) davom etadi — typing bloklanmaydi.

## 4. Belgi holatlarini ko'rsatish

Rang — sintaksis uchun; holat — boshqa kanallar orqali (`FR-CE-3`).

| Holat (`07` §2) | Ko'rinish |
| --- | --- |
| `pending` | Sintaksis rangi `opacity: 0.35` |
| `correct` | To'liq sintaksis rangi |
| `incorrect` | Rang `--error`, to'lqinsiz tagchiziq 2 px. Kutilgan belgi ko'rsatiladi (bosilgani emas); probel/`\n` xatosi — `--error` 25% fon |
| `corrected` | To'liq rang + ostida 2 px `--warning` nuqta |
| Joriy pozitsiya | Kursor (§5) |
| `auto` (avtomatik indent) | `showInvisibles` yoqiq: xira `·` (probel) / `→` (tab); o'chiq: bo'sh joy |
| Qator oxiri | `showInvisibles` yoqiq: xira `↵`; joriy pozitsiya `\n` bo'lsa `↵` yorqinlashadi |
| Stop-on-error xatosi | Joriy belgi 150 ms `--error` fon bilan yonadi; kursor qoladi |
| `blindMode` | `incorrect`/`corrected` belgilari ko'rsatilmaydi (`correct` kabi) |

Render optimizatsiyasi: bir qatorda holati va token klassi bir xil ketma-ket belgilar **bitta `<span>`** ga birlashtiriladi; qator `React.memo` — props: qator holatlari satri (masalan `"cccippp…"`), tokenlar, kursor shu qatordami.

## 5. Kursor

| Sozlama | Qiymatlar | Xulq |
| --- | --- | --- |
| `caretStyle` | `line` (2 px vertikal), `block` (belgi kengligida, 40% shaffof), `underline` (2 px ostida) | — |
| `smoothCaret` | `true` | `transform: translate()` + 80 ms `ease-out` o'tish; qator almashganda o'tish yo'q (sakraydi) |
| Miltillash | `idle`/`paused` da 1 s davr; `running` da miltillamaydi | `prefers-reduced-motion` da hech qachon |

Kursor pozitsiyasi: monospace bo'lgani uchun `x = col × charWidth`, `y = line × lineHeight`; `charWidth` shrift yuklangach bir marta o'lchanadi (`document.fonts.ready` dan keyin, yashirin `<span>` orqali) va shrift/o'lcham o'zgarganda qayta. DOM o'lchovi har klavishda **bajarilmaydi**.

## 6. Ko'rinadigan oyna va scroll

- Oyna balandligi: 9 qator (`xs`/`sm`: 6 qator).
- Joriy qator indeksi `L`: scroll maqsadi `top = clamp(L − 2, 0, maxTop)` — ya'ni joriy qator 3-o'rinda turadi (snippet boshida va oxirida tabiiy chegaralar).
- Scroll `transform: translateY()` bilan, 150 ms; `prefers-reduced-motion` da bir zumda.
- Oynadan tashqari qatorlar DOM'da qoladi (`content-visibility: auto`), virtualizatsiya yo'q (maks. ~80 qator).
- Gorizontal: qator ≤ 80 belgi; muharrir kengligi 80ch dan tor bo'lsa — konteyner ichida gorizontal scroll, joriy ustun ko'rinadigan zonaning 25–75% oralig'ida ushlab turiladi.
- Foydalanuvchi sichqoncha g'ildiragi bilan scroll qila olmaydi (typing paytida); replay rejimida mumkin.

## 7. Qo'shimcha muharrir elementlari

| Element | Tafsilot |
| --- | --- |
| Qator raqamlari | Haqiqiy raqamlar (1 dan); joriy qator raqami `--text`, qolganlari `--text-faint`; `lineNumbers=false` da gutter yashirin |
| Joriy qator ajratilishi | To'liq kenglikda `--surface-2` fon |
| Joriy so'z | `wordId` joriy bo'lgan belgilar ostida 1 px `--border-strong` chiziq (`pending` qismi) |
| Indent yo'riqchilari | Har indent darajasida 1 px vertikal `--border` chiziq |
| Juft qavs | Kursor `( [ {` ning oldida yoki `) ] }` ning orqasida bo'lsa — jufti (snippet matnidagi, oldindan hisoblangan stek bo'yicha) `--border-strong` ramka bilan. Satr/izoh ichidagi qavslar ham hisobga olinadi (soddalik) |
| Ko'p snippetli sessiya | Snippetlar orasida ajratgich chiziq va "2 / 5" belgisi; SPEED_TEST da keyingi snippet oldindan pastda xira ko'rinadi |

## 8. Muharrir sozlamalari

`05` §5.17 "Muharrir" bo'limi. Qo'llanilishi:

- Sozlamalar CSS o'zgaruvchilari orqali (`--editor-font-family`, `--editor-font-size`, `--editor-line-height`) — qayta render'siz.
- `ligatures=true` faqat ligatura qo'llaydigan shriftlarda ta'sir qiladi; yoqilganda ham har belgi alohida `<span>` chegarasiga tushishi mumkinligi sababli ligatura faqat bir xil holatdagi belgilar orasida hosil bo'ladi — bu kutilgan xulq.
- Shrift yuklanmasa `ui-monospace, SFMono-Regular, Menlo, Consolas, monospace` zaxirasi.

## 9. Replay rejimi

- Kirish: `GET /sessions/{id}/replay` → keylog (07 §7) + snippet(lar) matni + til konfiguratsiyasi.
- Yangi engine nusxasi yaratiladi; hodisalar `t` bo'yicha `requestAnimationFrame` siklida `dispatch` qilinadi.
- Boshqaruv: ijro/pauza (`Space`), tezlik 1×/2×/4×, ±5 s (`←`/`→`), vaqt chizig'i (bosib o'tish — boshidan shu nuqtagacha tez qayta ijro).
- Yon panel: joriy WPM (o'sha paytdagi), xatolar ro'yxati (bosilsa o'sha vaqtga o'tadi).
- Replay'da `HiddenInput` yo'q; muharrir faqat o'qish uchun; matnni tanlash/nusxalash ruxsat.
- Keylog muddati o'tgan (o'chirilgan) → `404 REPLAY_NOT_AVAILABLE` → "Replay 90 kun saqlanadi" izohi.

## 10. `SnippetEditor` (admin)

- CodeMirror 6: til rejimi (legacy-modes yoki rasmiy paketlar), qator raqamlari, 80-ustun chizig'i, ko'rinmas belgilar, tab → til indentiga mos probel/tab.
- Jonli validatsiya: 400 ms debounce → `POST /admin/exercises/validate` → diagnostika (`BL-EM-1`) CodeMirror lint paneliga (`{line, col}` bo'yicha).
- O'ng panel: `char_count`, `line_count`, `special_ratio`, ichma-ichlik, daraja oralig'iga mosligi (yashil/qizil), kutilgan davomiylik, belgi chastotasi (top-10).
- "Format" tugmasi **yo'q** — formatlash muallif mas'uliyati (til standartiga ko'ra); validator faqat tekshiradi.
- "Typing preview": shu kod bilan `TypingEditor` modalda ochiladi (serverga sessiya ochilmaydi, natija saqlanmaydi — bu yagona "lokal" typing holati va faqat admin uchun).

## 11. Unumdorlik talablari

| Ko'rsatkich | Byudjet | O'lchash |
| --- | --- | --- |
| `keydown` → keyingi kadr | ≤ 16 ms p95, 1800 belgili snippet, 4× CPU sekinlashtirish | Playwright + `PerformanceObserver('event')` / `performance.mark` |
| Bir klavishda qayta render qilinadigan qatorlar | ≤ 2 (joriy va, qator almashsa, oldingi) | React Profiler testi |
| Snippet yuklash → yozishga tayyor | ≤ 300 ms (grammatika keshda), ≤ 800 ms (sovuq) | Lab o'lchovi |
| Layout thrash | Klavish ishlovchisida DOM o'qish yo'q | Kod ko'rigi + ESLint qoidasi (ishlovchida `getBoundingClientRect` taqiqlangan) |

## 12. Accessibility

- `HiddenInput`: `aria-label` = "Kod yozish maydoni. Chiqish uchun Escape." ; `aria-describedby` → kontekst (til, mavzu).
- Ko'rinadigan kod `aria-hidden="true"` (ekran o'quvchi har belgini o'qimasligi uchun); muqobil: "Snippet matnini o'qish" tugmasi kodni oddiy `<pre>` da ochadi.
- Fokus halqasi butun muharrir ramkasida.
- Rang ko'r foydalanuvchilar: xato — tagchiziq, tuzatilgan — nuqta (rangdan mustaqil).
