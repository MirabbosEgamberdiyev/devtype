/**
 * Engine Public API — docs/07 §12.
 * Clean, decoupled engine instance with dispatch, subscribe, metrics, and exportKeylog.
 */

import { EngineConfig, EngineState, KeyEvent, EngineMetrics } from './types';
import { createInitialState, reduce } from './reducer';
import { calculateMetrics, calculateLiveWpm } from './metrics';
import { encodeKeylog } from './keylog';

export interface Engine {
  getState(): Readonly<EngineState>;
  dispatch(ev: KeyEvent): void;
  subscribe(listener: (s: EngineState, ev: KeyEvent) => void): () => void;
  metrics(): EngineMetrics;
  liveWpm(nowT: number): number | null;
  exportKeylog(): string;
}

/**
 * Creates an Engine instance for a typing session.
 */
export function createEngine(config: EngineConfig): Engine {
  let state = createInitialState(config);
  const listeners: Set<(s: EngineState, ev: KeyEvent) => void> = new Set();

  return {
    getState(): Readonly<EngineState> {
      return state;
    },

    dispatch(ev: KeyEvent): void {
      state = reduce(state, ev);
      listeners.forEach((listener) => listener(state, ev));
    },

    subscribe(listener: (s: EngineState, ev: KeyEvent) => void): () => void {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },

    metrics(): EngineMetrics {
      return calculateMetrics(state);
    },

    liveWpm(nowT: number): number | null {
      // Rolling 10s window (10000 ms)
      const windowStart = Math.max(0, nowT - 10000);
      const windowDuration = nowT - windowStart;

      // Filter correct typing events in the window
      let correctCharsInWindow = 0;
      for (const ev of state.log) {
        if (ev.t >= windowStart && ev.t <= nowT && (ev.k === 'char' || ev.k === 'enter' || ev.k === 'tab')) {
          correctCharsInWindow++;
        }
      }

      return calculateLiveWpm(correctCharsInWindow, windowDuration, state.activeMs);
    },

    exportKeylog(): string {
      return encodeKeylog(state.log, {
        strictIndent: config.strictIndent,
      });
    },
  };
}

export * from './types';
export * from './constants';
export * from './reducer';
export * from './prepare';
export * from './metrics';
export * from './keylog';
export * from './categories';
export * from './stats';
