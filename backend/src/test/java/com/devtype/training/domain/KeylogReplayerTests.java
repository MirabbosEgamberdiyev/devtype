package com.devtype.training.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KeylogReplayer Tests — docs/07 §8 & AC-TE-7, 8")
class KeylogReplayerTests {

    private void writeVarint(ByteArrayOutputStream out, int val) {
        int value = val;
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value & 0x7F);
    }

    private String createKeylog(List<KeyEvent> events, boolean strictIndent) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write(KeylogDecoder.MAGIC_0);
        raw.write(KeylogDecoder.MAGIC_1);
        raw.write(KeylogDecoder.VERSION_1);
        writeVarint(raw, 10000);
        raw.write(strictIndent ? 0x01 : 0x00);
        writeVarint(raw, events.size());

        int prevT = 0;
        for (int i = 0; i < events.size(); i++) {
            KeyEvent ev = events.get(i);
            int dt = i == 0 ? 0 : Math.max(0, ev.t() - prevT);
            prevT = ev.t();

            writeVarint(raw, dt);
            raw.write(ev.kind().getCode());
            if (ev.kind() == KeyKind.CHAR && ev.ch() != null) {
                raw.write((byte) ev.ch().charValue());
            }
        }

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(raw.toByteArray());
        }
        return Base64.getEncoder().encodeToString(compressed.toByteArray());
    }

    @Test
    @DisplayName("replays valid session accurately to calculate metrics and stats")
    void testReplaySession() throws IOException {
        String snippet = "let x = 1;";
        List<KeyEvent> events = List.of(
                KeyEvent.ofChar(0, 'l'),
                KeyEvent.ofChar(100, 'e'),
                KeyEvent.ofChar(200, 't'),
                KeyEvent.ofChar(300, ' '),
                KeyEvent.ofChar(400, 'x'),
                KeyEvent.ofChar(500, ' '),
                KeyEvent.ofChar(600, '='),
                KeyEvent.ofChar(700, ' '),
                KeyEvent.ofChar(800, '1'),
                KeyEvent.ofChar(900, ';')
        );

        String base64Keylog = createKeylog(events, false);

        KeylogReplayer.ReplaySessionConfig cfg = new KeylogReplayer.ReplaySessionConfig(
                EngineMode.PRACTICE,
                ErrorMode.STOP,
                null,
                false
        );

        ReplayResult res = KeylogReplayer.replay(
                cfg,
                List.of(snippet),
                LanguageConfig.defaultConfig(),
                base64Keylog,
                SuspicionRules.SuspicionContext.empty()
        );

        assertThat(res.metrics().accuracy()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(res.metrics().uncorrectedErrors()).isEqualTo(0);
        assertThat(res.metrics().correctChars()).isEqualTo(10);
        assertThat(res.suspicion().flagged()).isFalse();
    }

    @Test
    @DisplayName("rejects structural NEXT before completing current snippet")
    void testRejectsPrematureNext() throws IOException {
        String snippet = "hello";
        List<KeyEvent> events = List.of(
                KeyEvent.ofChar(0, 'h'),
                KeyEvent.ofNext(100) // snippet has 5 chars, NEXT sent at position 1
        );

        String base64Keylog = createKeylog(events, false);
        KeylogReplayer.ReplaySessionConfig cfg = new KeylogReplayer.ReplaySessionConfig(
                EngineMode.PRACTICE,
                ErrorMode.STOP,
                null,
                false
        );

        assertThatThrownBy(() -> KeylogReplayer.replay(
                cfg,
                List.of(snippet),
                LanguageConfig.defaultConfig(),
                base64Keylog,
                SuspicionRules.SuspicionContext.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("KEYLOG_INVALID");
    }
}
