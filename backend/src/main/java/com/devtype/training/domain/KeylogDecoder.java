package com.devtype.training.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Keylog Wire Format Decoder — docs/07 §7.
 * Decodes Base64(Gzip(Binary)) keylogs with strict anti-tampering limits.
 */
public final class KeylogDecoder {

    public static final byte MAGIC_0 = 0x44; // 'D'
    public static final byte MAGIC_1 = 0x54; // 'T'
    public static final byte VERSION_1 = 0x01;
    public static final int MAX_UNCOMPRESSED_BYTES = 1024 * 1024; // 1 MB (docs/00 §4.6)
    public static final int MAX_EVENTS_LIMIT = 50_000; // 50,000 events (docs/00 §4.6)
    public static final int MAX_DT_MS = 3_600_000; // 1 hour (docs/07 §7)

    public record KeylogHeader(
            int version,
            int engineVer,
            boolean strictIndent,
            boolean isTouch,
            int eventCount
    ) {}

    public record DecodedKeylog(
            KeylogHeader header,
            List<KeyEvent> events
    ) {}

    private KeylogDecoder() {
    }

    /**
     * Decodes an unsigned LEB128 varint from the byte array.
     */
    public static int decodeVarint(byte[] bytes, int[] offset) {
        int result = 0;
        int shift = 0;
        while (offset[0] < bytes.length) {
            byte b = bytes[offset[0]++];
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("KEYLOG_INVALID: unexpected end of varint stream");
    }

    /**
     * Decodes Base64(Gzip(Binary)) into DecodedKeylog.
     */
    public static DecodedKeylog decode(String base64GzipKeylog) {
        if (base64GzipKeylog == null || base64GzipKeylog.isBlank()) {
            throw new IllegalArgumentException("KEYLOG_INVALID: empty keylog payload");
        }

        byte[] compressed;
        try {
            compressed = Base64.getDecoder().decode(base64GzipKeylog.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("KEYLOG_INVALID: invalid base64 encoding", e);
        }

        byte[] binary;
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int read;
            int total = 0;
            while ((read = gzip.read(buffer)) != -1) {
                total += read;
                if (total > MAX_UNCOMPRESSED_BYTES) {
                    throw new IllegalArgumentException("KEYLOG_INVALID: uncompressed keylog exceeds 1MB limit");
                }
                out.write(buffer, 0, read);
            }
            binary = out.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("KEYLOG_INVALID: corrupted gzip stream", e);
        }

        if (binary.length < 5) {
            throw new IllegalArgumentException("KEYLOG_INVALID: payload too short for header");
        }

        int[] offset = new int[]{0};

        byte m0 = binary[offset[0]++];
        byte m1 = binary[offset[0]++];
        if (m0 != MAGIC_0 || m1 != MAGIC_1) {
            throw new IllegalArgumentException("KEYLOG_INVALID: invalid magic header");
        }

        byte ver = binary[offset[0]++];
        if (ver != VERSION_1) {
            throw new IllegalArgumentException("KEYLOG_INVALID: unsupported keylog version: " + ver);
        }

        int engineVer = decodeVarint(binary, offset);

        if (offset[0] >= binary.length) {
            throw new IllegalArgumentException("KEYLOG_INVALID: truncated flags");
        }
        byte flags = binary[offset[0]++];
        boolean strictIndent = (flags & 0x01) != 0;
        boolean isTouch = (flags & 0x02) != 0;

        int eventCount = decodeVarint(binary, offset);
        if (eventCount > MAX_EVENTS_LIMIT) {
            throw new IllegalArgumentException("KEYLOG_INVALID: event count " + eventCount + " exceeds 50000 limit");
        }

        List<KeyEvent> events = new ArrayList<>(eventCount);
        int currentT = 0;

        for (int i = 0; i < eventCount; i++) {
            int dt = decodeVarint(binary, offset);
            if (dt > MAX_DT_MS) {
                throw new IllegalArgumentException("KEYLOG_INVALID: dt " + dt + " exceeds maximum allowed 3600000ms");
            }
            currentT += dt;

            if (offset[0] >= binary.length) {
                throw new IllegalArgumentException("KEYLOG_INVALID: unexpected end of event payload");
            }

            int kindByte = binary[offset[0]++] & 0xFF;
            KeyKind kind = KeyKind.fromCode(kindByte);

            if (kind == KeyKind.CHAR) {
                if (offset[0] >= binary.length) {
                    throw new IllegalArgumentException("KEYLOG_INVALID: missing character byte for CHAR event");
                }
                int chByte = binary[offset[0]++] & 0xFF;
                if (chByte < 0x20 || chByte > 0x7E) {
                    throw new IllegalArgumentException("KEYLOG_INVALID: char byte 0x" + Integer.toHexString(chByte) + " outside ASCII printable range");
                }
                events.add(KeyEvent.ofChar(currentT, (char) chByte));
            } else if (kind == KeyKind.ENTER) {
                events.add(KeyEvent.ofEnter(currentT));
            } else if (kind == KeyKind.TAB) {
                events.add(KeyEvent.ofTab(currentT));
            } else if (kind == KeyKind.BACKSPACE) {
                events.add(KeyEvent.ofBackspace(currentT));
            } else if (kind == KeyKind.WORD_DELETE) {
                events.add(KeyEvent.ofWordDelete(currentT));
            } else if (kind == KeyKind.PAUSE) {
                events.add(KeyEvent.ofPause(currentT));
            } else if (kind == KeyKind.RESUME) {
                events.add(KeyEvent.ofResume(currentT));
            } else if (kind == KeyKind.NEXT) {
                events.add(KeyEvent.ofNext(currentT));
            } else if (kind == KeyKind.FINISH_EARLY) {
                events.add(KeyEvent.ofFinishEarly(currentT));
            } else if (kind == KeyKind.TIMEOUT) {
                events.add(KeyEvent.ofTimeout(currentT));
            }
        }

        KeylogHeader header = new KeylogHeader(ver, engineVer, strictIndent, isTouch, eventCount);
        return new DecodedKeylog(header, events);
    }
}
