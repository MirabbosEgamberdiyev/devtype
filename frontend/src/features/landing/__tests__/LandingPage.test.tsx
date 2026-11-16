import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { LandingPage } from '../LandingPage';
import { authApi } from '../../auth/api';
import '../../../shared/i18n';

// Mock TypingView for landing page unit test
vi.mock('../../typing/TypingView', () => ({
  TypingView: (props: { language?: string; duration?: number }) => (
    <div data-testid="typing-view-mock">
      <span>Demo Language: {props.language}</span>
      <span>Demo Duration: {props.duration}s</span>
    </div>
  ),
}));

describe('LandingPage (docs/04 §1.1, docs/05 §5.1, AC-UM-7)', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders hero title, description, and action links', () => {
    vi.spyOn(authApi, 'getGuestToken').mockResolvedValue({
      guestToken: 'mock-guest-token',
      expiresAt: '2026-12-31T00:00:00Z',
    });

    render(
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>
    );

    expect(screen.getAllByText(/DevType/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/Quick 30s Speed Test|30 soniyalik tezkor test|Быстрый 30-секундный тест/i)).toBeInTheDocument();
    expect(screen.getByText('Java')).toBeInTheDocument();
    expect(screen.getByText('Python')).toBeInTheDocument();
  });

  it('renders feature highlight cards', () => {
    vi.spyOn(authApi, 'getGuestToken').mockResolvedValue({
      guestToken: 'mock-guest-token',
      expiresAt: '2026-12-31T00:00:00Z',
    });

    render(
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>
    );

    expect(screen.getByText(/Real Codebases|Haqiqiy kodlar bazasi|Реальный код/i)).toBeInTheDocument();
    expect(screen.getByText(/Special Characters|Maxsus belgilar|Спецсимволы/i)).toBeInTheDocument();
    expect(screen.getByText(/Granular Analytics|Chuqur tahlil|Глубокая аналитика/i)).toBeInTheDocument();
  });

  it('mounts interactive 30s SPEED_TEST demo', () => {
    vi.spyOn(authApi, 'getGuestToken').mockResolvedValue({
      guestToken: 'mock-guest-token',
      expiresAt: '2026-12-31T00:00:00Z',
    });

    render(
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>
    );

    expect(screen.getByTestId('typing-view-mock')).toBeInTheDocument();
    expect(screen.getByText('Demo Language: java')).toBeInTheDocument();
    expect(screen.getByText('Demo Duration: 30s')).toBeInTheDocument();
  });
});
