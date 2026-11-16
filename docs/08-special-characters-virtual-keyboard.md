# 08. Special Characters Training va Virtual Keyboard

## 1. Maqsad

Dasturchi tezligining asosiy to'sig'i harflar emas — `{ } [ ] ( ) < > ; : = => -> || && ! % * / + - _ .` kabi belgilar va ularning kombinatsiyalari. Bu modul ularni alohida o'lchaydi, alohida mashq qildiradi va klaviaturada qayerda ekanini ko'rsatadi.

## 2. Belgi turkumlari

Har bosiladigan belgi aynan bitta turkumga tegishli (bigram/pattern — o'z turkumiga). Moslama `frontend/src/engine/categories.ts` va backend `CharCategory` enum'ida bir xil.

| Kod | Turkum | Belgilar / pattern'lar |
| --- | --- | --- |
| `BRACKETS` | Qavslar | `{ } [ ] ( ) < >` |
| `PUNCT` | Tinish | `; : , .` |
| `ASSIGN_CMP` | Tayinlash va taqqoslash | `=` va pattern'lar `:= == === != !== <= >= <> += -=` |
| `ARROWS` | Strelkalar | `=> -> <-` |
| `LOGICAL` | Mantiqiy | `!` `&` `\|` `?` va pattern'lar `&& \|\| ?? ?. ?:` |
| `ARITH` | Arifmetik | `+ - * / % ^ ~` va `++ -- ** //` |
| `IDENT` | Identifikator belgilari | `_ $ @ #` |
| `QUOTES` | Qo'shtirnoqlar | `' "` `` ` `` `\` |
| `WHITESPACE` | Bo'shliq | probel, `\n`, `\t` (faqat qo'lda terilganlari) |
| `CASE` | Registr | `A–Z` (katta harflar) |
| `ALNUM` | Harf va raqam (maxsus emas) | `a–z 0–9` |

- "Maxsus belgi" = `ALNUM`, `WHITESPACE`, `CASE` dan boshqa turkumlar.
- `<` va `>`: `BRACKETS` (HTML/generics) — `<=`, `>=`, `<>`, `->`, `=>` pattern'i ichida bo'lsa pattern turkumiga o'tadi.
- Pattern turkumi: yuqoridagi jadvalda ko'rsatilgan; ro'yxatda yo'q til pattern'i (`%TYPE`, `${`, `$(`, `[[`, `2>&1`, `</`, `/>`) — birinchi belgisining turkumiga.

## 3. Special Characters rejimi

### 3.1 Start ekrani (`/practice/:lang/special`)

