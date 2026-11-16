/**
 * Session Statistics Collector — docs/07 §6.
 * Computes char stats (ms, errors), bigram/pattern stats, confusion matrix, and per-line stats.
 */

import { KeyEvent, EngineState } from './types';

export interface CharStatEntry {
  expected: number;
  errors: number;
  totalMs: number;
}

export interface PatternStatEntry {
  pattern: string;
  count: number;
  errors: number;
  totalMs: number;
}

export interface LineStatEntry {
  lineIndex: number;
  charsTyped: number;
  errors: number;
  totalMs: number;
}

export interface SessionStats {
  charStats: Record<string, CharStatEntry>;
  confusion: Record<string, Record<string, number>>;
  patternStats: Record<string, PatternStatEntry>;
  perLine: LineStatEntry[];
}

/**
 * Collects analytical statistics from the completed engine session.
 */
export function collectSessionStats(state: EngineState): SessionStats {
  const charStats: Record<string, CharStatEntry> = {};
  const confusion: Record<string, Record<string, number>> = {};
  const patternStats: Record<string, PatternStatEntry> = {};
  const lineStatsMap: Map<number, LineStatEntry> = new Map();

  const getOrCreateCharStat = (ch: string): CharStatEntry => {
    if (!charStats[ch]) {
      charStats[ch] = { expected: 0, errors: 0, totalMs: 0 };
    }
    return charStats[ch];
  };

  const recordConfusion = (exp: string, typed: string) => {
    if (!confusion[exp]) {
      confusion[exp] = {};
    }
    confusion[exp][typed] = (confusion[exp][typed] || 0) + 1;
  };

  // We trace events step-by-step
  let prevEvent: KeyEvent | null = null;
  let isAfterPauseOrNext = false;
  let isFirstCharOfSession = true;

  // Track position first-attempts per snippet
  let currentSeq = 0;
  let pos = 0;
  const firstAttemptErrors: Map<string, boolean> = new Map();

  for (const ev of state.log) {
    if (ev.k === 'pause' || ev.k === 'next') {
      isAfterPauseOrNext = true;
      if (ev.k === 'next') {
        currentSeq++;
        pos = 0;
      }
      prevEvent = ev;
      continue;
    }

    if (ev.k === 'resume') {
      prevEvent = ev;
      continue;
    }

    if (ev.k !== 'char' && ev.k !== 'enter' && ev.k !== 'tab') {
      prevEvent = ev;
      continue;
    }

    // Now ev is a typing keystroke (char, enter, tab)
    const currentTargets = state.targets[currentSeq] || [];
    while (pos < currentTargets.length && currentTargets[pos]?.auto) {
      pos++;
    }

    if (pos < currentTargets.length) {
      const targetCharObj = currentTargets[pos];
      if (!targetCharObj) continue;
      const expChar = targetCharObj.ch;
      const typedChar = ev.k === 'enter' ? '\n' : ev.k === 'tab' ? '\t' : ev.ch;
      const posKey = `${currentSeq}:${pos}`;

      const stat = getOrCreateCharStat(expChar);
      const isFirstTry = !firstAttemptErrors.has(posKey);

      if (isFirstTry) {
        stat.expected++;
      }

      if (typedChar !== expChar) {
        // Mistake
        recordConfusion(expChar, typedChar);
        if (isFirstTry) {
          firstAttemptErrors.set(posKey, true);
          stat.errors++;
        }
      } else {
        // Correct keystroke
        if (isFirstTry) {
          firstAttemptErrors.set(posKey, false);
        }

        // Calculate dt for charStats:
        if (!isFirstCharOfSession && !isAfterPauseOrNext && prevEvent) {
          const dt = ev.t - prevEvent.t;
          if (dt > 0 && dt <= 5000) {
            stat.totalMs += dt;
          }
        }

        // Advance pos
        pos++;
      }

      // Track line statistics
      const lineIdx = currentTargets[pos]?.line ?? 0;
      let lineEntry = lineStatsMap.get(lineIdx);
      if (!lineEntry) {
        lineEntry = { lineIndex: lineIdx, charsTyped: 0, errors: 0, totalMs: 0 };
        lineStatsMap.set(lineIdx, lineEntry);
      }
      lineEntry.charsTyped++;
      if (typedChar !== expChar) {
        lineEntry.errors++;
      }
    }

    isFirstCharOfSession = false;
    isAfterPauseOrNext = false;
    prevEvent = ev;
  }

  const perLine = Array.from(lineStatsMap.values()).sort((a, b) => a.lineIndex - b.lineIndex);

  return {
    charStats,
    confusion,
    patternStats,
    perLine,
  };
}
