/**
 * Keylog Wire Format Codec (Version 1) — docs/07 §7.
 * Binary -> Gzip -> Base64 (standard with padding).
 */

import { gzipSync, gunzipSync } from 'fflate';
import { KeyEvent } from './types';

export const KEYLOG_MAGIC_0 = 0x44; // 'D'
export const KEYLOG_MAGIC_1 = 0x54; // 'T'
export const KEYLOG_VERSION_1 = 0x01;
export const CURRENT_ENGINE_VERSION = 10000; // 1.0.0
export const MAX_ALLOWED_DT_MS = 3600000; // 1 hour

export interface KeylogHeader {
  version: number;
  engineVer: number;
  strictIndent: boolean;
  isTouch: boolean;
  eventCount: number;
}

export interface DecodedKeylog {
  header: KeylogHeader;
  events: KeyEvent[];
}

export const EVENT_KIND_MAP: Record<KeyEvent['k'], number> = {
  char: 0,
  enter: 1,
  tab: 2,
  backspace: 3,
  wordDelete: 4,
  pause: 5,
  resume: 6,
  next: 7,
  finishEarly: 8,
  timeout: 9,
};

export const KIND_EVENT_MAP: Record<number, KeyEvent['k']> = {
  0: 'char',
  1: 'enter',
  2: 'tab',
  3: 'backspace',
  4: 'wordDelete',
  5: 'pause',
  6: 'resume',
  7: 'next',
  8: 'finishEarly',
  9: 'timeout',
};

/**
 * Encodes an unsigned integer into LEB128 varint byte array.
 */
export function encodeVarint(val: number): number[] {
  let value = val >>> 0;
  const bytes: number[] = [];
  while (value >= 0x80) {
    bytes.push((value & 0x7f) | 0x80);
    value >>>= 7;
  }
  bytes.push(value & 0x7f);
  return bytes;
}

/**
 * Decodes a LEB128 unsigned varint from a Uint8Array at a given offset.
 */
export function decodeVarint(bytes: Uint8Array, offset: { pos: number }): number {
  let result = 0;
  let shift = 0;
  while (offset.pos < bytes.length) {
    const b = bytes[offset.pos++];
    if (b === undefined) {
      throw new Error('KEYLOG_INVALID: Unexpected end of varint stream');
    }
    result |= (b & 0x7f) << shift;
    if ((b & 0x80) === 0) {
      return result >>> 0;
    }
    shift += 7;
  }
  throw new Error('KEYLOG_INVALID: Unexpected end of varint stream');
}

/**
 * Converts Uint8Array to Base64 with standard padding.
 */
export function uint8ArrayToBase64(bytes: Uint8Array): string {
  let binary = '';
  const len = bytes.length;
  for (let i = 0; i < len; i++) {
    const byte = bytes[i];
    if (byte !== undefined) {
      binary += String.fromCharCode(byte);
    }
  }
  return btoa(binary);
}

/**
 * Converts Base64 string to Uint8Array.
 */
