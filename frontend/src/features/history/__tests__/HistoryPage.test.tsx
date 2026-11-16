import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { HistoryPage } from '../HistoryPage';
import { devtypeApi } from '../../../shared/api/devtypeApi';

vi.mock('../../../shared/api/devtypeApi', () => ({
  devtypeApi: {
    getSessions: vi.fn(),
    getSessionReplay: vi.fn(),
  },
}));

describe('HistoryPage Component (AC-AN-9, AC-CE-8)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders history table with sessions list and filter controls', async () => {
    vi.mocked(devtypeApi.getSessions).mockResolvedValueOnce({
      sessions: [
        {
          sessionId: 'sess-1',
          language: 'sql',
          mode: 'PRACTICE',
          topicTitle: 'SELECT Basics',
          exerciseTitle: 'Simple Select',
          wpm: 55,
          accuracy: 96.0,
          durationSeconds: 32,
          earnedXp: 30,
          status: 'COMPLETED',
          createdAt: '2026-11-16T12:00:00Z',
        },
      ],
      nextCursor: null,
      hasMore: false,
    });

    render(
      <MemoryRouter>
        <HistoryPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('SQL')).toBeInTheDocument();
    });
    expect(screen.getByText('55')).toBeInTheDocument();
    expect(screen.getByText('96%')).toBeInTheDocument();
    expect(screen.getByText('32s')).toBeInTheDocument();
    expect(screen.getByText('+30')).toBeInTheDocument();
    expect(screen.getByText(/Replay/i)).toBeInTheDocument();
  });

  it('filters by language change', async () => {
    vi.mocked(devtypeApi.getSessions).mockResolvedValue({
      sessions: [],
      nextCursor: null,
      hasMore: false,
    });

    render(
      <MemoryRouter>
        <HistoryPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Session History/i)).toBeInTheDocument();
    });

    const selects = screen.getAllByRole('combobox');
    const langSelect = selects[0];
    if (langSelect) {
      fireEvent.change(langSelect, { target: { value: 'sql' } });
      expect(devtypeApi.getSessions).toHaveBeenCalledWith(
        expect.objectContaining({ language: 'sql' })
      );
    }
  });
});
