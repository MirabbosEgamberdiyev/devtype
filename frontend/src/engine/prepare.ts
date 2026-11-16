/**
 * Target Preparation — docs/07 §1.
 * Pure function that transforms snippet code into structured TargetChar arrays.
 */

import { TargetChar } from './types';

export interface LanguageConfig {
  indentStyle?: 'SPACE' | 'TAB';
  indentSize?: number;
  tokenPatterns?: string[];
}

/**
 * Checks if a character is considered a word character for wordId boundary detection:
 * [A-Za-z0-9_]
 */
function isWordChar(ch: string): boolean {
  if (!ch) return false;
  const code = ch.charCodeAt(0);
  return (
    (code >= 65 && code <= 90) || // A-Z
    (code >= 97 && code <= 122) || // a-z
    (code >= 48 && code <= 57) || // 0-9
    ch === '_'
  );
}

/**
 * Checks if a character is whitespace (space, tab, newline).
 */
function isWhitespace(ch: string): boolean {
  return ch === ' ' || ch === '\t' || ch === '\n';
}

/**
 * Prepares TargetChar array for a single snippet.
 */
export function prepareSingleTarget(
  rawCode: string,
  langConfig: LanguageConfig = {},
  strictIndent: boolean = false,
  exerciseSeq: number = 0
): TargetChar[] {
  // Rule 1: strip \r carriage returns
  let code = rawCode.replace(/\r/g, '');

  // Rule 4: Trailing \n after the last visible line is stripped — session ends at the last visible char
  while (code.endsWith('\n')) {
    code = code.slice(0, -1);
  }

  if (code.length === 0) {
    return [];
  }

  const lines = code.split('\n');
  const targetChars: TargetChar[] = [];

  // Sort token patterns by length descending for greedy matching (longest match first)
  const sortedPatterns = [...(langConfig.tokenPatterns || [])].filter((p) => p.length > 0);
  sortedPatterns.sort((a, b) => b.length - a.length);

  // We will build characters and then assign token patterns & wordIds
  let currentLine = 0;
  let nextPatternGroupId = 0;

  // Temporary buffer to hold line characters
  interface RawCharEntry {
    ch: string;
    line: number;
    col: number;
    auto: boolean;
  }
  const rawEntries: RawCharEntry[] = [];

  for (let l = 0; l < lines.length; l++) {
    const lineText = lines[l] ?? '';
    currentLine = l;

    // Rule 2: Automatic indentation — all leading spaces/tabs up to first non-whitespace
    // are auto = !strictIndent.
    let firstNonWsIndex = 0;
    while (
      firstNonWsIndex < lineText.length &&
      (lineText[firstNonWsIndex] === ' ' || lineText[firstNonWsIndex] === '\t')
    ) {
      firstNonWsIndex++;
    }

    for (let c = 0; c < lineText.length; c++) {
      const ch = lineText[c] ?? '';
      const isLeadingIndent = c < firstNonWsIndex;
      const auto = isLeadingIndent && !strictIndent;

      rawEntries.push({
        ch,
        line: currentLine,
        col: c,
        auto,
      });
    }

    // Rule 3: Add newline between lines (if not the last line)
    if (l < lines.length - 1) {
      rawEntries.push({
        ch: '\n',
        line: currentLine,
        col: lineText.length,
        auto: false,
      });
    }
  }

  // Assign token pattern IDs (Rule 5: greedy, left-to-right, non-overlapping)
  const patternIds: number[] = new Array(rawEntries.length).fill(-1);
  if (sortedPatterns.length > 0) {
    let i = 0;
    while (i < rawEntries.length) {
      let matched = false;
      for (const pattern of sortedPatterns) {
        const patLen = pattern.length;
        if (i + patLen <= rawEntries.length) {
          let matches = true;
          for (let p = 0; p < patLen; p++) {
            const entry = rawEntries[i + p];
            if (!entry || entry.ch !== pattern[p]) {
              matches = false;
              break;
            }
          }
          if (matches) {
            const grpId = nextPatternGroupId++;
            for (let p = 0; p < patLen; p++) {
              patternIds[i + p] = grpId;
            }
            i += patLen;
            matched = true;
            break;
          }
        }
      }
      if (!matched) {
        i++;
      }
    }
  }

  // Assign wordIds (Rule 6):
  let currentWordId = 0;
  const wordIds: number[] = new Array(rawEntries.length).fill(0);

  let prevChar: string | null = null;
  for (let i = 0; i < rawEntries.length; i++) {
    const entry = rawEntries[i];
    if (!entry) continue;
    const ch = entry.ch;

    if (i > 0) {
      const prev = prevChar!;
      if (isWhitespace(prev)) {
        if (!isWhitespace(ch)) {
          currentWordId++;
        }
      } else if (!isWhitespace(ch)) {
        const prevIsWord = isWordChar(prev);
        const currIsWord = isWordChar(ch);
        if (prevIsWord !== currIsWord) {
          currentWordId++;
        }
      }
    }

    wordIds[i] = currentWordId;
    prevChar = ch;
  }

  // Build final TargetChar array
  for (let i = 0; i < rawEntries.length; i++) {
    const entry = rawEntries[i];
    if (!entry) continue;
    targetChars.push({
      ch: entry.ch,
      line: entry.line,
      col: entry.col,
      auto: entry.auto,
      wordId: wordIds[i] ?? 0,
      patternId: patternIds[i] ?? -1,
      exerciseSeq,
    });
  }

  return targetChars;
}

/**
 * Prepares targets for multi-exercise sessions (Rule 7).
 */
export function prepareTargets(
  snippets: string[],
  langConfig: LanguageConfig = {},
  strictIndent: boolean = false
): TargetChar[][] {
  return snippets.map((snippet, idx) =>
    prepareSingleTarget(snippet, langConfig, strictIndent, idx)
  );
}