export function base64ToUint8Array(base64: string): Uint8Array {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

/**
 * Encodes a list of KeyEvents into Base64(Gzip(Binary)) format.
 */
export function encodeKeylog(
  events: KeyEvent[],
  options: {
    strictIndent?: boolean;
    isTouch?: boolean;
    engineVer?: number;
  } = {}
): string {
  const engineVer = options.engineVer ?? CURRENT_ENGINE_VERSION;
  const strictIndent = options.strictIndent ?? false;
  const isTouch = options.isTouch ?? false;

  const rawBytes: number[] = [];

  // Magic 'DT'
  rawBytes.push(KEYLOG_MAGIC_0);
  rawBytes.push(KEYLOG_MAGIC_1);

  // Version 1
  rawBytes.push(KEYLOG_VERSION_1);

  // EngineVer varint
  rawBytes.push(...encodeVarint(engineVer));

  // Flags: bit0 = strictIndent, bit1 = touch
  let flags = 0;
  if (strictIndent) flags |= 0x01;
  if (isTouch) flags |= 0x02;
  rawBytes.push(flags);

  // EventCount varint
  rawBytes.push(...encodeVarint(events.length));

  // Events
  let prevT = 0;
  for (let i = 0; i < events.length; i++) {
    const ev = events[i];
    if (!ev) continue;
    const dt = i === 0 ? 0 : Math.max(0, ev.t - prevT);
    prevT = ev.t;

    rawBytes.push(...encodeVarint(dt));

    const kindByte = EVENT_KIND_MAP[ev.k];
    if (kindByte === undefined) {
      throw new Error(`Unknown event kind: ${(ev as KeyEvent).k}`);
    }
    rawBytes.push(kindByte);

    if (ev.k === 'char') {
      const code = ev.ch.charCodeAt(0);
      rawBytes.push(code & 0xff);
    }
  }

  const binaryBuffer = new Uint8Array(rawBytes);
  const compressed = gzipSync(binaryBuffer);
  return uint8ArrayToBase64(compressed);
}

/**
 * Decodes a Base64(Gzip(Binary)) keylog string.
 */
export function decodeKeylog(encodedBase64: string): DecodedKeylog {
  if (!encodedBase64 || encodedBase64.trim() === '') {
    throw new Error('KEYLOG_INVALID: empty keylog');
  }

  const compressed = base64ToUint8Array(encodedBase64);
  const binary = gunzipSync(compressed);

  if (binary.length < 5) {
    throw new Error('KEYLOG_INVALID: header too short');
  }

  const offset = { pos: 0 };

  const magic0 = binary[offset.pos++];
  const magic1 = binary[offset.pos++];
  if (magic0 !== KEYLOG_MAGIC_0 || magic1 !== KEYLOG_MAGIC_1) {
    throw new Error('KEYLOG_INVALID: invalid magic bytes');
  }

  const version = binary[offset.pos++];
  if (version !== KEYLOG_VERSION_1) {
    throw new Error(`KEYLOG_INVALID: unsupported version ${version}`);
  }

  const engineVer = decodeVarint(binary, offset);

  const flags = binary[offset.pos++];
  if (flags === undefined) {
    throw new Error('KEYLOG_INVALID: truncated flags');
  }
  const strictIndent = (flags & 0x01) !== 0;
  const isTouch = (flags & 0x02) !== 0;

  const eventCount = decodeVarint(binary, offset);
  const events: KeyEvent[] = [];

  let currentT = 0;
  for (let i = 0; i < eventCount; i++) {
    const dt = decodeVarint(binary, offset);
    if (dt > MAX_ALLOWED_DT_MS) {
      throw new Error(`KEYLOG_INVALID: dt ${dt} exceeds maximum 3600000 ms`);
    }

    currentT += dt;

    if (offset.pos >= binary.length) {
      throw new Error('KEYLOG_INVALID: unexpected end of event stream');
    }

    const kindByte = binary[offset.pos++];
    if (kindByte === undefined) {
      throw new Error('KEYLOG_INVALID: unexpected end of event stream');
    }
    const kind = KIND_EVENT_MAP[kindByte];
    if (!kind) {
      throw new Error(`KEYLOG_INVALID: unknown kind code ${kindByte}`);
    }

    if (kind === 'char') {
      if (offset.pos >= binary.length) {
        throw new Error('KEYLOG_INVALID: missing character byte for char event');
      }
      const chCode = binary[offset.pos++];
      if (chCode === undefined) {
        throw new Error('KEYLOG_INVALID: missing character byte for char event');
      }
      const ch = String.fromCharCode(chCode);
      events.push({ t: currentT, k: 'char', ch });
    } else if (kind === 'enter') {
      events.push({ t: currentT, k: 'enter' });
    } else if (kind === 'tab') {
      events.push({ t: currentT, k: 'tab' });
    } else if (kind === 'backspace') {
      events.push({ t: currentT, k: 'backspace' });
    } else if (kind === 'wordDelete') {
      events.push({ t: currentT, k: 'wordDelete' });
    } else if (kind === 'pause') {
      events.push({ t: currentT, k: 'pause' });
    } else if (kind === 'resume') {
      events.push({ t: currentT, k: 'resume' });
    } else if (kind === 'next') {
      events.push({ t: currentT, k: 'next' });
    } else if (kind === 'finishEarly') {
      events.push({ t: currentT, k: 'finishEarly' });
    } else if (kind === 'timeout') {
      events.push({ t: currentT, k: 'timeout' });
    }
  }

  return {
    header: {
      version,
      engineVer,
      strictIndent,
      isTouch,
      eventCount,
    },
    events,
  };
}
