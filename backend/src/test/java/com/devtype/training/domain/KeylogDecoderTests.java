package com.devtype.training.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KeylogDecoder Tests — docs/07 §7 & AC-TE-16")
class KeylogDecoderTests {

    private void writeVarint(ByteArrayOutputStream out, int val) {
        int value = val;
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value & 0x7F);
    }

    private String encodeToWireFormat(boolean strictIndent, int eventCount, byte[] eventsPayload) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write(KeylogDecoder.MAGIC_0);
        raw.write(KeylogDecoder.MAGIC_1);
        raw.write(KeylogDecoder.VERSION_1);
        writeVarint(raw, 10000); // engineVer 1.0.0
        raw.write(strictIndent ? 0x01 : 0x00); // flags
        writeVarint(raw, eventCount);
        if (eventsPayload != null) {
            raw.write(eventsPayload);
        }

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(raw.toByteArray());
        }

        return Base64.getEncoder().encodeToString(compressed.toByteArray());
    }

    @Test
    @DisplayName("decodes valid header and single char event")
    void testDecodesValidKeylog() throws IOException {
        ByteArrayOutputStream events = new ByteArrayOutputStream();
        writeVarint(events, 0); // dt = 0
        events.write(0); // kind = 0 (CHAR)
        events.write((byte) 'A'); // ch = 'A'

        String wire = encodeToWireFormat(false, 1, events.toByteArray());
        KeylogDecoder.DecodedKeylog decoded = KeylogDecoder.decode(wire);

        assertThat(decoded.header().version()).isEqualTo(1);
        assertThat(decoded.header().engineVer()).isEqualTo(10000);
        assertThat(decoded.header().strictIndent()).isFalse();
        assertThat(decoded.header().eventCount()).isEqualTo(1);
        assertThat(decoded.events()).hasSize(1);
        assertThat(decoded.events().get(0).ch()).isEqualTo('A');
    }

    @Test
    @DisplayName("AC-TE-16: rejects keylog with invalid magic bytes")
    void testRejectsInvalidMagic() throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write(0x00);
        raw.write(0x00); // wrong magic
        raw.write(0x01);

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(raw.toByteArray());
        }
        String wire = Base64.getEncoder().encodeToString(compressed.toByteArray());

        assertThatThrownBy(() -> KeylogDecoder.decode(wire))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("KEYLOG_INVALID");
    }

    @Test
    @DisplayName("AC-TE-16: rejects dt > 3600000 ms")
    void testRejectsExcessiveDt() throws IOException {
        ByteArrayOutputStream events = new ByteArrayOutputStream();
        writeVarint(events, KeylogDecoder.MAX_DT_MS + 10); // dt > 1 hour
        events.write(0); // CHAR
        events.write((byte) 'x');

        String wire = encodeToWireFormat(false, 1, events.toByteArray());

        assertThatThrownBy(() -> KeylogDecoder.decode(wire))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("KEYLOG_INVALID");
    }
}
