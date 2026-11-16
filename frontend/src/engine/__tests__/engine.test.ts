import { describe, it, expect, vi } from 'vitest';
import { createEngine } from '../index';
import { decodeKeylog } from '../keylog';

describe('createEngine API — docs/07 §12', () => {
  it('manages full lifecycle: dispatch, subscribe, metrics, liveWpm, exportKeylog', () => {
    const engine = createEngine({
      snippets: ['let x = 1;'],
      errorMode: 'stop',
    });

    const listener = vi.fn();
    const unsubscribe = engine.subscribe(listener);

    expect(engine.getState().phase).toBe('idle');

    // Dispatch first event 'l' at t=0
    engine.dispatch({ t: 0, k: 'char', ch: 'l' });
    expect(listener).toHaveBeenCalledTimes(1);
    expect(engine.getState().phase).toBe('running');
    expect(engine.getState().cursor).toBe(1);

    // Type rest of 'let x = 1;'
    const remaining = 'et x = 1;';
    for (let i = 0; i < remaining.length; i++) {
      const ch = remaining[i]!;
      engine.dispatch({ t: (i + 1) * 100, k: 'char', ch });
    }

    expect(engine.getState().phase).toBe('finished');
    const m = engine.metrics();
    expect(m.accuracy).toBe(100.0);
    expect(m.uncorrectedErrors).toBe(0);
    expect(m.correctChars).toBe(10);

    // Keylog export should be valid Base64(Gzip(Binary))
    const keylogBase64 = engine.exportKeylog();
    expect(typeof keylogBase64).toBe('string');
    const decoded = decodeKeylog(keylogBase64);
    expect(decoded.header.eventCount).toBe(10);
    expect(decoded.events.length).toBe(10);

    // Unsubscribe works
    unsubscribe();
    engine.dispatch({ t: 2000, k: 'char', ch: 'a' });
    expect(listener).toHaveBeenCalledTimes(10); // Not called after unsubscribe
  });
});
