import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, act, within } from '@testing-library/react';
import { TypingEditor } from '../TypingEditor';
import '../../../shared/i18n';

describe('TypingEditor Component (AC-TE-10, AC-TE-14, AC-VK-5)', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders code snippet, line gutter, and caret', () => {
    render(
      <TypingEditor
        code="int x = 42;"
        language="java"
        mode="PRACTICE"
        onComplete={vi.fn()}
      />
    );

    const editor = screen.getByTestId('typing-editor');
    expect(editor).toBeInTheDocument();
    expect(screen.getByTestId('typing-caret')).toBeInTheDocument();
    expect(screen.getByTestId('gutter-line')).toHaveTextContent('1');
    expect(within(editor).getByText('i')).toBeInTheDocument();
  });

  it('advances cursor on valid keystroke', () => {
    const handleComplete = vi.fn();
    const { container } = render(
      <TypingEditor
        code="hi"
        language="java"
        mode="PRACTICE"
        onComplete={handleComplete}
      />
    );

    const editor = screen.getByTestId('typing-editor');
    const input = container.querySelector('textarea')!;
    expect(input).toBeInTheDocument();

    // Type 'h'
    fireEvent.keyDown(input, { key: 'h', code: 'KeyH' });

    // Check that 'h' turned correct
    const hChar = within(editor).getByText('h');
    expect(hChar.className).toContain('text-text-primary');

    // Type 'i'
    fireEvent.keyDown(input, { key: 'i', code: 'KeyI' });

    expect(handleComplete).toHaveBeenCalledTimes(1);
  });

  it('marks error on incorrect keystroke in stopOnError mode', () => {
    const { container } = render(
      <TypingEditor
        code="hi"
        language="java"
        mode="PRACTICE"
        stopOnError={true}
        onComplete={vi.fn()}
      />
    );

    const editor = screen.getByTestId('typing-editor');
    const input = container.querySelector('textarea')!;

    // Type wrong key 'z' instead of 'h'
    fireEvent.keyDown(input, { key: 'z', code: 'KeyZ' });

    const hChar = within(editor).getByText('h');
    expect(hChar.className).toContain('text-red-500');
  });

  it('prevents paste and displays paste warning toast (AC-TE-14)', () => {
    const { container } = render(
      <TypingEditor
        code="secret code"
        language="java"
        onComplete={vi.fn()}
      />
    );

    const input = container.querySelector('textarea')!;
    fireEvent.paste(input, { clipboardData: { getData: () => 'pasted text' } });

    expect(screen.getByText(/pasting code is disabled|taqiqlangan|запрещена/i)).toBeInTheDocument();
  });

  it('displays Caps Lock indicator when CapsLock modifier is active', () => {
    const { container } = render(
      <TypingEditor
        code="test"
        language="java"
        onComplete={vi.fn()}
      />
    );

    const input = container.querySelector('textarea')!;
    fireEvent.keyDown(input, {
      key: 'a',
      code: 'KeyA',
      modifierCapsLock: true,
    });

    expect(screen.getByText('CAPS LOCK')).toBeInTheDocument();
  });

  it('pauses automatically after 15 seconds of inactivity (AC-TM-1)', () => {
    vi.useFakeTimers();

    const { container } = render(
      <TypingEditor
        code="test inactivity timeout"
        language="java"
        onComplete={vi.fn()}
      />
    );

    const input = container.querySelector('textarea')!;
    // Start typing to enter running phase
    fireEvent.keyDown(input, { key: 't', code: 'KeyT' });

    // Advance 16 seconds
    act(() => {
      vi.advanceTimersByTime(16000);
    });

    expect(screen.getByText(/paused|toʻxtatildi|приостановлена/i)).toBeInTheDocument();

    vi.useRealTimers();
  });
});