- Turkum kartochkalari (8 ta maxsus turkum + `CASE`): nom, belgilar, foydalanuvchining shu turkumdagi accuracy va tezligi (≥ 30 kuzatuv bo'lsa; aks holda "ma'lumot yetarli emas").
- "Tavsiya etilgan" belgisi: eng yuqori zaiflik balli turkum (`FR-SC-6`); ma'lumot yetmasa — til uchun standart tartibdagi birinchi mashq qilinmagan turkum: `BRACKETS`, `PUNCT`, `ASSIGN_CMP`, `QUOTES`, `LOGICAL`, `ARITH`, `ARROWS`, `IDENT`, `CASE`.
- "Aralash" varianti: barcha turkumlar, zaiflik bo'yicha vaznlangan.

### 3.2 BL-SC-1 Mashq generatsiyasi (server)

Maqsad uzunlik: 150 terilgan belgi (±10). Natija — bitta sessiya uchun vaqtinchalik "exercise" (bazaga `exercises` sifatida yozilmaydi; `session_exercises.generated_code` da saqlanadi — replay va validatsiya uchun).

```text
generate(lang, category | weakSet, seed):
  rng = seeded(seed)                              # seed sessiyada saqlanadi
  lines = []
  # 1) 60% — real kod qatorlari
  pool = SPECIAL_CHARS turidagi PUBLISHED mashqlar qatorlari (lang) ∪
         SNIPPET mashqlarining alohida qatorlari (lang), bunda
         density(line, target) = target belgilar/pattern'lar soni / qator uzunligi ≥ 0.25,
         qator uzunligi 15..60, indentsiz (trim)
  while len(lines) < 0.6 × 150: lines += rng.pick(pool, takrorsiz)
  # 2) 40% — generatsiya qilingan ketma-ketliklar
  templates = lang.special_templates[category]    # til konfiguratsiyasida, masalan:
       BRACKETS (java): "{ident}[{n}]", "({ident}, {ident})", "new {Type}<>()", "{ {n}, {n} }"
       ASSIGN_CMP (plsql): "{v} := {n};", "IF {v} <> {n} THEN", "{v} >= {n}"
  while len(lines) < 150: lines += fill(rng.pick(templates), identDict(lang), rng)
  shuffle(lines, rng) ; join('\n') ; kesish: oxirgi qator 150±10 ga tushadigan qilib
```

- `identDict(lang)` — til uslubidagi real identifikatorlar lug'ati (camelCase / snake_case), 200 ta, `content/<lang>/_special/idents.txt`.
- Pool bo'sh yoki yetarli emas → faqat shablonlar; shablon ham yo'q → `409 NO_EXERCISES_AVAILABLE`.
- Generatsiya qilingan matn `BL-EM-1` ning 1–6-qoidalaridan o'tishi shart (unit test bilan kafolatlanadi).
- Rejim: stop-on-error; indent yo'q (barcha qatorlar chapdan).

### 3.3 Natija

Oddiy natija + turkum kesimi: tanlangan turkum bo'yicha accuracy, belgi boshiga o'rtacha ms, oldingi 5 sessiya o'rtachasi bilan farq; eng ko'p chalkashtirilgan 3 juftlik.

## 4. Virtual Keyboard

### 4.1 Vazifasi

Ekrandagi klaviatura tasviri — **yo'riqnoma va tahlil vositasi**, kiritish qurilmasi emas (`FR-VK-6`). U: (1) keyingi klavishni va kerakli Shift'ni ko'rsatadi; (2) barmoq zonalarini o'rgatadi; (3) analytics'da heatmap sifatida ishlaydi.

### 4.2 Layout ma'lumot modeli

Layoutlar statik JSON (`frontend/src/features/keyboard/layouts/*.json`), bazada saqlanmaydi.

```ts
type KeyDef = {
  code: string;          // KeyboardEvent.code: "KeyA", "Digit1", "BracketLeft", "ShiftLeft"…
  base?: string;         // Shift'siz belgi
  shift?: string;        // Shift bilan belgi
  label?: string;        // maxsus klavish yorlig'i i18n kaliti ("Tab", "Enter"…)
  finger: 0|1|2|3|4|5|6|7|8;   // 0–3 chap (jimjiloq→ko'rsatkich), 4–7 o'ng (ko'rsatkich→jimjiloq), 8 bosh barmoq
  w: number;             // kenglik, birlik = 1 oddiy klavish (Tab 1.5, Caps 1.75, Enter 2.25, Space 6.25 …)
};
type KeyboardLayout = {
  id: 'ansi-us' | 'iso-uk' | 'dvorak' | 'colemak';
  rows: KeyDef[][];      // 5 qator
};
```

- Qo'llab-quvvatlanadigan layoutlar: `ansi-us` (standart), `iso-uk`, `dvorak` (ANSI geometriyasi), `colemak` (ANSI geometriyasi).
- `charMap: Map<char, {code, shift}>` layoutdan quriladi (`base`/`shift` bo'yicha). `\n` → `Enter`, `\t` → `Tab`, probel → `Space`.
- Modifikator yorliqlari OS'ga qarab (`navigator.userAgentData.platform` / `navigator.platform`): Windows/Linux — `Ctrl`, `Win`, `Alt`; macOS — `control`, `option`, `command`. Faqat yorliq farq qiladi.
- Barmoq moslamasi — standart touch-typing (ANSI): `` ` 1 Q A Z`` → 0; `2 W S X` → 1; `3 E D C` → 2; `4 5 R T F G V B` → 3; `6 7 Y U H J N M` → 4; `8 I K ,` → 5; `9 O L .` → 6; `0 - = P [ ] \ ; ' /` va Enter, Backspace → 7; Space → 8; `ShiftLeft` → 0, `ShiftRight` → 7.

### 4.3 Ko'rinish holatlari

| Holat | Ko'rinish |
| --- | --- |
| Oddiy klavish | `--surface` fon, `--border`, belgi(lar) markazda (`base` pastda, `shift` yuqorida kichik) |
| Barmoq yo'riqnomasi yoqiq | Fon — barmoq zonasi rangi 20% |
| **Keyingi klavish** | `--accent` chegarasi 2 px + fon 25%; pulsatsiya yo'q (chalg'itadi) |
| **Kerakli Shift** | Keyingi belgi `shift` bo'lsa: klavish barmog'i chap qo'lda (0–3) → `ShiftRight` ajratiladi; o'ng qo'lda → `ShiftLeft` |
| Bosildi — to'g'ri | 120 ms `--accent` to'ldirish |
| Bosildi — xato | 120 ms `--error` to'ldirish (bosilgan klavishda, `event.code` bo'yicha); keyingi klavish belgisi saqlanadi |
| Caps Lock yoqiq | `CapsLock` klavishi `--warning` chegarali |
| Heatmap | Fon intensivligi qiymatga ko'ra (§4.6) |

