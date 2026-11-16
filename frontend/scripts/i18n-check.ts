import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const LOCALES = ['en', 'uzl', 'uzc', 'ru'];
const LOCALES_DIR = path.resolve(__dirname, '../src/shared/i18n/locales');

function getKeys(obj: Record<string, unknown>, prefix = ''): string[] {
  let keys: string[] = [];
  for (const [k, v] of Object.entries(obj)) {
    const fullKey = prefix ? `${prefix}.${k}` : k;
    if (v && typeof v === 'object' && !Array.isArray(v)) {
      keys = keys.concat(getKeys(v as Record<string, unknown>, fullKey));
    } else {
      keys.push(fullKey);
    }
  }
  return keys;
}

function checkApostrophes(obj: Record<string, unknown>, pathPrefix = ''): string[] {
  const errors: string[] = [];
  for (const [k, v] of Object.entries(obj)) {
    const fullKey = pathPrefix ? `${pathPrefix}.${k}` : k;
    if (typeof v === 'string') {
      const invalidRegex = /(o['‘`]|g['‘`])/i;
      if (invalidRegex.test(v)) {
        errors.push(`Invalid apostrophe in ${fullKey}: "${v}" (use U+02BB for oʻ/gʻ)`);
      }
    } else if (v && typeof v === 'object' && !Array.isArray(v)) {
      errors.push(...checkApostrophes(v as Record<string, unknown>, fullKey));
    }
  }
  return errors;
}

function main() {
  console.log('--- DevType i18n Verification ---');
  let hasErrors = false;

  const namespaces = ['common'];

  for (const ns of namespaces) {
    const enFile = path.join(LOCALES_DIR, 'en', `${ns}.json`);
    if (!fs.existsSync(enFile)) {
      console.error(`Source locale file not found: ${enFile}`);
      process.exit(1);
    }

    const enContent = JSON.parse(fs.readFileSync(enFile, 'utf-8'));
    const enKeys = new Set(getKeys(enContent));

    for (const locale of LOCALES) {
      if (locale === 'en') continue;

      const locFile = path.join(LOCALES_DIR, locale, `${ns}.json`);
      if (!fs.existsSync(locFile)) {
        console.error(`Missing translation file for ${locale}: ${locFile}`);
        hasErrors = true;
        continue;
      }

      const locContent = JSON.parse(fs.readFileSync(locFile, 'utf-8'));
      const locKeys = new Set(getKeys(locContent));

      const missing: string[] = [];
      for (const k of enKeys) {
        if (!locKeys.has(k)) {
          missing.push(k);
        }
      }

      if (missing.length > 0) {
        console.error(`[FAIL] ${locale}/${ns}.json missing ${missing.length} keys:\n  - ${missing.join('\n  - ')}`);
        hasErrors = true;
      } else {
        console.log(`[PASS] ${locale}/${ns}.json: 100% keys match source (${enKeys.size} keys)`);
      }

      if (locale === 'uzl') {
        const apostropheErrors = checkApostrophes(locContent);
        if (apostropheErrors.length > 0) {
          console.error(`[FAIL] O'zbek lotin imlo qoidalari buzilgan:\n  - ${apostropheErrors.join('\n  - ')}`);
          hasErrors = true;
        } else {
          console.log(`[PASS] uzl/${ns}.json: O'zbek lotin imlo va apostroflar to'g'ri (U+02BB)`);
        }
      }
    }
  }

  if (hasErrors) {
    console.error('\ni18n verification FAILED.');
    process.exit(1);
  } else {
    console.log('\ni18n verification SUCCEEDED across all 4 locales!');
  }
}

main();
