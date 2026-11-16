/**
 * Character and Token Pattern Categories — docs/08 §2.
 * Strictly maps every printable ASCII character (0x20..0x7E), '\n', '\t'
 * and token patterns to exactly one category.
 */

import { CharCategory } from './constants';

const KNOWN_PATTERN_CATEGORIES: Record<string, CharCategory> = {
  // ASSIGN_CMP: := == === != !== <= >= <> += -=
  ':=': 'ASSIGN_CMP',
  '==': 'ASSIGN_CMP',
  '===': 'ASSIGN_CMP',
  '!=': 'ASSIGN_CMP',
  '!==': 'ASSIGN_CMP',
  '<=': 'ASSIGN_CMP',
  '>=': 'ASSIGN_CMP',
  '<>': 'ASSIGN_CMP',
  '+=': 'ASSIGN_CMP',
  '-=': 'ASSIGN_CMP',

  // ARROWS: => -> <-
  '=>': 'ARROWS',
  '->': 'ARROWS',
  '<-': 'ARROWS',

  // LOGICAL: && || ?? ?. ?:
  '&&': 'LOGICAL',
  '||': 'LOGICAL',
  '??': 'LOGICAL',
  '?.': 'LOGICAL',
  '?:': 'LOGICAL',

  // ARITH: ++ -- ** //
  '++': 'ARITH',
  '--': 'ARITH',
  '**': 'ARITH',
  '//': 'ARITH',
};

/**
 * Maps single characters to their canonical CharCategory.
 */
function getSingleCharCategory(ch: string): CharCategory {
  if (ch === ' ' || ch === '\n' || ch === '\t') {
    return 'WHITESPACE';
  }

  const code = ch.charCodeAt(0);

  // A-Z (0x41..0x5A)
  if (code >= 65 && code <= 90) {
    return 'CASE';
  }

  // a-z (0x61..0x7A) or 0-9 (0x30..0x39)
  if ((code >= 97 && code <= 122) || (code >= 48 && code <= 57)) {
    return 'ALNUM';
  }

  // BRACKETS: { } [ ] ( ) < >
  if (
    ch === '{' ||
    ch === '}' ||
    ch === '[' ||
    ch === ']' ||
    ch === '(' ||
    ch === ')' ||
    ch === '<' ||
    ch === '>'
  ) {
    return 'BRACKETS';
  }

  // PUNCT: ; : , .
  if (ch === ';' || ch === ':' || ch === ',' || ch === '.') {
    return 'PUNCT';
  }

  // ASSIGN_CMP: =
  if (ch === '=') {
    return 'ASSIGN_CMP';
  }

  // LOGICAL: ! & | ?
  if (ch === '!' || ch === '&' || ch === '|' || ch === '?') {
    return 'LOGICAL';
  }

  // ARITH: + - * / % ^ ~
  if (
    ch === '+' ||
    ch === '-' ||
    ch === '*' ||
    ch === '/' ||
    ch === '%' ||
    ch === '^' ||
    ch === '~'
  ) {
    return 'ARITH';
  }

  // IDENT: _ $ @ #
  if (ch === '_' || ch === '$' || ch === '@' || ch === '#') {
    return 'IDENT';
  }

  // QUOTES: ' " ` \
  if (ch === "'" || ch === '"' || ch === '`' || ch === '\\') {
    return 'QUOTES';
  }

  // Fallback (for any other single ASCII)
  return 'ALNUM';
}

/**
 * Returns the category for a character, taking optional token pattern context into account.
 * (docs/08 §2: Pattern category takes precedence; unlisted patterns inherit first char's category).
 */
export function getCharCategory(ch: string, pattern?: string | null): CharCategory {
  if (pattern && pattern.length > 1) {
    const known = KNOWN_PATTERN_CATEGORIES[pattern];
    if (known) {
      return known;
    }
    // Unlisted pattern: inherit category of its first character
    return getSingleCharCategory(pattern[0]!);
  }

  return getSingleCharCategory(ch);
}

/**
 * Determines whether a category is considered a "special character" category.
 * (docs/08 §2: ALNUM, WHITESPACE, CASE are NOT special; the other 8 are).
 */
export function isSpecialCategory(category: CharCategory): boolean {
  return (
    category !== 'ALNUM' &&
    category !== 'WHITESPACE' &&
    category !== 'CASE'
  );
}
