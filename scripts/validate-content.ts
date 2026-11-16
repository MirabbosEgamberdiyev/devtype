import fs from 'fs';
import path from 'path';
import crypto from 'crypto';
import yaml from 'yaml';

interface ValidationError {
  file: string;
  rule: string;
  line?: number;
  col?: number;
  message: string;
}

interface ValidationWarning {
  file: string;
  rule: string;
  message: string;
}

interface SnippetData {
  language: string;
  topic?: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  type?: 'SNIPPET' | 'SPECIAL_CHARS' | 'CHALLENGE';
  title: string;
  note?: {
    en?: string;
    uzl?: string;
    ru?: string;
    uzc?: string;
  };
  tags?: string[];
  ranked_pool?: boolean;
  source?: string;
  code: string;
}

const LEVEL_RANGES: Record<string, { min: number; max: number }> = {
  BEGINNER: { min: 80, max: 200 },
  INTERMEDIATE: { min: 200, max: 450 },
  ADVANCED: { min: 450, max: 900 },
  EXPERT: { min: 900, max: 1800 },
};

const LANG_CONFIG: Record<string, { indentStyle: 'SPACE' | 'TAB'; indentSize: number; isRiverAllowed?: boolean }> = {
  java: { indentStyle: 'SPACE', indentSize: 4 },
  python: { indentStyle: 'SPACE', indentSize: 4 },
  javascript: { indentStyle: 'SPACE', indentSize: 2 },
  typescript: { indentStyle: 'SPACE', indentSize: 2 },
  sql: { indentStyle: 'SPACE', indentSize: 2, isRiverAllowed: true },
  plsql: { indentStyle: 'SPACE', indentSize: 2, isRiverAllowed: true },
  go: { indentStyle: 'TAB', indentSize: 1 },
  csharp: { indentStyle: 'SPACE', indentSize: 4 },
  cpp: { indentStyle: 'SPACE', indentSize: 4 },
  c: { indentStyle: 'SPACE', indentSize: 4 },
  kotlin: { indentStyle: 'SPACE', indentSize: 4 },
  rust: { indentStyle: 'SPACE', indentSize: 4 },
  php: { indentStyle: 'SPACE', indentSize: 4 },
  bash: { indentStyle: 'SPACE', indentSize: 2 },
  html: { indentStyle: 'SPACE', indentSize: 2 },
  css: { indentStyle: 'SPACE', indentSize: 2 },
};

// Calculate effective character count excluding automatic leading indent spaces (BL-EM-2)
export function calculateCharCount(code: string): number {
  const normalized = code.endsWith('\n') ? code.slice(0, -1) : code;
  const lines = normalized.split('\n');
  let count = 0;
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i] ?? '';
    if (i === 0) {
      // First line: count all chars (no prefill on line 0)
      count += line.length;
    } else {
      // Subsequent lines: leading spaces are prefilled on Enter
      const nonIndent = line.replace(/^[ \t]+/, '');
      count += nonIndent.length;
    }
    // Newline character counts towards char_count
    if (i < lines.length - 1) {
      count += 1;
    }
  }
  return count;
}

