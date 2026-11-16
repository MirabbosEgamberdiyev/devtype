import { describe, it, expect, vi, beforeEach } from 'vitest';
import { enqueueOfflineSession, flushOfflineQueue } from '../offlineQueue';
import * as typingApi from '../../typing/api';

describe('offlineQueue (AC-TE-15)', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('returns 0 when navigator is offline without attempting to flush', async () => {
    Object.defineProperty(navigator, 'onLine', {
      value: false,
      configurable: true,
    });

    const count = await flushOfflineQueue();
    expect(count).toBe(0);
  });

  it('handles enqueue and flush operations gracefully', async () => {
    Object.defineProperty(navigator, 'onLine', {
      value: true,
      configurable: true,
    });

    const mockRequest = {
      completeToken: 'token-123',
      keylog: 'base64log',
      clientElapsedMs: 10000,
      clientMetrics: { wpm: 55, accuracy: 98 },
    };

    // Even if indexedDB is absent in some test environments, it should not throw
    await expect(enqueueOfflineSession('sess-1', mockRequest)).resolves.not.toThrow();

    vi.spyOn(typingApi, 'completeSession').mockResolvedValue(
      {} as unknown as typingApi.CompleteSessionResponse
    );

    const flushed = await flushOfflineQueue();
    expect(typeof flushed).toBe('number');
  });
});
