import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { transliterateUzlToUzc } from '../src/shared/i18n/translit';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

function translitObj(obj: unknown): unknown {
  if (typeof obj === 'string') return transliterateUzlToUzc(obj);
  if (typeof obj === 'object' && obj !== null) {
    const res: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(obj as Record<string, unknown>)) {
      res[k] = translitObj(v);
    }
    return res;
  }
  return obj;
}

const uzlPath = path.resolve(__dirname, '../src/shared/i18n/locales/uzl/common.json');
const uzcPath = path.resolve(__dirname, '../src/shared/i18n/locales/uzc/common.json');

const uzl = JSON.parse(fs.readFileSync(uzlPath, 'utf8'));
const uzc = translitObj(uzl);
fs.writeFileSync(uzcPath, JSON.stringify(uzc, null, 2), 'utf8');
console.log('uzc/common.json successfully generated from uzl/common.json');