O'lcham: klavish birligi `clamp(28px, 4.2vw, 44px)`; klaviatura kengligi ≤ muharrir kengligi; `md` da 85% masshtab; < 768 px — ko'rsatilmaydi.

### 4.4 BL-VK-1 Ko'rsatish qoidasi

```text
visible =
  setting == 'always'                         → true
  setting == 'never'                          → false
  setting == 'auto'  →  mode == SPECIAL_CHARS
                        OR (mode == PRACTICE AND difficulty == BEGINNER)
                        OR placement test
  AND viewportWidth ≥ 768 AND inputDevice == PHYSICAL
```

Typing paytida `Ctrl`+`Shift`+`K` bilan vaqtincha yoqiladi/o'chiriladi (sozlamani o'zgartirmaydi). Focus mode'da klaviatura xiralashmaydi.

### 4.5 BL-VK-2 Layout nomuvofiqligini aniqlash

Har `keydown` da: `expectedChar = layout.charOf(event.code, event.shiftKey)`; agar `event.key` bosiladigan ASCII va `event.key != expectedChar` bo'lsa `mismatch += 1`, aks holda `match += 1`.

- Birinchi 20 belgidan keyin `mismatch / (match + mismatch) > 0.3` bo'lsa: boshqa layoutlarni sinab, eng ko'p mos keluvchisi (≥ 90% mos) topilsa — toast: "Klaviaturangiz {layout} ga o'xshaydi. Almashtirilsinmi?" (bir sessiyada bir marta; "Yo'q" tanlansa 30 kun so'ralmaydi).
- Mos layout topilmasa — hech narsa ko'rsatilmaydi, virtual klaviaturada bosilgan klavish belgisi `event.code` bo'yicha davom etadi.
- Bu aniqlash engine'ga ta'sir qilmaydi (engine faqat `event.key` ga qaraydi).

### 4.6 Heatmap (Analytics)

Manba: `GET /analytics/languages/{slug}/chars` (`10` §A.4). Uch rejim:

| Rejim | Klavish qiymati | Rang shkalasi |
| --- | --- | --- |
| Xato | Shu klavishdagi barcha belgilar (`base`+`shift`) bo'yicha `Σerror / Σexpected` | 0% → `--surface`; ≥ 15% → `--error` (chiziqli) |
| Tezlik | Vaznli o'rtacha `ewma_ms`, foydalanuvchi mediani bilan nisbat | ≤ 0.8× → `--accent`; ≥ 1.6× → `--warning` |
| Barmoq | Barmoq bo'yicha yig'ilgan xato darajasi | Barmoq zonalari, intensivlik qiymatga ko'ra |

- < 30 kuzatuvli klavish — shtrixlangan (ma'lumot yetarli emas).
- Hover/fokus: tooltip — belgi(lar), kutilgan soni, xato %, o'rtacha ms.
- Shift talab qiladigan belgilar uchun alohida ko'rsatkich: "Shift bilan" vs "Shift'siz" accuracy (klaviatura ostida 2 ta raqam).
- Barmoq statistikasi jadvali: barmoq, bosishlar, xato %, o'rtacha ms — tanlangan layout bo'yicha hisoblanadi (klient tomonda, `charMap` orqali; serverda saqlanmaydi).

### 4.7 Accessibility

Klaviatura `role="img"` va `aria-label` ("Virtual klaviatura: keyingi klavish {key}") — har klavish alohida e'lon qilinmaydi; `aria-live` yo'q (yozish paytida shovqin). Heatmap rejimida ostida ma'lumot jadvali (`<details>`).
