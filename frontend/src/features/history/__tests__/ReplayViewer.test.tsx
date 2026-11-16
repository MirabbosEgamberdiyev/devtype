import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ReplayViewer } from '../ReplayViewer';
import { encodeKeylog } from '../../../engine/keylog';
import { KeyEvent } from '../../../engine/types';

describe('ReplayViewer Component (AC-CE-8)', () => {
  const code = 'const x = 1;';
  const events: KeyEvent[] = [
    { t: 0, k: 'char', ch: 'c' },
    { t: 100, k: 'char', ch: 'o' },
    { t: 200, k: 'char', ch: 'n' },
    { t: 300, k: 'char', ch: 's' },
    { t: 400, k: 'char', ch: 't' },
    { t: 500, k: 'char', ch: ' ' },
    { t: 600, k: 'char', ch: 'x' },
    { t: 700, k: 'char', ch: ' ' },
    { t: 800, k: 'char', ch: '=' },
    { t: 900, k: 'char', ch: ' ' },
    { t: 1000, k: 'char', ch: '1' },
    { t: 1100, k: 'char', ch: ';' },
  ];
  const keylogBase64 = encodeKeylog(events);

  it('renders replay controls, speed buttons, and keystrokes count (AC-CE-8)', () => {
    const onClose = vi.fn();
    render(
      <ReplayViewer
        code={code}
        keylogBase64={keylogBase64}
        language="javascript"
        finalMetrics={{
          wpm: 60,
          accuracy: 100,
          durationMs: 1100,
          errorCount: 0,
        }}
        onClose={onClose}
      />
    );

    expect(screen.getByText(/JAVASCRIPT REPLAY/i)).toBeInTheDocument();
    expect(screen.getByText(/12 keystrokes/i)).toBeInTheDocument();
    expect(screen.getByText('1x')).toBeInTheDocument();
    expect(screen.getByText('2x')).toBeInTheDocument();
    expect(screen.getByText('4x')).toBeInTheDocument();
  });

  it('toggles playback on play/pause click and switches speed', () => {
    render(
      <ReplayViewer
        code={code}
        keylogBase64={keylogBase64}
        language="javascript"
      />
    );

    const playBtn = screen.getByLabelText(/Play replay/i);
    fireEvent.click(playBtn);
    expect(screen.getByLabelText(/Pause replay/i)).toBeInTheDocument();

    const speed2x = screen.getByText('2x');
    fireEvent.click(speed2x);
    expect(speed2x.className).toContain('bg-accent');
  });

  it('handles seek and skip buttons', () => {
    render(
      <ReplayViewer
        code={code}
        keylogBase64={keylogBase64}
        language="javascript"
      />
    );

    const seekFwd = screen.getByLabelText(/Seek forward 5 seconds/i);
    fireEvent.click(seekFwd);

    const seekBack = screen.getByLabelText(/Seek back 5 seconds/i);
    fireEvent.click(seekBack);
  });
});
