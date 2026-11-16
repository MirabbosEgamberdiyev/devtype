import { apiClient } from './client';

export interface DashboardResponse {
  isNewUser: boolean;
  resume: {
    type: 'FIRST_EXERCISE' | 'NEXT_EXERCISE' | 'NEXT_LEVEL' | 'SPEED_TEST';
    language: string;
    topicId: string | null;
    level: string | null;
    exerciseId: string | null;
    exerciseTitle: string | null;
    message: string;
  } | null;
  gamification: {
    totalXp: number;
    currentLevel: number;
    xpIntoLevel: number;
    xpToNext: number;
    streakDays: number;
    streakAtRisk: boolean;
    streakFrozen: boolean;
    streakFreezesRemaining: number;
  };
  languages: Array<{
    language: string;
    languageName: string;
    rank: string;
    bestWpm: number;
    currentWpm: number;
    accuracy: number;
    sessionsCount: number;
    sparkline: Array<{ date: string; wpm: number }>;
  }>;
  recentSessions: Array<{
    sessionId: string;
    language: string;
    mode: string;
    topicTitle: string | null;
    wpm: number;
    accuracy: number;
    earnedXp: number;
    completedAt: string;
  }>;
}

export interface SessionHistoryItem {
  sessionId: string;
  language: string;
  mode: string;
  topicTitle: string | null;
  exerciseTitle: string | null;
  wpm: number;
  accuracy: number;
  durationSeconds: number;
  earnedXp: number;
  status: string;
  createdAt: string;
}

export interface SessionHistoryResponse {
  sessions: SessionHistoryItem[];
  nextCursor: string | null;
  hasMore: boolean;
}

export interface ReplayResponse {
  sessionId: string;
  language: string;
  code: string;
  keylog: string; // Base64 encoded or string
  metrics: {
    wpm: number;
    cpm: number;
    accuracy: number;
    durationMs: number;
    errorCount: number;
  };
}

export interface AnalyticsOverview {
  hasEnoughData: boolean;
  totalSessions: number;
  totalTimeMinutes: number;
  avgWpm: number;
  bestWpm: number;
  avgAccuracy: number;
  totalChars: number;
  devScore: number;
  weakestLanguage: {
    language: string;
    ratio: number;
    medianWpm: number;
  } | null;
  wpmTrend: Array<{ date: string; wpm: number; accuracy: number }>;
}

export interface ActivityDay {
  date: string;
  sessionsCount: number;
  minutesTrained: number;
  level: number; // 0..4
}

export interface CharErrorStat {
  character: string;
  totalCount: number;
  errorCount: number;
  errorRate: number;
  zScore: number;
}

export interface UserSettings {
  theme: string;
  locale: string;
  strictIndent: boolean;
  practiceStopOnError: boolean;
  showLiveStats: boolean;
  keySound: string;
  soundVolume: number;
  blindMode: boolean;
  fontFamily: string;
  fontSize: number;
  lineHeight: number;
  caretStyle: string;
  smoothCaret: boolean;
  ligatures: boolean;
  lineNumbers: boolean;
  showInvisibles: boolean;
  bracketMatching: boolean;
  layout: string;
  showVirtualKeyboard: string;
  fingerGuide: boolean;
  profilePublic: boolean;
  showOnLeaderboard: boolean;
  coachEnabled: boolean;
  emailStreakReminder: boolean;
  emailWeeklyReport: boolean;
  emailAchievements: boolean;
  emailCompetitions: boolean;
}

export const devtypeApi = {
  // Dashboard
  getDashboard: () => apiClient<DashboardResponse>('/me/dashboard'),

  // History & Replay
  getSessions: (params: {
    language?: string;
    mode?: string;
    status?: string;
    cursor?: string;
    limit?: number;
  }) => {
    const search = new URLSearchParams();
    if (params.language) search.set('language', params.language);
    if (params.mode) search.set('mode', params.mode);
    if (params.status) search.set('status', params.status);
    if (params.cursor) search.set('cursor', params.cursor);
    if (params.limit) search.set('limit', String(params.limit));
    const qs = search.toString();
    return apiClient<SessionHistoryResponse>(`/sessions${qs ? `?${qs}` : ''}`);
  },

  getSessionReplay: (sessionId: string) =>
    apiClient<ReplayResponse>(`/sessions/${sessionId}/replay`),

  // Analytics
  getAnalyticsOverview: (language?: string) => {
    const search = language ? `?language=${encodeURIComponent(language)}` : '';
    return apiClient<AnalyticsOverview>(`/analytics/overview${search}`);
  },

  getAnalyticsActivity: () =>
    apiClient<{ days: ActivityDay[] }>('/analytics/activity'),

  getAnalyticsChars: (language: string) =>
    apiClient<{ chars: CharErrorStat[] }>(`/analytics/languages/${encodeURIComponent(language)}/chars`),

  // Settings & Profile
  getUserSettings: () => apiClient<UserSettings>('/me/settings'),
  updateUserSettings: (settings: Partial<UserSettings>) =>
    apiClient<UserSettings>('/me/settings', {
      method: 'PUT',
      body: JSON.stringify(settings),
    }),

  getMe: () =>
    apiClient<{
      id: number;
      username: string;
      email: string;
      role: string;
      country: string | null;
      timezone: string;
      createdAt: string;
    }>('/me'),

  updateProfile: (profile: { username?: string; country?: string; timezone?: string }) =>
    apiClient<{ id: number; username: string }>('/me', {
      method: 'PATCH',
      body: JSON.stringify(profile),
    }),

  exportUserData: () => apiClient<Record<string, unknown>>('/me/export'),

  deleteAccount: () =>
    apiClient<{ message: string; scheduledDeletionDate: string }>('/me', {
      method: 'DELETE',
    }),
};
