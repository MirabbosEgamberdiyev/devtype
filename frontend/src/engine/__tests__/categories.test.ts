import { describe, it, expect } from 'vitest';
import { getCharCategory, isSpecialCategory } from '../categories';
import { CHAR_CATEGORIES, CharCategory } from '../constants';

describe('categories — docs/08 §2 & AC-SC-1', () => {
  it('covers all 95 printable ASCII characters (0x20..0x7E) exactly into valid categories', () => {
    const counts: Record<CharCategory, number> = {
      BRACKETS: 0,
      PUNCT: 0,
      ASSIGN_CMP: 0,
      ARROWS: 0,
      LOGICAL: 0,
      ARITH: 0,
      IDENT: 0,
      QUOTES: 0,
      WHITESPACE: 0,
      CASE: 0,
      ALNUM: 0,
    };

    for (let code = 0x20; code <= 0x7e; code++) {
      const ch = String.fromCharCode(code);
      const cat = getCharCategory(ch);
      expect(CHAR_CATEGORIES).toContain(cat);
      counts[cat]++;
    }

    // 95 characters total
    const total = Object.values(counts).reduce((a, b) => a + b, 0);
    expect(total).toBe(95);

    // Specific category checks according to docs/08 §2 table:
    // BRACKETS: { } [ ] ( ) < > (8 chars)
    expect(counts.BRACKETS).toBe(8);
    // PUNCT: ; : , . (4 chars)
    expect(counts.PUNCT).toBe(4);
    // ASSIGN_CMP: = (1 char)
    expect(counts.ASSIGN_CMP).toBe(1);
    // LOGICAL: ! & | ? (4 chars)
    expect(counts.LOGICAL).toBe(4);
    // ARITH: + - * / % ^ ~ (7 chars)
    expect(counts.ARITH).toBe(7);
    // IDENT: _ $ @ # (4 chars)
    expect(counts.IDENT).toBe(4);
    // QUOTES: ' " ` \ (4 chars)
    expect(counts.QUOTES).toBe(4);
    // WHITESPACE: space (1 char)
    expect(counts.WHITESPACE).toBe(1);
    // CASE: A-Z (26 chars)
    expect(counts.CASE).toBe(26);
    // ALNUM: a-z (26) + 0-9 (10) = 36 chars
    expect(counts.ALNUM).toBe(36);
  });

  it('classifies newlines and tabs as WHITESPACE', () => {
    expect(getCharCategory('\n')).toBe('WHITESPACE');
    expect(getCharCategory('\t')).toBe('WHITESPACE');
  });

  it('correctly maps known multi-character token patterns to categories', () => {
    expect(getCharCategory(':', ':=')).toBe('ASSIGN_CMP');
    expect(getCharCategory('=', '==')).toBe('ASSIGN_CMP');
    expect(getCharCategory('=', '===')).toBe('ASSIGN_CMP');
    expect(getCharCategory('!', '!=')).toBe('ASSIGN_CMP');
    expect(getCharCategory('!', '!==')).toBe('ASSIGN_CMP');
    expect(getCharCategory('<', '<=')).toBe('ASSIGN_CMP');
    expect(getCharCategory('>', '>=')).toBe('ASSIGN_CMP');
    expect(getCharCategory('<', '<>')).toBe('ASSIGN_CMP');
    expect(getCharCategory('+', '+=')).toBe('ASSIGN_CMP');
    expect(getCharCategory('-', '-=')).toBe('ASSIGN_CMP');

    expect(getCharCategory('=', '=>')).toBe('ARROWS');
    expect(getCharCategory('-', '->')).toBe('ARROWS');
    expect(getCharCategory('<', '<-')).toBe('ARROWS');

    expect(getCharCategory('&', '&&')).toBe('LOGICAL');
    expect(getCharCategory('|', '||')).toBe('LOGICAL');
    expect(getCharCategory('?', '??')).toBe('LOGICAL');
    expect(getCharCategory('?', '?.')).toBe('LOGICAL');
    expect(getCharCategory('?', '?:')).toBe('LOGICAL');

    expect(getCharCategory('+', '++')).toBe('ARITH');
    expect(getCharCategory('-', '--')).toBe('ARITH');
    expect(getCharCategory('*', '**')).toBe('ARITH');
    expect(getCharCategory('/', '//')).toBe('ARITH');
  });

  it('unlisted patterns inherit the category of their first character', () => {
    expect(getCharCategory('%', '%TYPE')).toBe('ARITH'); // '%' is ARITH
    expect(getCharCategory('$', '${')).toBe('IDENT'); // '$' is IDENT
    expect(getCharCategory('$', '$(')).toBe('IDENT');
    expect(getCharCategory('[', '[[')).toBe('BRACKETS'); // '[' is BRACKETS
    expect(getCharCategory('<', '</')).toBe('BRACKETS');
  });

  it('isSpecialCategory correctly identifies special vs standard categories', () => {
    expect(isSpecialCategory('ALNUM')).toBe(false);
    expect(isSpecialCategory('WHITESPACE')).toBe(false);
    expect(isSpecialCategory('CASE')).toBe(false);

    expect(isSpecialCategory('BRACKETS')).toBe(true);
    expect(isSpecialCategory('PUNCT')).toBe(true);
    expect(isSpecialCategory('ASSIGN_CMP')).toBe(true);
    expect(isSpecialCategory('ARROWS')).toBe(true);
    expect(isSpecialCategory('LOGICAL')).toBe(true);
    expect(isSpecialCategory('ARITH')).toBe(true);
    expect(isSpecialCategory('IDENT')).toBe(true);
    expect(isSpecialCategory('QUOTES')).toBe(true);
  });
});
