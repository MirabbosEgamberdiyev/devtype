/**
 * O'zbek lotin yozuvidan kirill yozuviga transliteratsiya (docs/22 §6).
 */

const PROTECTED_TERMS = [
  'WPM', 'CPM', 'XP', 'DevType', 'Java', 'Python', 'JavaScript', 'TypeScript',
  'SQL', 'PL/SQL', 'C#', 'C++', 'Go', 'Kotlin', 'Rust', 'PHP', 'HTML', 'CSS', 'Bash',
  'Speed Test', 'Practice', 'Accuracy', 'Challenge', 'Special Characters', 'Code Sprint',
  'Enter', 'Tab', 'Shift', 'Esc', 'Ctrl', 'Caps Lock', 'Alt', 'Option', 'Command'
];

const EXCEPTIONS: Record<string, string> = {
  funksiya: 'функция',
  funksiyalar: 'функциялар',
  sirkulyatsiya: 'циркуляция',
  sirkul: 'циркуль',
  oktyabr: 'октябрь',
  aktyor: 'актёр',
  stansiya: 'станция',
  seksiya: 'секция',
  konsert: 'концерт',
  sentner: 'центнер',
};

export function transliterateUzlToUzc(input: string): string {
  if (!input) return '';

  // 1. Himoyalangan qismlarni (ICU o'zgaruvchilari, HTML, maxsus so'zlar) vaqtincha almashtirish
  const placeholders: string[] = [];
  let text = input;

  // ICU o'zgaruvchilari: {name}, {count, plural, ...}
  text = text.replace(/\{[^}]+\}/g, (match) => {
    placeholders.push(match);
    return `___PH_${placeholders.length - 1}___`;
  });

  // HTML teglar
  text = text.replace(/<[^>]+>/g, (match) => {
    placeholders.push(match);
    return `___PH_${placeholders.length - 1}___`;
  });

  // Kod bloklari `...`
  text = text.replace(/`[^`]+`/g, (match) => {
    placeholders.push(match);
    return `___PH_${placeholders.length - 1}___`;
  });

  // Himoyalangan texnik atamalar
  for (const term of PROTECTED_TERMS) {
    const escaped = term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`(?:\\b|(?<=\\s|^))${escaped}(?:\\b|(?=\\s|$|[.,!?:;]))`, 'g');
    text = text.replace(regex, (match) => {
      placeholders.push(match);
      return `___PH_${placeholders.length - 1}___`;
    });
  }

  // 2. So'zma-so'z istisnolar tekshiruvi
  text = text.replace(/\b[a-zA-Zʻʼ'`-]+\b/g, (word) => {
    const lower = word.toLowerCase();
    if (EXCEPTIONS[lower]) {
      const repl = EXCEPTIONS[lower]!;
      if (word === word.toUpperCase()) return repl.toUpperCase();
      if (word[0] && word[0] === word[0].toUpperCase()) {
        return repl.charAt(0).toUpperCase() + repl.slice(1);
      }
      return repl;
    }
    return transliterateWord(word);
  });

  // Placeholderlarni qaytarish
  text = text.replace(/___PH_(\d+)___/g, (_, indexStr) => {
    const idx = parseInt(indexStr, 10);
    return placeholders[idx] ?? '';
  });

  return text;
}

function transliterateWord(word: string): string {
  // Lotin apostroflarini normallashtirish: ʻ (U+02BB) va ʼ (U+02BC)
  let w = word
    .replace(/o['ʻ‘`]/gi, (m) => m[0] === m[0]?.toUpperCase() ? 'Ў' : 'ў')
    .replace(/g['ʻ‘`]/gi, (m) => m[0] === m[0]?.toUpperCase() ? 'Ғ' : 'ғ');

  // Digraflar
  w = w
    .replace(/sh/g, 'ш').replace(/Sh/g, 'Ш').replace(/SH/g, 'Ш')
    .replace(/ch/g, 'ч').replace(/Ch/g, 'Ч').replace(/CH/g, 'Ч')
    .replace(/ng/g, 'нг').replace(/Ng/g, 'Нг').replace(/NG/g, 'НГ');

  // Ajratilgan s'h (digraf emas)
  w = w.replace(/s['ʼ`]h/gi, 'сҳ');

  // Yolashgan unlilar va e/э qoidasi
  const vowels = 'aeiouўғAEIOUЎҒ';
  let result = '';

  for (let i = 0; i < w.length; i++) {
    const char = w[i]!;
    const prev = i > 0 ? w[i - 1] : undefined;
    const next = i + 1 < w.length ? w[i + 1] : undefined;
    const isStartOrAfterVowel = prev === undefined || vowels.includes(prev);

    if ((char === 'y' || char === 'Y') && next) {
      const isUpper = char === 'Y';
      const nextLower = next.toLowerCase();
      if (nextLower === 'o') {
        result += isUpper ? 'Ё' : 'ё';
        i++;
        continue;
      }
      if (nextLower === 'u') {
        result += isUpper ? 'Ю' : 'ю';
        i++;
        continue;
      }
      if (nextLower === 'a') {
        result += isUpper ? 'Я' : 'я';
        i++;
        continue;
      }
      if (nextLower === 'e') {
        result += isUpper ? 'Е' : 'е';
        i++;
        continue;
      }
    }

    if (char === 'e' || char === 'E') {
      const isUpper = char === 'E';
      if (isStartOrAfterVowel) {
        result += isUpper ? 'Э' : 'э';
      } else {
        result += isUpper ? 'Е' : 'е';
      }
      continue;
    }

    // Oddiy harflar xaritasi
    result += mapChar(char);
  }

  return result;
}

const CHAR_MAP: Record<string, string> = {
  a: 'а', b: 'б', d: 'д', f: 'ф', g: 'г', h: 'ҳ', i: 'и', j: 'ж',
  k: 'к', l: 'л', m: 'м', n: 'н', o: 'о', p: 'п', q: 'қ', r: 'р',
  s: 'с', t: 'т', u: 'у', v: 'в', x: 'х', y: 'й', z: 'з',
  'ʼ': 'ъ', "'": 'ъ', '`': 'ъ',
  A: 'А', B: 'Б', D: 'Д', F: 'Ф', G: 'Г', H: 'Ҳ', I: 'И', J: 'Ж',
  K: 'К', L: 'Л', M: 'М', N: 'Н', O: 'О', P: 'П', Q: 'Қ', R: 'Р',
  S: 'С', T: 'Т', U: 'У', V: 'В', X: 'Х', Y: 'Й', Z: 'З',
};

function mapChar(c: string): string {
  return CHAR_MAP[c] ?? c;
}
