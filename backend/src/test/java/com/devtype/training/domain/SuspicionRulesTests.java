package com.devtype.training.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SuspicionRules Anti-Cheat Tests — docs/00 §4.6, docs/07 §9.3 & AC-SP-20")
class SuspicionRulesTests {

    private EngineMetrics mockMetrics(double wpm, int incorrectKeys, int correctChars) {
        return new EngineMetrics(
                BigDecimal.valueOf(wpm),
                BigDecimal.valueOf(wpm),
                BigDecimal.valueOf(wpm * 5),
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(100.0),
                60000,
                correctChars + incorrectKeys,
                correctChars,
                0,
                correctChars,
                incorrectKeys,
                0,
                BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(100.0)
        );
    }

    @Test
    @DisplayName("AC-SP-20: flat rhythm keylog triggers FLAT_RHYTHM and gets flagged if combined with another rule")
    void testFlatRhythmKeylog() {
        // Exactly 50ms interval across 40 keystrokes (CV = 0 < 0.15 -> +40)
        List<KeyEvent> events = new ArrayList<>();
        int t = 0;
        for (int i = 0; i < 40; i++) {
            events.add(KeyEvent.ofChar(t, 'a'));
            t += 50; // exactly 50ms between keys
        }

        // Suppose user is jumping from median 40 WPM to 80 WPM (JUMP -> +20)
        // 40 + 20 = 60 >= 50 threshold -> flagged!
        EngineMetrics metrics = mockMetrics(80.0, 0, 40);
        SuspicionRules.SuspicionContext ctx = new SuspicionRules.SuspicionContext(40.0, 15);

        SuspicionRules.SuspicionEvaluation eval = SuspicionRules.evaluate(metrics, events, ctx);
        assertThat(eval.reasons()).contains("FLAT_RHYTHM", "JUMP");
        assertThat(eval.score()).isEqualTo(60);
        assertThat(eval.flagged()).isTrue();
    }

    @Test
    @DisplayName("superhuman speed > 220 WPM alone triggers flag (+50)")
    void testSuperhumanSpeed() {
        List<KeyEvent> events = List.of(KeyEvent.ofChar(0, 'a'));
        EngineMetrics metrics = mockMetrics(225.0, 0, 500);

        SuspicionRules.SuspicionEvaluation eval = SuspicionRules.evaluate(
                metrics,
                events,
                SuspicionRules.SuspicionContext.empty()
        );

        assertThat(eval.reasons()).contains("SPEED");
        assertThat(eval.score()).isGreaterThanOrEqualTo(50);
        assertThat(eval.flagged()).isTrue();
    }

    @Test
    @DisplayName("fast intervals (< 20ms share > 20%) triggers FAST_INTERVALS (+50)")
    void testFastIntervals() {
        List<KeyEvent> events = new ArrayList<>();
        int t = 0;
        // 40 keystrokes: 10 with 5ms (<20ms: 25% > 20%), 30 with 100ms
        for (int i = 0; i < 10; i++) {
            events.add(KeyEvent.ofChar(t, 'a'));
            t += 5;
        }
        for (int i = 0; i < 30; i++) {
            events.add(KeyEvent.ofChar(t, 'b'));
            t += 100;
        }

        EngineMetrics metrics = mockMetrics(60.0, 0, 40);
        SuspicionRules.SuspicionEvaluation eval = SuspicionRules.evaluate(
                metrics,
                events,
                SuspicionRules.SuspicionContext.empty()
        );

        assertThat(eval.reasons()).contains("FAST_INTERVALS");
        assertThat(eval.score()).isGreaterThanOrEqualTo(50);
        assertThat(eval.flagged()).isTrue();
    }
}
