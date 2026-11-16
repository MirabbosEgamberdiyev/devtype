import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { TypingView } from '../TypingView';
import '../../../shared/i18n';

describe('TypingView Vertical Slice Integration', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('fetches session and renders TypingEditor with code', async () => {
    const mockSession = {
      sessionId: 'test-session-uuid',
      startedAt: '2026-11-16T10:00:00Z',
      expiresAt: '2026-11-17T10:00:00Z',
      mode: 'PRACTICE',
      errorMode: 'STOP',
      durationLimitS: null,
      language: {
        slug: 'java',
        grammarId: 'java',
        indent: { style: 'SPACE', size: 4 },
        tokenPatterns: ['->', '::'],
      },
      exercises: [
        {
          seq: 0,
          exerciseKey: 'ex-1',
          title: 'Hello World',
          note: 'Java intro',
          difficulty: 'BEGINNER',
          code: 'public class HelloWorld {\n}',
          charCount: 26,
        },
      ],
      completeToken: 'valid.token',
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify(mockSession), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    render(<TypingView />);

    await waitFor(() => {
      expect(screen.getByText('Hello World')).toBeInTheDocument();
      expect(screen.getByText('Java intro')).toBeInTheDocument();
      expect(screen.getByText(/java · PRACTICE/i)).toBeInTheDocument();
    });
  });
});
