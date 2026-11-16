import { describe, it, expect } from 'vitest';
import { transliterateUzlToUzc } from '../translit';

describe('Oʻzbek lotin -> kirill transliteratsiyasi (docs/22 §6)', () => {
  it('oddiy soʻzlarni toʻgʻri transliteratsiya qiladi', () => {
    expect(transliterateUzlToUzc('salom')).toBe('салом');
    expect(transliterateUzlToUzc('dasturchi')).toBe('дастурчи');
  });

  it('oʻ va gʻ harflarini toʻgʻri oʻgiring', () => {
    expect(transliterateUzlToUzc('oʻzbek')).toBe('ўзбек');
    expect(transliterateUzlToUzc('Oʻzbekiston')).toBe('Ўзбекистон');
    expect(transliterateUzlToUzc('gʻolib')).toBe('ғолиб');
    expect(transliterateUzlToUzc('Gʻalaba')).toBe('Ғалаба');
  });

  it('digraflarni (sh, ch, ng) toʻgʻri oʻgiradi', () => {
    expect(transliterateUzlToUzc('shahar')).toBe('шаҳар');
    expect(transliterateUzlToUzc('chiroyli')).toBe('чиройли');
    expect(transliterateUzlToUzc('tong')).toBe('тонг');
  });

  it('yolashgan unlilarni toʻgʻri oʻgiradi', () => {
    expect(transliterateUzlToUzc('yozuvchi')).toBe('ёзувчи');
    expect(transliterateUzlToUzc('yulduz')).toBe('юлдуз');
    expect(transliterateUzlToUzc('yaxshi')).toBe('яхши');
    expect(transliterateUzlToUzc('yetti')).toBe('етти');
  });

  it('e harfini soʻz boshida va oʻrtasida toʻgʻri ajratadi', () => {
    expect(transliterateUzlToUzc('elak')).toBe('элак');
    expect(transliterateUzlToUzc('kelak')).toBe('келак');
    expect(transliterateUzlToUzc('aeroport')).toBe('аэропорт');
  });

  it('istisnolarni toʻgʻri oʻgiradi (ts -> ц)', () => {
    expect(transliterateUzlToUzc('funksiya')).toBe('функция');
    expect(transliterateUzlToUzc('sirkulyatsiya')).toBe('циркуляция');
  });

  it('himoyalangan atamalar va ICU oʻzgaruvchilarini saqlaydi', () => {
    expect(transliterateUzlToUzc('WPM tezligi')).toBe('WPM тезлиги');
    expect(transliterateUzlToUzc('Java va SQL mashqi')).toBe('Java ва SQL машқи');
    expect(transliterateUzlToUzc('{count} ta xato topildi')).toBe('{count} та хато топилди');
    expect(transliterateUzlToUzc('Kodni `git push` qiling')).toBe('Кодни `git push` қилинг');
  });
});
