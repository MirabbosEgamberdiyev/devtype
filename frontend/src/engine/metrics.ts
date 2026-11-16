/**
 * Engine Metrics Calculations — docs/00 §4.5, docs/07 §6.
 * Single source of truth for WPM, CPM, accuracy, consistency, progress, and rounding.
 */

import { EngineState, EngineMetrics } from './types';

/**
 * Standard HALF_UP rounding to specified decimal places.
 * Handles floating-point quirks cleanly (e.g. 1.005 -> 1.01).
 */
export function roundHalfUp(value: number, decimals: number = 2): number {
  if (!Number.isFinite(value) || Number.isNaN(value)) {
    return 0;
  }
  const factor = Math.pow(10, decimals);
  // Add Number.EPSILON to avoid floating-point representation errors with .005
  const rounded = Math.round((value + Number.EPSILON) * factor) / factor;
  return Number(rounded.toFixed(decimals));
}

/**
 * Calculates population standard deviation: sigma = sqrt(sum((x - mu)^2) / N)
 */
export function populationStdDev(values: number[], mean: number): number {
  if (values.length === 0) return 0;
  const variance =
    values.reduce((acc, v) => acc + Math.pow(v - mean, 2), 0) / values.length;
  return Math.sqrt(variance);
}

/**
 * Calculates consistency percentage from per-second raw keystroke counts:
 * r_i = bosishlar_i / 5 * 60 (to'liq soniyalar; oxirgi to'liqsiz soniya tashlanadi)
 * clamp(100 * (1 - sigma / mu), 0, 100)
 * Boundary: < 3 full seconds or mu == 0 -> 0
 */
export function calculateConsistency(perSecondKeystrokes: number[]): number {
  // We need at least 3 full seconds of data
  if (perSecondKeystrokes.length < 3) {
    return 0;
  }

  // Convert keystrokes per second to raw WPM: (count / 5) * 60
  const r = perSecondKeystrokes.map((count) => (count / 5) * 60);
  const n = r.length;
  const sum = r.reduce((a, b) => a + b, 0);
  const mu = sum / n;

  if (mu <= 0) {
    return 0;
  }

  const sigma = populationStdDev(r, mu);
  const consistency = 100 * (1 - sigma / mu);

  const clamped = Math.max(0, Math.min(100, consistency));
  return roundHalfUp(clamped, 2);
}

/**
 * Calculates all metrics for an EngineState.
 */
