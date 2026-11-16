import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AnalyticsPage } from '../AnalyticsPage';
import { devtypeApi } from '../../../shared/api/devtypeApi';

vi.mock('../../../shared/api/devtypeApi', () => ({
  devtypeApi: {
    getAnalyticsOverview: vi.fn(),
    getAnalyticsActivity: vi.fn(),
    getAnalyticsChars: vi.fn(),
  },
}));

describe('AnalyticsPage (AC-AN-1, AC-AN-6, AC-AN-7, AC-AN-8)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders empty state when hasEnoughData is false (AC-AN-8)', async () => {
    vi.mocked(devtypeApi.getAnalyticsOverview).mockResolvedValueOnce({
      hasEnoughData: false,
      totalSessions: 3,
      totalTimeMinutes: 5,
      avgWpm: 42,
      bestWpm: 45,
      avgAccuracy: 95.0,
      totalChars: 800,
      devScore: 0,
      weakestLanguage: null,
      wpmTrend: [],
    });
    vi.mocked(devtypeApi.getAnalyticsActivity).mockResolvedValueOnce({ days: [] });

    render(
      <MemoryRouter>
        <AnalyticsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Not Enough Training Data/i)).toBeInTheDocument();
    });
    expect(
      screen.getByText(/Complete at least 5 sessions to unlock comprehensive analytics/i)
    ).toBeInTheDocument();
  });

  it('renders KPI metrics, weakest language warning, and activity calendar when data is sufficient (AC-AN-1, AC-AN-6, AC-AN-7)', async () => {
    vi.mocked(devtypeApi.getAnalyticsOverview).mockResolvedValueOnce({
      hasEnoughData: true,
      totalSessions: 30,
      totalTimeMinutes: 75,
      avgWpm: 58,
      bestWpm: 72,
      avgAccuracy: 97.4,
      totalChars: 15400,
      devScore: 164,
      weakestLanguage: {
        language: 'python',
        ratio: 0.68,
        medianWpm: 60,
      },
      wpmTrend: [{ date: '2026-11-16', wpm: 58, accuracy: 97.4 }],
    });
    vi.mocked(devtypeApi.getAnalyticsActivity).mockResolvedValueOnce({
      days: [
        {
          date: '2026-11-16',
          sessionsCount: 5,
          minutesTrained: 25,
          level: 2,
        },
      ],
    });

    render(
      <MemoryRouter>
        <AnalyticsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Analytics & Insights/i)).toBeInTheDocument();
    });

    // KPI verification
    expect(screen.getByText('58')).toBeInTheDocument();
    expect(screen.getByText('72')).toBeInTheDocument();
    expect(screen.getByText('97.4%')).toBeInTheDocument();
    expect(screen.getByText('164')).toBeInTheDocument();

    // Weakest language banner (AC-AN-6)
    expect(screen.getByText(/Focus Opportunity Identified/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Practice PYTHON/i })).toBeInTheDocument();

    // Activity Calendar (AC-AN-7)
    expect(screen.getByText(/Activity Calendar/i)).toBeInTheDocument();
  });
});
