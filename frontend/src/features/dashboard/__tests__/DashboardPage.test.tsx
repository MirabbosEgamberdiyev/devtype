import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { DashboardPage } from '../DashboardPage';
import { devtypeApi } from '../../../shared/api/devtypeApi';

vi.mock('../../../shared/api/devtypeApi', () => ({
  devtypeApi: {
    getDashboard: vi.fn(),
  },
}));

describe('DashboardPage (AC-UD-1, AC-UD-3, AC-UD-4, AC-UD-6, BL-UD-1)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders welcome banner when isNewUser is true (AC-UD-1)', async () => {
    vi.mocked(devtypeApi.getDashboard).mockResolvedValueOnce({
      isNewUser: true,
      resume: {
        type: 'FIRST_EXERCISE',
        language: 'sql',
        topicId: 't-1',
        level: 'BEGINNER',
        exerciseId: 'ex-1',
        exerciseTitle: 'First SQL Query',
        message: 'Start your first exercise',
      },
      gamification: {
        totalXp: 0,
        currentLevel: 1,
        xpIntoLevel: 0,
        xpToNext: 100,
        streakDays: 0,
        streakAtRisk: false,
        streakFrozen: false,
        streakFreezesRemaining: 2,
      },
      languages: [],
      recentSessions: [],
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Welcome to DevType/i)).toBeInTheDocument();
    });
    expect(screen.getByText(/First SQL Query/i)).toBeInTheDocument();
  });

  it('renders streak at risk banner when streakAtRisk is true (AC-UD-3)', async () => {
    vi.mocked(devtypeApi.getDashboard).mockResolvedValueOnce({
      isNewUser: false,
      resume: null,
      gamification: {
        totalXp: 1500,
        currentLevel: 5,
        xpIntoLevel: 250,
        xpToNext: 600,
        streakDays: 14,
        streakAtRisk: true,
        streakFrozen: false,
        streakFreezesRemaining: 1,
      },
      languages: [
        {
          language: 'sql',
          languageName: 'SQL',
          rank: 'BRONZE',
          bestWpm: 48,
          currentWpm: 45,
          accuracy: 97.5,
          sessionsCount: 12,
          sparkline: [{ date: '2026-11-15', wpm: 45 }],
        },
      ],
      recentSessions: [],
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Streak at Risk/i)).toBeInTheDocument();
    });
    expect(screen.getByText('14 days')).toBeInTheDocument();
    expect(screen.getByText('SQL')).toBeInTheDocument();
  });

  it('renders recent sessions table with last completed sessions (AC-UD-6)', async () => {
    vi.mocked(devtypeApi.getDashboard).mockResolvedValueOnce({
      isNewUser: false,
      resume: null,
      gamification: {
        totalXp: 2400,
        currentLevel: 8,
        xpIntoLevel: 120,
        xpToNext: 800,
        streakDays: 5,
        streakAtRisk: false,
        streakFrozen: false,
        streakFreezesRemaining: 2,
      },
      languages: [],
      recentSessions: [
        {
          sessionId: 's-123',
          language: 'python',
          mode: 'PRACTICE',
          topicTitle: 'Functions',
          wpm: 65,
          accuracy: 98.2,
          earnedXp: 45,
          completedAt: '2026-11-16T10:00:00Z',
        },
      ],
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('PYTHON')).toBeInTheDocument();
    });
    expect(screen.getByText('65')).toBeInTheDocument();
    expect(screen.getByText('98.2%')).toBeInTheDocument();
    expect(screen.getByText('+45')).toBeInTheDocument();
  });
});
