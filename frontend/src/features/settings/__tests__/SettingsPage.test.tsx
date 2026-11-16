import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { SettingsPage } from '../SettingsPage';
import { devtypeApi, type UserSettings } from '../../../shared/api/devtypeApi';

vi.mock('../../../shared/api/devtypeApi', () => ({
  devtypeApi: {
    getUserSettings: vi.fn(),
    updateUserSettings: vi.fn(),
    getMe: vi.fn(),
    updateProfile: vi.fn(),
    exportUserData: vi.fn(),
    deleteAccount: vi.fn(),
  },
}));

describe('SettingsPage (AC-UM-8, AC-UM-9, AC-UM-11)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders settings navigation and switches across all 7 sections', async () => {
    vi.mocked(devtypeApi.getUserSettings).mockResolvedValueOnce({
      theme: 'dark',
      locale: 'en',
      strictIndent: false,
      practiceStopOnError: true,
      showLiveStats: false,
      keySound: 'off',
      soundVolume: 50,
      blindMode: false,
      fontFamily: 'JetBrains Mono',
      fontSize: 18,
      lineHeight: 1.6,
      caretStyle: 'line',
      smoothCaret: true,
      ligatures: false,
      lineNumbers: true,
      showInvisibles: true,
      bracketMatching: true,
      layout: 'ansi-us',
      showVirtualKeyboard: 'auto',
      fingerGuide: true,
      profilePublic: true,
      showOnLeaderboard: true,
      coachEnabled: true,
      emailStreakReminder: false,
      emailWeeklyReport: false,
      emailAchievements: false,
      emailCompetitions: false,
    });
    vi.mocked(devtypeApi.getMe).mockResolvedValueOnce({
      id: 1,
      username: 'dev_user',
      email: 'user@example.com',
      role: 'USER',
      country: 'UZ',
      timezone: 'UTC',
      createdAt: '2026-11-01T00:00:00Z',
    });

    render(
      <MemoryRouter>
        <SettingsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Appearance' })).toBeInTheDocument();
    });

    // 1. Typing rules tab
    fireEvent.click(screen.getByText('Typing Rules'));
    expect(screen.getByText('Stop on Error')).toBeInTheDocument();

    // 2. Code editor tab
    fireEvent.click(screen.getByText('Code Editor'));
    expect(screen.getByText('Font Family')).toBeInTheDocument();
    expect(screen.getByText('Live Preview')).toBeInTheDocument();

    // 3. Keyboard tab
    fireEvent.click(screen.getByText('Keyboard & Layout'));
    expect(screen.getByText('Finger Color Guidance')).toBeInTheDocument();

    // 4. Account tab (AC-UM-8)
    fireEvent.click(screen.getByText('Account'));
    expect(screen.getByDisplayValue('dev_user')).toBeInTheDocument();
    expect(screen.getByText(/30 days/i)).toBeInTheDocument();

    // 5. Privacy & Export tab (AC-UM-11)
    fireEvent.click(screen.getByText('Privacy & Export'));
    expect(screen.getByText(/Export Personal Data/i)).toBeInTheDocument();
    expect(screen.getByText(/Download JSON/i)).toBeInTheDocument();

    // 6. Danger Zone tab (AC-UM-11)
    fireEvent.click(screen.getByText('Danger Zone'));
    expect(screen.getByRole('button', { name: /Delete Account/i })).toBeInTheDocument();
  });

  it('triggers delete account confirmation dialog', async () => {
    vi.mocked(devtypeApi.getUserSettings).mockResolvedValueOnce({} as unknown as UserSettings);
    vi.mocked(devtypeApi.getMe).mockResolvedValueOnce({
      id: 1,
      username: 'dev_user',
      email: 'user@example.com',
      role: 'USER',
      country: null,
      timezone: 'UTC',
      createdAt: '2026-11-01T00:00:00Z',
    });

    render(
      <MemoryRouter>
        <SettingsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Danger Zone')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Danger Zone'));
    const deleteBtn = screen.getByRole('button', { name: /Delete Account/i });
    fireEvent.click(deleteBtn);

    expect(screen.getByText(/Confirm Account Deletion/i)).toBeInTheDocument();
    expect(screen.getByText(/Yes, Delete My Account/i)).toBeInTheDocument();
  });
});