export function validateSnippet(filePath: string, contentStr: string, seenHashes: Map<string, string>): { errors: ValidationError[]; warnings: ValidationWarning[] } {
  const errors: ValidationError[] = [];
  const warnings: ValidationWarning[] = [];

  // Check Rule 2: CRLF not allowed
  if (contentStr.includes('\r')) {
    errors.push({
      file: filePath,
      rule: 'CRLF_NOT_ALLOWED',
      message: 'CRLF (\\r\\n) taqiqlangan; faqat LF (\\n) qator oxiri ishlatilsin',
    });
  }

  // Check Rule 4 (file-level): No blank line at end of file
  if (/\n[ \t]*\n[ \t]*$/.test(contentStr)) {
    errors.push({
      file: filePath,
      rule: 'BLANK_LINES',
      message: 'Fayl oxirida bo\'sh qator taqiqlanadi',
    });
  }

  let parsed: SnippetData;
  try {
    parsed = yaml.parse(contentStr) as SnippetData;
  } catch (err: unknown) {
    errors.push({
      file: filePath,
      rule: 'YAML_SYNTAX',
      message: `YAML sintaksis xatosi: ${err instanceof Error ? err.message : String(err)}`,
    });
    return { errors, warnings };
  }

  if (!parsed || typeof parsed !== 'object') {
    errors.push({ file: filePath, rule: 'EMPTY_FILE', message: 'Fayl bo\'sh yoki obyekt emas' });
    return { errors, warnings };
  }

  const { language, difficulty, type = 'SNIPPET', title, note, code } = parsed;

  if (!language) {
    errors.push({ file: filePath, rule: 'LANGUAGE_REQUIRED', message: 'language maydoni majburiy' });
  }

  if (!title || typeof title !== 'string') {
    errors.push({ file: filePath, rule: 'TITLE_REQUIRED', message: 'title maydoni majburiy' });
  }

  if (!difficulty || !LEVEL_RANGES[difficulty]) {
    errors.push({
      file: filePath,
      rule: 'DIFFICULTY_INVALID',
      message: `difficulty noto'g'ri: ${difficulty} (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT kutiladi)`,
    });
  }

  // Rule 12: TOPIC_REQUIRED
  if (type === 'SNIPPET' && (!parsed.topic || parsed.topic.trim() === '')) {
    errors.push({
      file: filePath,
      rule: 'TOPIC_REQUIRED',
      message: 'SNIPPET turi uchun topic maydoni majburiy',
    });
  }

  if (!code || typeof code !== 'string') {
    errors.push({ file: filePath, rule: 'CODE_REQUIRED', message: 'code maydoni bo\'sh' });
    return { errors, warnings };
  }

  // Rule 10: SMART_PUNCTUATION
  const smartPunctRegex = /[“”‘’–—]/;
  if (smartPunctRegex.test(code)) {
    errors.push({
      file: filePath,
      rule: 'SMART_PUNCTUATION',
      message: '"Aqlli" qo\'shtirnoq va tire (“ ” ‘ ’ – —) taqiqlangan; ASCII \' " - ishlating',
    });
  }

  // Rule 1: CHAR_NOT_ALLOWED
  const langConf = LANG_CONFIG[language] || { indentStyle: 'SPACE', indentSize: 4 };
  // Block scalar in YAML (code: |) appends a single trailing newline. Strip single trailing \n for line analysis.
  const normalizedCode = code.endsWith('\n') ? code.slice(0, -1) : code;
  const lines = normalizedCode.split('\n');

  // Rule 11: TOO_FEW_LINES
  if (type !== 'SPECIAL_CHARS' && lines.length < 2) {
    errors.push({
      file: filePath,
      rule: 'TOO_FEW_LINES',
      message: `Kamida 2 qator bo'lishi shart, topildi: ${lines.length}`,
    });
  }

  // Rule 4: BLANK_LINES
  if (lines.length > 0 && lines[lines.length - 1] === '') {
    errors.push({
      file: filePath,
      rule: 'BLANK_LINES',
      message: 'Fayl / kod oxirida bo\'sh qator taqiqlanadi',
    });
  }

  let consecutiveBlank = 0;
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i] ?? '';
    const lineNum = i + 1;

    if (line.trim() === '') {
      consecutiveBlank++;
      if (consecutiveBlank > 2) {
        errors.push({
          file: filePath,
          rule: 'BLANK_LINES',
          line: lineNum,
          message: 'Ketma-ket 2 dan ortiq bo\'sh qator taqiqlanadi',
        });
      }
    } else {
      consecutiveBlank = 0;
    }

    // Rule 3: TRAILING_WHITESPACE
    if (/[ \t]+$/.test(line)) {
      errors.push({
        file: filePath,
        rule: 'TRAILING_WHITESPACE',
        line: lineNum,
        message: `Qator oxirida bo'shliq taqiqlanadi`,
      });
    }

    // Rule 6: LINE_TOO_LONG
    if (line.length > 80) {
      errors.push({
        file: filePath,
        rule: 'LINE_TOO_LONG',
        line: lineNum,
        message: `Qator uzunligi 80 belgidan oshmasligi kerak: ${line.length}`,
      });
    }

    // Rule 1: CHAR_NOT_ALLOWED
    for (let col = 0; col < line.length; col++) {
      const codePoint = line.charCodeAt(col);
      if (codePoint === 9) { // TAB
        if (langConf.indentStyle !== 'TAB' || col > 0) {
          errors.push({
            file: filePath,
            rule: 'INDENT_STYLE',
            line: lineNum,
            col: col + 1,
            message: `SPACE rejimida tab (\\t) taqiqlangan`,
          });
          break;
        }
      } else if (codePoint < 0x20 || codePoint > 0x7E) {
        errors.push({
          file: filePath,
          rule: 'CHAR_NOT_ALLOWED',
          line: lineNum,
          col: col + 1,
          message: `Ruxsat etilmagan belgi (ASCII 0x20-0x7E dan tashqari): 0x${codePoint.toString(16)}`,
        });
      }
    }

    // Rule 5: INDENT_STYLE
    if (langConf.indentStyle === 'SPACE' && !langConf.isRiverAllowed) {
      const leadingSpaces = line.match(/^[ ]*/)?.[0].length ?? 0;
      if (leadingSpaces % langConf.indentSize !== 0) {
        errors.push({
          file: filePath,
          rule: 'INDENT_STYLE',
          line: lineNum,
          message: `Indent ${langConf.indentSize} ga karrali bo'lishi shart: topildi ${leadingSpaces}`,
        });
      }
    }
  }

  // Rule 7: LENGTH_OUT_OF_RANGE
  const effectiveLength = calculateCharCount(code);
  const range = LEVEL_RANGES[difficulty];
  if (range && (effectiveLength < range.min || effectiveLength > range.max)) {
    errors.push({
      file: filePath,
      rule: 'LENGTH_OUT_OF_RANGE',
      message: `char_count ${range.min}..${range.max} oralig'ida bo'lishi shart, hisoblandi: ${effectiveLength}`,
    });
  }

  // Rule 8: DUPLICATE_EXERCISE
  const normalized = code.trim().replace(/\r\n/g, '\n');
  const hash = crypto.createHash('sha256').update(normalized).digest('hex');
  const langKey = `${language}:${hash}`;
  if (seenHashes.has(langKey)) {
    errors.push({
      file: filePath,
      rule: 'DUPLICATE_EXERCISE',
      message: `Dublikat kod aniqlandi (fayl: ${seenHashes.get(langKey)})`,
    });
  } else {
    seenHashes.set(langKey, filePath);
  }

  // Rule 9: FORBIDDEN_CONSTRUCT
  const forbiddenPatternsAll = [
    { re: /\bTODO\b/i, name: 'TODO' },
    { re: /\bFIXME\b/i, name: 'FIXME' },
    { re: /lorem\s+ipsum/i, name: 'lorem ipsum' },
    { re: /\bfoo\b/i, name: 'foo identifikatori' },
    { re: /\bbar\b/i, name: 'bar identifikatori' },
    { re: /\bbaz\b/i, name: 'baz identifikatori' },
  ];

  for (const p of forbiddenPatternsAll) {
    if (p.re.test(code)) {
      errors.push({
        file: filePath,
        rule: 'FORBIDDEN_CONSTRUCT',
        message: `Taqiqlangan konstruksiya topildi: ${p.name}`,
      });
    }
  }

  if (language === 'plsql') {
    const plsqlForbidden = [
      { re: /\$\$/, name: '$$ (Postgres)' },
      { re: /LANGUAGE\s+plpgsql/i, name: 'LANGUAGE plpgsql' },
      { re: /RAISE\s+NOTICE/i, name: 'RAISE NOTICE' },
      { re: /RETURNS\s+/i, name: 'RETURNS' },
      { re: /PERFORM\s+/i, name: 'PERFORM' },
      { re: /DELIMITER/i, name: 'DELIMITER' },
      { re: /@\w+\s*=/i, name: '@var = (T-SQL)' },
      { re: /DECLARE\s+@/i, name: 'DECLARE @' },
      { re: /^\s*GO\s*$/m, name: 'GO (T-SQL)' },
    ];
    for (const p of plsqlForbidden) {
      if (p.re.test(code)) {
        errors.push({
          file: filePath,
          rule: 'FORBIDDEN_CONSTRUCT',
          message: `PL/SQL da taqiqlangan konstruksiya: ${p.name}`,
        });
      }
    }
  } else if (language === 'sql') {
    const tags = parsed.tags ?? [];
    const isDialectTagged = tags.includes('oracle') || tags.includes('postgres');
    if (!isDialectTagged) {
      const sqlForbidden = [
        { re: /\bNVL\(/i, name: 'NVL(' },
        { re: /\bDECODE\(/i, name: 'DECODE(' },
        { re: /\(\+\)/, name: '(+)' },
        { re: /::\w+/, name: '::type' },
        { re: /\bILIKE\b/i, name: 'ILIKE' },
        { re: /\bTOP\s+\d+/i, name: 'TOP N' },
        { re: /`/, name: 'backtick `' },
      ];
      for (const p of sqlForbidden) {
        if (p.re.test(code)) {
          errors.push({
            file: filePath,
            rule: 'FORBIDDEN_CONSTRUCT',
            message: `Standart ANSI SQL da dialekt konstruksiyasi tegsiz taqiqlangan: ${p.name}`,
          });
        }
      }
    }
  }

  // Warnings: Notes check
  if (!note?.en || !note?.uzl || !note?.ru) {
    warnings.push({
      file: filePath,
      rule: 'NOTE_I18N_INCOMPLETE',
      message: 'note obyektida en, uzl, ru tillari to\'liq ta\'minlanishi tavsiya etiladi',
    });
  }

  return { errors, warnings };
}

export function runContentValidator(contentDir: string): { totalFiles: number; errorCount: number; warningCount: number } {
  console.log(`\n--- DevType Content Validator (docs/03 §5 BL-EM-1..4 & docs/06) ---`);
  console.log(`Scanning directory: ${contentDir}\n`);

  if (!fs.existsSync(contentDir)) {
    console.error(`Xato: ${contentDir} papkasi topilmadi.`);
    return { totalFiles: 0, errorCount: 1, warningCount: 0 };
  }

  const seenHashes = new Map<string, string>();
  let totalFiles = 0;
  let errorCount = 0;
  let warningCount = 0;

  function walkDir(dir: string) {
    const entries = fs.readdirSync(dir, { withFileTypes: true });
    for (const ent of entries) {
      const fullPath = path.join(dir, ent.name);
      if (ent.isDirectory()) {
        walkDir(fullPath);
      } else if (ent.isFile() && (ent.name.endsWith('.yaml') || ent.name.endsWith('.yml'))) {
        totalFiles++;
        const contentStr = fs.readFileSync(fullPath, 'utf-8');
        const { errors, warnings } = validateSnippet(fullPath, contentStr, seenHashes);

        if (errors.length > 0) {
          errorCount += errors.length;
          console.error(`[FAIL] ${fullPath}`);
          for (const err of errors) {
            const loc = err.line ? ` (qator ${err.line}${err.col ? `:${err.col}` : ''})` : '';
            console.error(`   - [${err.rule}]${loc}: ${err.message}`);
          }
        } else {
          console.log(`[PASS] ${fullPath}`);
        }

        if (warnings.length > 0) {
          warningCount += warnings.length;
          for (const w of warnings) {
            console.warn(`   * [WARN ${w.rule}]: ${w.message}`);
          }
        }
      }
    }
  }

  walkDir(contentDir);

  console.log(`\nJami tekshirilgan fayllar: ${totalFiles}`);
  console.log(`Xatolar: ${errorCount}`);
  console.log(`Ogohlantirishlar: ${warningCount}`);

  return { totalFiles, errorCount, warningCount };
}

// Direct execution CLI entrypoint
if (import.meta.url === `file:///${process.argv[1]?.replace(/\\/g, '/')}`) {
  const contentDir = path.resolve(process.cwd(), 'content');
  const result = runContentValidator(contentDir);
  if (result.errorCount > 0) {
    process.exit(1);
  }
}
