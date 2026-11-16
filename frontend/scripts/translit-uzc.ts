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

const files = ['common.json', 'auth.json'];

for (const file of files) {
  const uzlPath = path.resolve(__dirname, `../src/shared/i18n/locales/uzl/${file}`);
  const uzcPath = path.resolve(__dirname, `../src/shared/i18n/locales/uzc/${file}`);

  if (fs.existsSync(uzlPath)) {
    const uzl = JSON.parse(fs.readFileSync(uzlPath, 'utf8'));
    const uzc = translitObj(uzl);
    fs.writeFileSync(uzcPath, JSON.stringify(uzc, null, 2), 'utf8');
    console.log(`Generated uzc/${file} from uzl/${file}`);
  }
}
