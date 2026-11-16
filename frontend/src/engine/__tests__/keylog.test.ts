import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import {
  encodeVarint,
  decodeVarint,
  encodeKeylog,
  decodeKeylog,
  MAX_ALLOWED_DT_MS,
} from '../keylog';
import { KeyEvent } from '../types';

describe('keylog codec — docs/07 §7', () => {
  describe('LEB128 varint encoding/decoding', () => {
    it('encodes and decodes known values correctly', () => {
      const testValues = [0, 1, 63, 127, 128, 255, 300, 16384, 1000000];
      for (const val of testValues) {
        const encoded = encodeVarint(val);
        const offset = { pos: 0 };
        const decoded = decodeVarint(new Uint8Array(encoded), offset);
        expect(decoded).toBe(val);
        expect(offset.pos).toBe(encoded.length);
      }
    });

    it('roundtrips arbitrary uint32 with fast-check', () => {
      fc.assert(
        fc.property(fc.integer({ min: 0, max: 0x7fffffff }), (val) => {
          const encoded = encodeVarint(val);
          const offset = { pos: 0 };
          const decoded = decodeVarint(new Uint8Array(encoded), offset);
          return decoded === val && offset.pos === encoded.length;
        })
      );
    });
  });

  describe('encodeKeylog and decodeKeylog roundtrip', () => {
    it('encodes and decodes a typical sequence of events', () => {
      const events: KeyEvent[] = [
        { t: 0, k: 'char', ch: 'H' },
        { t: 120, k: 'char', ch: 'i' },
        { t: 250, k: 'enter' },
        { t: 300, k: 'tab' },
        { t: 400, k: 'backspace' },
        { t: 450, k: 'wordDelete' },
        { t: 500, k: 'pause' },
        { t: 1000, k: 'resume' },
        { t: 1200, k: 'next' },
        { t: 1500, k: 'timeout' },
      ];

      const b64 = encodeKeylog(events, { strictIndent: true, isTouch: false });
      expect(typeof b64).toBe('string');
      expect(b64.length).toBeGreaterThan(0);

      const decoded = decodeKeylog(b64);
      expect(decoded.header.strictIndent).toBe(true);
      expect(decoded.header.isTouch).toBe(false);
      expect(decoded.header.eventCount).toBe(events.length);
      expect(decoded.events).toEqual(events);
    });

    it('rejects dt exceeding maximum 3600000 ms', () => {
      const events: KeyEvent[] = [
        { t: 0, k: 'char', ch: 'a' },
        { t: MAX_ALLOWED_DT_MS + 10, k: 'char', ch: 'b' },
      ];
      const b64 = encodeKeylog(events);
      expect(() => decodeKeylog(b64)).toThrow('KEYLOG_INVALID');
    });

    it('rejects empty or corrupt keylogs', () => {
      expect(() => decodeKeylog('')).toThrow('KEYLOG_INVALID');
      expect(() => decodeKeylog('not-valid-base64-gzip')).toThrow();
    });
  });
});
