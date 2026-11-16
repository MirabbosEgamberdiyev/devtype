import { describe, it, expect } from 'vitest';
import fs from 'fs';
import path from 'path';
import { createInitialState, reduce } from '../reducer';
import { calculateMetrics } from '../metrics';
import { EngineState, KeyEvent } from '../types';

interface VectorData {
  name: string;
  language: {
    indentStyle: 'SPACE' | 'TAB';
    indentSize: number;
    tokenPatterns: string[];
  };
  mode: 'PRACTICE' | 'SPEED_TEST' | 'ACCURACY' | 'CHALLENGE' | 'SPECIAL_CHARS' | 'WEAKNESS' | 'CODE_SPRINT';
  limitMs: number | null;
  strictIndent: boolean;
  exercises: string[];
  events: KeyEvent[];
  expect: {
    phase: string;
    cursor: number;
    seq: number;
    states: string;
    metrics: {
      wpm: number;
      rawWpm: number;
      accuracy: number;
      correctChars: number;
      errors: number;
      backspaces: number;
    };
  };
}

describe('engine-vectors — AC-TE-1 (Frontend Execution)', () => {
  const vectorsDir = path.resolve(__dirname, '../../../../engine-vectors');
  const files = fs
    .readdirSync(vectorsDir)
    .filter((f) => f.endsWith('.json'))
    .sort();

  expect(files.length).toBeGreaterThanOrEqual(40);

  function statesToString(state: EngineState): string {
    const currentTargets = state.targets[state.seq] || [];
    return currentTargets
      .map((t, idx) => {
        if (t.auto) return 'a';
        const s = state.states[idx];
        if (s === 'correct') return 'c';
        if (s === 'incorrect') return 'i';
        if (s === 'corrected') return 'r';
        return 'p';
      })
      .join('');
  }

  for (const file of files) {
    const filePath = path.join(vectorsDir, file);
    const raw = fs.readFileSync(filePath, 'utf-8');
    const v: VectorData = JSON.parse(raw);

    it(`Vector: ${v.name} (${file})`, () => {
      let state = createInitialState({
        snippets: v.exercises,
        mode: v.mode,
        strictIndent: v.strictIndent,
        limitSeconds: v.limitMs ? v.limitMs / 1000 : null,
        indentStyle: v.language.indentStyle,
        indentSize: v.language.indentSize,
        tokenPatterns: v.language.tokenPatterns,
      });

      for (const ev of v.events) {
        state = reduce(state, ev);
      }

      // Check phase, cursor, seq
      expect(state.phase).toBe(v.expect.phase);
      expect(state.cursor).toBe(v.expect.cursor);
      expect(state.seq).toBe(v.expect.seq);

      // Check states representation
      const actualStates = statesToString(state);
      expect(actualStates).toBe(v.expect.states);

      // Check metrics
      const m = calculateMetrics(state);
      expect(m.wpm).toBe(v.expect.metrics.wpm);
      expect(m.accuracy).toBe(v.expect.metrics.accuracy);
      expect(m.correctChars).toBe(v.expect.metrics.correctChars);
      expect(m.incorrectKeys).toBe(v.expect.metrics.errors);
      expect(m.backspaceCount).toBe(v.expect.metrics.backspaces);
    });
  }
});
