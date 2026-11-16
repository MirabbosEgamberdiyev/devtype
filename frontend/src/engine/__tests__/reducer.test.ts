import { describe, it, expect } from 'vitest';
import { createInitialState, reduce } from '../reducer';

describe('reducer — docs/07 §2, §3 & AC-TE-1, 4, 5, 13', () => {
  it('transitions from idle to running on first typing keystroke', () => {
    const state = createInitialState({ snippets: ['hello'] });
    expect(state.phase).toBe('idle');

    // Non-typing events should be ignored while idle
    const withPause = reduce(state, { t: 0, k: 'pause' });
    expect(withPause.phase).toBe('idle');

    // First char transitions to running
    const running = reduce(state, { t: 0, k: 'char', ch: 'h' });
    expect(running.phase).toBe('running');
    expect(running.cursor).toBe(1);
    expect(running.correctKeys).toBe(1);
    expect(running.states[0]).toBe('correct');
  });

  it('handles stop-on-error mode: cursor does not advance on incorrect char', () => {
    const state = createInitialState({ snippets: ['hello'], errorMode: 'stop' });
    const started = reduce(state, { t: 0, k: 'char', ch: 'h' });

    // Type incorrect char 'x' instead of 'e'
    const wrong = reduce(started, { t: 100, k: 'char', ch: 'x' });
    expect(wrong.cursor).toBe(1); // still at 1
    expect(wrong.states[1]).toBe('pending');
    expect(wrong.incorrectKeys).toBe(1);
    expect(wrong.everWrong[1]).toBe(true);

    // Type correct char 'e'
    const correct = reduce(wrong, { t: 200, k: 'char', ch: 'e' });
    expect(correct.cursor).toBe(2);
    expect(correct.states[1]).toBe('corrected'); // corrected because it was everWrong
    expect(correct.correctKeys).toBe(2);
  });

  it('handles free mode: cursor advances on incorrect char, records typed char', () => {
    const state = createInitialState({ snippets: ['hello'], errorMode: 'free' });
    const started = reduce(state, { t: 0, k: 'char', ch: 'h' });

    const wrong = reduce(started, { t: 100, k: 'char', ch: 'x' });
    expect(wrong.cursor).toBe(2); // advanced to 2
    expect(wrong.states[1]).toBe('incorrect');
    expect(wrong.typed[1]).toBe('x');
    expect(wrong.consecutiveErrors).toBe(1);
  });

  it('AC-TE-13: free mode blocks input after 8 consecutive errors, unblocks after Backspace', () => {
    // Target has 10 characters
    const state = createInitialState({ snippets: ['0123456789'], errorMode: 'free' });
    let s = state;

    // Type 8 consecutive incorrect characters
    for (let i = 0; i < 8; i++) {
      s = reduce(s, { t: i * 100, k: 'char', ch: 'x' });
    }
    expect(s.cursor).toBe(8);
    expect(s.consecutiveErrors).toBe(8);
    expect(s.incorrectKeys).toBe(8);

    // 9th keystroke should be blocked (ignored from counters & cursor)
    const blocked = reduce(s, { t: 800, k: 'char', ch: 'x' });
    expect(blocked.cursor).toBe(8);
    expect(blocked.incorrectKeys).toBe(8); // did not increment
    expect(blocked.totalTyped).toBe(8);

    // Press backspace to unblock
    const afterBs = reduce(blocked, { t: 900, k: 'backspace' });
    expect(afterBs.cursor).toBe(7);
    expect(afterBs.consecutiveErrors).toBe(7);
    expect(afterBs.states[7]).toBe('pending');

    // Now input is unblocked and typing resumes
    const unblocked = reduce(afterBs, { t: 1000, k: 'char', ch: '7' });
    expect(unblocked.cursor).toBe(8);
    expect(unblocked.states[7]).toBe('corrected');
    expect(unblocked.consecutiveErrors).toBe(0);
  });

  it('AC-TE-5: automatically skips leading indentation after enter when strictIndent=false', () => {
    const code = 'fn() {\n  return 1;\n}';
    const state = createInitialState({
      snippets: [code],
      strictIndent: false,
    });

    let s = state;
    const line1 = 'fn() {\n';
    for (let i = 0; i < line1.length; i++) {
      const ch = line1[i]!;
      if (ch === '\n') {
        s = reduce(s, { t: i * 50, k: 'enter' });
      } else {
        s = reduce(s, { t: i * 50, k: 'char', ch });
      }
    }

    // After Enter, cursor should have automatically skipped the 2 leading spaces of '  return'
    // Line 1 has 'fn() {\n' (7 chars). Positions 7 and 8 are the two leading spaces with auto=true.
    // Cursor must jump directly to position 9 ('r').
    expect(s.cursor).toBe(9);
    expect(s.targets[0]?.[s.cursor]?.ch).toBe('r');
  });

  it('AC-TE-4: pauses in stop-on-error mode and preserves active time', () => {
    const state = createInitialState({ snippets: ['abc'], errorMode: 'stop' });
    const s1 = reduce(state, { t: 0, k: 'char', ch: 'a' });
    expect(s1.activeMs).toBe(0);

    const s2 = reduce(s1, { t: 1000, k: 'pause' });
    expect(s2.phase).toBe('paused');
    expect(s2.activeMs).toBe(1000);

    // Resuming 20 seconds later (t = 21000)
    const s3 = reduce(s2, { t: 21000, k: 'resume' });
    expect(s3.phase).toBe('running');
    expect(s3.activeMs).toBe(1000); // 20s pause was not added!

    const s4 = reduce(s3, { t: 21500, k: 'char', ch: 'b' });
    expect(s4.activeMs).toBe(1500); // Only 500ms since resume added
  });

  it('finishes session when last character of last snippet is correctly typed', () => {
    const state = createInitialState({ snippets: ['hi'] });
    let s = state;
    s = reduce(s, { t: 0, k: 'char', ch: 'h' });
    expect(s.phase).toBe('running');
    s = reduce(s, { t: 100, k: 'char', ch: 'i' });
    expect(s.phase).toBe('finished');
  });

  it('wordDelete deletes word back to boundary and does not cross newline', () => {
    const code = 'foo\nbar_baz';
    const state = createInitialState({ snippets: [code], errorMode: 'free' });
    let s = state;

    // Type 'foo\nbar_'
    const text = 'foo\nbar_';
    for (let i = 0; i < text.length; i++) {
      const ch = text[i]!;
      if (ch === '\n') {
        s = reduce(s, { t: i * 50, k: 'enter' });
      } else {
        s = reduce(s, { t: i * 50, k: 'char', ch });
      }
    }

    expect(s.cursor).toBe(text.length);
    // Execute wordDelete
    const afterWd = reduce(s, { t: 500, k: 'wordDelete' });
    // 'bar_' is one word token [A-Za-z0-9_]. It should be cleared back to '\n'
    expect(afterWd.cursor).toBe(4); // Start of line 2
  });
});
