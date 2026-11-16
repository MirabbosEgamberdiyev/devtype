import { apiClient } from '../../shared/api/client';

export interface StartSessionRequest {
  language?: string;
  mode?: 'PRACTICE' | 'SPEED_TEST' | 'ACCURACY' | 'CHALLENGE' | 'SPECIAL_CHARS' | 'WEAKNESS' | 'CODE_SPRINT';
  topic?: string;
  difficulty?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  exerciseKey?: string;
  duration?: 30 | 60 | 120;
  accuracyMaster?: boolean;
  specialCategory?: string;
  challengeId?: number;
  competitionId?: number;
  placement?: boolean;
  inputDevice?: 'PHYSICAL' | 'TOUCH';
  strictIndent?: boolean;
}

export interface SessionExercise {
  seq: number;
  exerciseKey: string;
  title: string;
  note: string;
  difficulty: string;
  code: string;
  charCount: number;
}

export interface StartSessionResponse {
  sessionId: string;
  startedAt: string;
  expiresAt: string;
  mode: string;
  errorMode: string;
  durationLimitS: number | null;
  language: {
    slug: string;
    grammarId: string;
    indent: { style: string; size: number };
    tokenPatterns: string[];
  };
  exercises: SessionExercise[];
  completeToken: string;
}

export interface ClientMetrics {
  wpm: number;
  accuracy: number;
}

export interface CompleteSessionRequest {
  completeToken: string;
  keylog: string;
  clientElapsedMs: number;
  clientMetrics: ClientMetrics;
}

export interface ServerMetrics {
  wpm: number;
  rawWpm: number;
  cpm: number;
  accuracy: number;
  consistency: number;
  errors: number;
  uncorrectedErrors: number;
  backspaces: number;
  correctChars: number;
  elapsedMs: number;
  completionPct: number;
  specialAccuracy: number;
}

export interface WpmPoint {
  t: number;
  wpm: number;
  raw: number;
  errors: number;
}

export interface WeakToken {
  token: string;
  errors: number;
  expected: number;
  avgMs: number;
}

export interface SlowLine {
  line: number;
  ms: number;
}

export interface RewardsInfo {
  xp: number;
  totalXp: number;
  level: number;
  levelUp?: number | null;
  streak: { current: number; extendedToday: boolean };
}

export interface ProgressInfo {
  topic: string;
  difficulty: string;
  state: string;
  completed: number;
  required: number;
  exerciseCompleted: boolean;
  levelUnlocked?: string | null;
}

export interface CompleteSessionResponse {
  sessionId: string;
  status: string;
  ranked: boolean;
  rankedReason: string;
  metrics: ServerMetrics;
  wpmSeries: WpmPoint[];
  weakTokens: WeakToken[];
  slowLines: SlowLine[];
  rewards?: RewardsInfo | null;
  progress?: ProgressInfo | null;
  coachTip?: { code: string; text: string } | null;
  next?: { exerciseKey: string; title: string } | null;
}

export interface NextExerciseResponse {
  exercises: SessionExercise[];
}

export async function startSession(req: StartSessionRequest): Promise<StartSessionResponse> {
  return apiClient<StartSessionResponse>('/sessions', {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function completeSession(
  sessionId: string,
  req: CompleteSessionRequest
): Promise<CompleteSessionResponse> {
  return apiClient<CompleteSessionResponse>(`/sessions/${sessionId}/complete`, {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function nextExercise(
  sessionId: string,
  afterSeq: number
): Promise<NextExerciseResponse> {
  return apiClient<NextExerciseResponse>(`/sessions/${sessionId}/next`, {
    method: 'POST',
    body: JSON.stringify({ afterSeq }),
  });
}

export async function abandonSession(sessionId: string): Promise<void> {
  await apiClient<void>(`/sessions/${sessionId}/abandon`, {
    method: 'POST',
  });
}

export async function getSession(sessionId: string): Promise<CompleteSessionResponse> {
  return apiClient<CompleteSessionResponse>(`/sessions/${sessionId}`);
}

export async function claimSessions(guestToken: string): Promise<{ claimed: number }> {
  return apiClient<{ claimed: number }>('/sessions/claim', {
    method: 'POST',
    body: JSON.stringify({ guestToken }),
  });
}
