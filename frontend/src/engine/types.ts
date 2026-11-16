import { Mode } from './constants';

export type CharState = 'pending' | 'correct' | 'incorrect' | 'corrected';

export interface TargetChar {
  ch: string;
  line: number;
  col: number;
  auto: boolean;
  wordId: number;
  patternId: number;
  exerciseSeq: number;
}

export type KeyEvent =
  | { t: number; k: 'char'; ch: string }
  | { t: number; k: 'enter' }
  | { t: number; k: 'tab' }
  | { t: number; k: 'backspace' }
  | { t: number; k: 'wordDelete' }
  | { t: number; k: 'pause' }
  | { t: number; k: 'resume' }
  | { t: number; k: 'next' }
  | { t: number; k: 'finishEarly' }
  | { t: number; k: 'timeout' };

export interface EngineState {
  phase: 'idle' | 'running' | 'paused' | 'finished';
  mode: Mode;
  errorMode: 'stop' | 'free';
  targets: TargetChar[][];
  seq: number;
  states: CharState[];
  typed: (string | null)[];
  everWrong: boolean[];
  cursor: number;
  consecutiveErrors: number;
  activeMs: number;
  lastEventAt: number;
  limitMs: number | null;
  correctKeys: number;
  incorrectKeys: number;
  backspaces: number;
  totalTyped: number;
  finishedCorrect: number;
  perSecond: number[];
  log: KeyEvent[];
}

export interface EngineConfig {
  snippets: string[];
  mode?: Mode;
  errorMode?: 'stop' | 'free';
  limitSeconds?: number | null;
  strictIndent?: boolean;
  indentStyle?: 'SPACE' | 'TAB';
  indentSize?: number;
  tokenPatterns?: string[];
}

export interface EngineMetrics {
  wpm: number;
  rawWpm: number;
  cpm: number;
  accuracy: number;
  consistency: number;
  elapsedMs: number;
  totalTyped: number;
  correctChars: number;
  uncorrectedErrors: number;
  correctKeys: number;
  incorrectKeys: number;
  backspaceCount: number;
  completionPct: number;
}
