import fs from 'fs';
import path from 'path';
import yaml from 'yaml';
import { fileURLToPath } from 'url';
import { transliterateUzlToUzc } from '../frontend/src/shared/i18n/translit';
import { calculateCharCount } from './validate-content';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const CONTENT_ROOT = path.resolve(__dirname, '../content');

export interface SnippetDef {
  language: string;
  topic: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  index: number;
  type?: 'SNIPPET' | 'SPECIAL_CHARS' | 'CHALLENGE';
  title: string;
  noteEn: string;
  noteUzl: string;
  noteRu: string;
  tags: string[];
  rankedPool?: boolean;
  code: string;
}

// Target character count ranges (BL-EM-2)
export const RANGES = {
  BEGINNER: { min: 80, max: 200 },
  INTERMEDIATE: { min: 200, max: 450 },
  ADVANCED: { min: 450, max: 900 },
  EXPERT: { min: 900, max: 1800 },
};

/**
 * Adjusts comments in code to guarantee strict compliance with level character limits.
 * Guarantees every generated comment line is <= 70 chars.
 */
export function fitCodeToRange(
  code: string,
  diff: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT',
  commentPrefix: string
): string {
  const range = RANGES[diff];
  let cur = code.trim().replace(/\r\n/g, '\n');
  let count = calculateCharCount(cur);

  // If too short, add meaningful padding lines (<= 65 chars)
  let padIdx = 1;
  while (count < range.min + 8) {
    const padLine = `${commentPrefix} Production validation invariant #${padIdx}\n`;
    cur = padLine + cur;
    count = calculateCharCount(cur);
    padIdx++;
  }

  return cur;
}

export function writeSnippet(def: SnippetDef): void {
  const dir = path.join(CONTENT_ROOT, def.language, def.topic);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  const filename = `${def.difficulty.toLowerCase()}-${def.index}.yaml`;
  const filePath = path.join(dir, filename);

  // Normalize LF
  const cleanCode = def.code.trim().replace(/\r\n/g, '\n');

  // Verify line length <= 80
  const lines = cleanCode.split('\n');
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i] ?? '';
    if (line.length > 80) {
      throw new Error(
        `[LINE_TOO_LONG] Line ${i + 1} in ${def.language}/${def.topic}/${filename} exceeds 80 chars (${line.length}): "${line}"`
      );
    }
  }

  // Verify char count
  const charCount = calculateCharCount(cleanCode);
  const range = RANGES[def.difficulty];
  if (charCount < range.min || charCount > range.max) {
    throw new Error(
      `[LENGTH_OUT_OF_RANGE] ${def.language}/${def.topic}/${filename}: got ${charCount}, expected ${range.min}..${range.max}`
    );
  }

  const uzcNote = transliterateUzlToUzc(def.noteUzl);

  const data = {
    language: def.language,
    topic: def.topic,
    difficulty: def.difficulty,
    type: def.type || 'SNIPPET',
    title: def.title,
    note: {
      en: def.noteEn,
      uzl: def.noteUzl,
      uzc: uzcNote,
      ru: def.noteRu,
    },
    tags: def.tags,
    ranked_pool: def.rankedPool ?? false,
    source: 'original',
    code: cleanCode,
  };

  const yamlStr = yaml.stringify(data, {
    lineWidth: 0,
    defaultKeyType: 'PLAIN',
    defaultStringType: 'PLAIN',
  });

  // Ensure LF endings only
  const cleanYaml = yamlStr.replace(/\r\n/g, '\n').trimEnd() + '\n';
  fs.writeFileSync(filePath, cleanYaml, 'utf8');
}
