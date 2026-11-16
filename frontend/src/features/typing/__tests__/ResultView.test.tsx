import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ResultView } from '../ResultView';
import { CompleteSessionResponse } from '../api';
import '../../../shared/i18n';

describe('ResultView Component (AC-TM-10, AC-TE-9)', () => {
  const mockResult: CompleteSessionResponse = {
    sessionId: 'test-session-123',
    status: 'COMPLETED',
    ranked: true,
    rankedReason: 'OFFICIAL',
    metrics: {
      wpm: 68.4,
      rawWpm: 72.1,
      cpm: 342,
      accuracy: 97.5,
      consistency: 89.2,
      elapsedMs: 25000,
      errors: 3,
      correctChars: 142,
      uncorrectedErrors: 0,
      backspaces: 5,
      completionPct: 100,
      specialAccuracy: 95.0,
    },
    wpmSeries: [
      { t: 0, wpm: 0, raw: 0, errors: 0 },
      { t: 5000, wpm: 60, raw: 65, errors: 1 },
      { t: 15000, wpm: 70, raw: 75, errors: 1 },
      { t: 25000, wpm: 68.4, raw: 72.1, errors: 1 },
    ],
    weakTokens: [
      { token: 'System.out.println', errors: 1, expected: 2, avgMs: 250 },
    ],
    slowLines: [
      { line: 2, ms: 4500 },
    ],
    rewards: {
      xp: 120,
      totalXp: 850,
      level: 2,
      streak: { current: 5, extendedToday: true },
    },
    progress: {
      topic: 'Java Basics',
      difficulty: 'BEGINNER',
      state: 'COMPLETED',
      completed: 8,
      required: 10,
      exerciseCompleted: true,
    },
    coachTip: {
      code: 'BRACKET_SPEED',
      text: 'Practice curly brackets without looking at the keyboard.',
    },
    next: {
      exerciseKey: 'ex-2',
      title: 'Next Java Exercise',
    },
  };

  it('renders all main KPIs with proper formatting', () => {
    render(<ResultView result={mockResult} onRetry={vi.fn()} />);

    expect(screen.getByTestId('result-wpm')).toHaveTextContent('68.4');
    expect(screen.getByTestId('result-accuracy')).toHaveTextContent('97.5%');
    expect(screen.getByText('89.2%')).toBeInTheDocument();
    expect(screen.getByText('25.0s')).toBeInTheDocument();
    expect(screen.getByText('CPM: 342')).toBeInTheDocument();
  });

  it('renders ranked session badge when ranked is true', () => {
    render(<ResultView result={mockResult} onRetry={vi.fn()} />);
    expect(screen.getByText(/ranked/i)).toBeInTheDocument();
  });

  it('renders SVG speed graph points', () => {
    const { container } = render(<ResultView result={mockResult} onRetry={vi.fn()} />);
    const polyline = container.querySelector('polyline');
    expect(polyline).toBeInTheDocument();
  });

  it('renders weak tokens, slow lines, and coach tip', () => {
    render(<ResultView result={mockResult} onRetry={vi.fn()} />);

    expect(screen.getByText('System.out.println')).toBeInTheDocument();
    expect(screen.getByText(/250 ms avg/i)).toBeInTheDocument();
    expect(screen.getByText('Practice curly brackets without looking at the keyboard.')).toBeInTheDocument();
  });

  it('renders gamification rewards and progress', () => {
    render(<ResultView result={mockResult} onRetry={vi.fn()} />);

    expect(screen.getByText('+120 XP')).toBeInTheDocument();
    expect(screen.getByText(/Total: 850 XP/i)).toBeInTheDocument();
    expect(screen.getByText(/Java Basics/i)).toBeInTheDocument();
  });

  it('triggers onRetry and onNext callback handlers', () => {
    const handleRetry = vi.fn();
    const handleNext = vi.fn();

    render(<ResultView result={mockResult} onRetry={handleRetry} onNext={handleNext} />);

    const retryBtn = screen.getByText(/try again|qayta urinish|попробовать снова/i);
    fireEvent.click(retryBtn);
    expect(handleRetry).toHaveBeenCalledTimes(1);

    const nextBtn = screen.getByText(/next exercise|keyingi mashq|следующее упражнение/i);
    fireEvent.click(nextBtn);
    expect(handleNext).toHaveBeenCalledTimes(1);
  });
});
