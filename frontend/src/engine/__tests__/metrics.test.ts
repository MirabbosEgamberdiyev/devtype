import { describe, it, expect } from 'vitest';
import {
  calculateMetrics,
  calculateConsistency,
  roundHalfUp,
  formatUiWpm,
  formatUiAccuracy,
} from '../metrics';
import { EngineState } from '../types';

describe('metrics — docs/00 §4.5, docs/07 §6 & AC-TE-3, AC-TE-9', () => {
  function makeMockState(overrides: Partial<EngineState> = {}): EngineState {
    return {
      phase: 'finished',
      mode: 'PRACTICE',
      errorMode: 'stop',
      targets: [
        [
          {
            ch: 'a',
            line: 0,
            col: 0,
            auto: false,
            wordId: 0,
            patternId: -1,
            exerciseSeq: 0,
          },
        ],
      ],
      seq: 0,
      states: ['correct'],
      typed: [null],
      everWrong: [false],
      cursor: 1,
      consecutiveErrors: 0,
      activeMs: 60000, // 1 minute
      lastEventAt: 60000,
      limitMs: null,
      correctKeys: 250,
      incorrectKeys: 0,
      backspaces: 0,
      totalTyped: 250,
      finishedCorrect: 0,
      perSecond: [4, 4, 4, 4],
      log: [],
      ...overrides,
    };
  }

  describe('AC-TE-3: 10 documented manual calculation cases', () => {
    // Case 1: Standard 50 WPM test (250 chars in 1 min, 0 errors)
    // C=250, m=1.0. WPM = (250/5)/1 = 50.00. Acc = 100.00%.
    it('Case 1: 250 chars in 60s, 0 errors -> 50 WPM, 100% acc', () => {
      const state = makeMockState({
        activeMs: 60000,
        correctKeys: 250,
        incorrectKeys: 0,
        totalTyped: 250,
        finishedCorrect: 249, // 249 + 1 in current snippet = 250
      });
      const m = calculateMetrics(state);
      expect(m.wpm).toBe(50.0);
      expect(m.rawWpm).toBe(50.0);
      expect(m.cpm).toBe(250.0);
      expect(m.accuracy).toBe(100.0);
    });

    // Case 2: 120 WPM test (600 chars in 1 min, 20 errors)
    // K_v = 600, K_x = 20. Total keys = 620. Acc = (600/620)*100 = 96.774... -> 96.77%.
    // C = 600, m = 1. WPM = (600/5)/1 = 120.00. Raw = (620/5)/1 = 124.00. CPM = 600.00.
    it('Case 2: 600 correct keys, 20 errors in 60s -> 120 WPM, 124 raw, 96.77% acc', () => {
      const state = makeMockState({
        activeMs: 60000,
        correctKeys: 600,
        incorrectKeys: 20,
        totalTyped: 620,
        finishedCorrect: 599,
      });
      const m = calculateMetrics(state);
      expect(m.wpm).toBe(120.0);
      expect(m.rawWpm).toBe(124.0);
      expect(m.cpm).toBe(600.0);
      expect(m.accuracy).toBe(96.77);
    });

    // Case 3: 30-second test with 150 correct chars, 0 errors
    // C=150, m = 30000/60000 = 0.5 min. WPM = (150/5)/0.5 = 60.00. CPM = 150/0.5 = 300.00.
    it('Case 3: 150 chars in 30s -> 60 WPM, 300 CPM', () => {
      const state = makeMockState({
        activeMs: 30000,
        correctKeys: 150,
        incorrectKeys: 0,
        totalTyped: 150,
        finishedCorrect: 149,
      });
      const m = calculateMetrics(state);
      expect(m.wpm).toBe(60.0);
      expect(m.cpm).toBe(300.0);
      expect(m.accuracy).toBe(100.0);
    });

    // Case 4: 15 seconds test (100 chars, 5 errors)
    // C=100, m = 15000/60000 = 0.25 min.
    // WPM = (100/5)/0.25 = 80.00.
    // Raw WPM = (105/5)/0.25 = 84.00.
    // Acc = 100/105 * 100 = 95.238... -> 95.24%.
    it('Case 4: 100 chars in 15s with 5 errors -> 80 WPM, 84 raw, 95.24% acc', () => {
      const state = makeMockState({
        activeMs: 15000,
        correctKeys: 100,
        incorrectKeys: 5,
        totalTyped: 105,
        finishedCorrect: 99,
      });
      const m = calculateMetrics(state);
      expect(m.wpm).toBe(80.0);
      expect(m.rawWpm).toBe(84.0);
      expect(m.accuracy).toBe(95.24);
    });

    // Case 5: Zero active time boundary
    it('Case 5: activeMs = 0 -> all rates 0.00, no division by zero', () => {
      const state = makeMockState({
        activeMs: 0,
        correctKeys: 0,
        incorrectKeys: 0,
        totalTyped: 0,
        finishedCorrect: 0,
        states: ['pending'],
      });
      const m = calculateMetrics(state);
      expect(m.wpm).toBe(0);
      expect(m.rawWpm).toBe(0);
      expect(m.cpm).toBe(0);
      expect(m.accuracy).toBe(0);
    });

    // Case 6: Precision rounding edge case: 1.005 -> 1.01 (HALF_UP)
    it('Case 6: HALF_UP rounding for 1.005 -> 1.01', () => {
      expect(roundHalfUp(1.005, 2)).toBe(1.01);
      expect(roundHalfUp(1.004, 2)).toBe(1.0);
      expect(roundHalfUp(25.555, 2)).toBe(25.56);
      expect(roundHalfUp(25.554, 2)).toBe(25.55);
    });

    // Case 7: Speed test 60s limit with timeout
    // In speed test, timeout sets m = limitMs / 60000 exactly
    it('Case 7: SPEED_TEST mode timeout with 240 chars -> 48.00 WPM', () => {
      const state = makeMockState({
        mode: 'SPEED_TEST',
        limitMs: 60000,
        activeMs: 60230, // slightly more activeMs due to network/timer tick
        correctKeys: 240,
        incorrectKeys: 0,
        totalTyped: 240,
        finishedCorrect: 239,
        log: [{ t: 60000, k: 'timeout' }],
      });
      const m = calculateMetrics(state);
      // activeMinutes must use limitMs=60000 -> m = 1.0
      expect(m.wpm).toBe(48.0);
    });

    // Case 8: Consistency calculation with exact population std dev
    // 4 seconds with values: [5, 5, 5, 5] -> mean=60, sigma=0 -> consistency = 100.00%
    it('Case 8: perfectly uniform typing -> 100% consistency', () => {
      const cons = calculateConsistency([5, 5, 5, 5]);
      expect(cons).toBe(100.0);
    });

    // Case 9: Consistency with variance
    // perSecond = [10, 2, 8, 4] keystrokes -> raw WPM r = [120, 24, 96, 48].
    // mu = (120+24+96+48)/4 = 72.
    // diffs = [48, -48, 24, -24].
    // diffs^2 = [2304, 2304, 576, 576] -> sum = 5760.
    // variance = 5760 / 4 = 1440.
    // sigma = sqrt(1440) = 37.94733...
    // 100 * (1 - sigma / mu) = 100 * (1 - 37.94733 / 72) = 100 * (1 - 0.527046) = 47.295...
    // HALF_UP -> 47.30%.
    it('Case 9: non-uniform typing consistency calculation -> 47.30%', () => {
      const cons = calculateConsistency([10, 2, 8, 4]);
      expect(cons).toBe(47.3);
    });

    // Case 10: Consistency boundary (< 3 seconds -> 0)
    it('Case 10: < 3 full seconds of data -> consistency = 0', () => {
      expect(calculateConsistency([5, 5])).toBe(0);
      expect(calculateConsistency([])).toBe(0);
      expect(calculateConsistency([0, 0, 0])).toBe(0); // mu = 0 -> 0
    });
  });

  describe('AC-TE-9: UI Formatting', () => {
    it('formats UI WPM as integer (rounded) and UI Accuracy with 1 decimal', () => {
      expect(formatUiWpm(74.49)).toBe('74');
      expect(formatUiWpm(74.5)).toBe('75');
      expect(formatUiWpm(74.99)).toBe('75');

      expect(formatUiAccuracy(98.74)).toBe('98.7%');
      expect(formatUiAccuracy(98.75)).toBe('98.8%');
      expect(formatUiAccuracy(100.0)).toBe('100.0%');
    });
  });
});