export function calculateMetrics(state: EngineState): EngineMetrics {
  const currentTargets = state.targets[state.seq] || [];
  const currentStates = state.states;

  // Total typable characters across all snippets
  let totalTypableAllSnippets = 0;
  for (const snippetTargets of state.targets) {
    for (const t of snippetTargets) {
      if (!t.auto) {
        totalTypableAllSnippets++;
      }
    }
  }

  // Count correct and corrected characters in current snippet
  let currentCorrectCount = 0;
  let uncorrectedErrors = 0;
  let typedPositions = 0;

  // Track word completion if timeout occurred
  const isTimeout = state.log.length > 0 && state.log[state.log.length - 1]?.k === 'timeout';
  let incompleteWordId: number | null = null;

  if (isTimeout && state.cursor < currentTargets.length) {
    // Current word where cursor is stopped
    const cursorWordId = currentTargets[state.cursor]?.wordId;
    if (cursorWordId !== undefined) {
      // Check if this word was completely typed
      const wordChars = currentTargets
        .map((tc, idx) => ({ tc, idx }))
        .filter((entry) => entry.tc.wordId === cursorWordId && !entry.tc.auto);

      const isCompleted = wordChars.every(
        (entry) =>
          currentStates[entry.idx] === 'correct' ||
          currentStates[entry.idx] === 'corrected'
      );

      if (!isCompleted) {
        incompleteWordId = cursorWordId;
      }
    }
  }

  for (let i = 0; i < currentTargets.length; i++) {
    const tc = currentTargets[i];
    if (!tc) continue;
    const isAuto = tc.auto;
    const s = currentStates[i];

    if (!isAuto) {
      if (s !== 'pending') {
        typedPositions++;
      }

      if (s === 'correct' || s === 'corrected') {
        // If timed out with an incomplete word, exclude that word's characters from C
        if (incompleteWordId !== null && tc.wordId === incompleteWordId) {
          // Excluded
        } else {
          currentCorrectCount++;
        }
      } else if (s === 'incorrect') {
        uncorrectedErrors++;
      }
    }
  }

  // C: Finished snippets correct chars + current snippet correct chars
  const C = state.finishedCorrect + currentCorrectCount;
  const T = state.totalTyped;
  const K_correct = state.correctKeys;
  const K_incorrect = state.incorrectKeys;

  // Active time in minutes: m = activeMs / 60000
  // In SPEED_TEST with timeout, m = limitMs / 60000 exactly
  let activeMinutes: number;
  if (state.mode === 'SPEED_TEST' && isTimeout && state.limitMs) {
    activeMinutes = state.limitMs / 60000;
  } else {
    activeMinutes = state.activeMs / 60000;
  }

  // WPM (net): (C / 5) / m
  const wpmRaw = activeMinutes > 0 ? C / 5 / activeMinutes : 0;
  const wpm = roundHalfUp(wpmRaw, 2);

  // Raw WPM: (T / 5) / m
  const rawWpmVal = activeMinutes > 0 ? T / 5 / activeMinutes : 0;
  const rawWpm = roundHalfUp(rawWpmVal, 2);

  // CPM: C / m
  const cpmVal = activeMinutes > 0 ? C / activeMinutes : 0;
  const cpm = roundHalfUp(cpmVal, 2);

  // Accuracy %: K_correct / (K_correct + K_incorrect) * 100
  const totalKeys = K_correct + K_incorrect;
  const accuracyVal = totalKeys > 0 ? (K_correct / totalKeys) * 100 : 0;
  const accuracy = roundHalfUp(accuracyVal, 2);

  // Consistency %
  const consistency = calculateConsistency(state.perSecond);

  // Completion %
  let completionPct = 0;
  let progressPct = 0;
  if (totalTypableAllSnippets > 0) {
    progressPct = roundHalfUp((typedPositions / totalTypableAllSnippets) * 100, 2);
    if (state.phase === 'finished' && state.seq >= state.targets.length - 1 && uncorrectedErrors === 0) {
      completionPct = 100;
    } else {
      completionPct = roundHalfUp((C / totalTypableAllSnippets) * 100, 2);
    }
  }

  return {
    wpm,
    rawWpm,
    cpm,
    accuracy,
    consistency,
    elapsedMs: state.activeMs,
    totalTyped: T,
    correctChars: C,
    uncorrectedErrors,
    correctKeys: K_correct,
    incorrectKeys: K_incorrect,
    backspaceCount: state.backspaces,
    completionPct,
    progressPct,
  };
}

/**
 * Calculates live WPM over a rolling 10s window (docs/07 §6).
 * Returns null if active time is under 3 seconds.
 */
export function calculateLiveWpm(
  correctCharsInWindow: number,
  windowDurationMs: number,
  totalActiveMs: number
): number | null {
  if (totalActiveMs < 3000 || windowDurationMs <= 0) {
    return null;
  }
  const windowMinutes = windowDurationMs / 60000;
  const live = correctCharsInWindow / 5 / windowMinutes;
  return roundHalfUp(live, 2);
}

/**
 * Formats WPM for UI display (docs/00 §4.5: WPM is an integer).
 */
export function formatUiWpm(wpm: number): string {
  return Math.round(wpm).toString();
}

/**
 * Formats Accuracy for UI display (docs/00 §4.5: Accuracy has 1 decimal place).
 */
export function formatUiAccuracy(accuracy: number): string {
  return roundHalfUp(accuracy, 1).toFixed(1) + '%';
}
