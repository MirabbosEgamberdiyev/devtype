package com.devtype.training.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MetricsCalculator Tests — docs/00 §4.5, docs/07 §6 & AC-TE-3, AC-TE-9")
class MetricsCalculatorTests {

    private EngineState makeMockState(
            int activeMs,
            int correctKeys,
            int incorrectKeys,
            int totalTyped,
            int finishedCorrect,
            List<Integer> perSecond
    ) {
        TargetChar tc = new TargetChar('a', 0, 0, false, 0, -1, 0);
        return new EngineState(
                EnginePhase.FINISHED,
                EngineMode.PRACTICE,
                ErrorMode.STOP,
                List.of(List.of(tc)),
                0,
                List.of(CharState.CORRECT),
                Collections.singletonList(null),
                List.of(false),
                1,
                0,
                activeMs,
                activeMs,
                null,
                correctKeys,
                incorrectKeys,
                0,
                totalTyped,
                finishedCorrect,
                perSecond != null ? perSecond : List.of(4, 4, 4, 4),
                List.of()
        );
    }

    @Test
    @DisplayName("AC-TE-3 Case 1: 250 chars in 60s, 0 errors -> 50.00 WPM, 100.00% acc")
    void testCase1() {
        EngineState state = makeMockState(60000, 250, 0, 250, 249, null);
        EngineMetrics m = MetricsCalculator.calculate(state);

        assertThat(m.wpm()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(m.rawWpm()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(m.cpm()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(m.accuracy()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("AC-TE-3 Case 2: 600 correct keys, 20 errors in 60s -> 120.00 WPM, 124.00 raw, 96.77% acc")
    void testCase2() {
        EngineState state = makeMockState(60000, 600, 20, 620, 599, null);
        EngineMetrics m = MetricsCalculator.calculate(state);

        assertThat(m.wpm()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(m.rawWpm()).isEqualByComparingTo(new BigDecimal("124.00"));
        assertThat(m.cpm()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(m.accuracy()).isEqualByComparingTo(new BigDecimal("96.77"));
    }

    @Test
    @DisplayName("AC-TE-3 Case 3: 150 chars in 30s -> 60.00 WPM, 300.00 CPM")
    void testCase3() {
        EngineState state = makeMockState(30000, 150, 0, 150, 149, null);
        EngineMetrics m = MetricsCalculator.calculate(state);

        assertThat(m.wpm()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(m.cpm()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(m.accuracy()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("AC-TE-3 Case 4: 100 chars in 15s with 5 errors -> 80.00 WPM, 84.00 raw, 95.24% acc")
    void testCase4() {
        EngineState state = makeMockState(15000, 100, 5, 105, 99, null);
        EngineMetrics m = MetricsCalculator.calculate(state);

        assertThat(m.wpm()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(m.rawWpm()).isEqualByComparingTo(new BigDecimal("84.00"));
        assertThat(m.accuracy()).isEqualByComparingTo(new BigDecimal("95.24"));
    }

    @Test
    @DisplayName("AC-TE-3 Case 5: activeMs = 0 -> all rates 0.00, no division by zero")
    void testCase5ZeroActiveMs() {
        EngineState state = makeMockState(0, 0, 0, 0, 0, null);
        EngineMetrics m = MetricsCalculator.calculate(state);

        assertThat(m.wpm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(m.rawWpm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(m.cpm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(m.accuracy()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("AC-TE-3 Case 6: HALF_UP rounding tests")
    void testCase6Rounding() {
        assertThat(EngineMetrics.roundHalfUp(1.005, 2)).isEqualByComparingTo(new BigDecimal("1.01"));
        assertThat(EngineMetrics.roundHalfUp(1.004, 2)).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(EngineMetrics.roundHalfUp(25.555, 2)).isEqualByComparingTo(new BigDecimal("25.56"));
    }

    @Test
    @DisplayName("AC-TE-3 Case 8: uniform typing -> 100.00% consistency")
    void testConsistencyUniform() {
        BigDecimal cons = MetricsCalculator.calculateConsistency(List.of(5, 5, 5, 5));
        assertThat(cons).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("AC-TE-3 Case 9: non-uniform typing consistency -> 47.30%")
    void testConsistencyVariance() {
        BigDecimal cons = MetricsCalculator.calculateConsistency(List.of(10, 2, 8, 4));
        assertThat(cons).isEqualByComparingTo(new BigDecimal("47.30"));
    }

    @Test
    @DisplayName("AC-TE-3 Case 10: < 3 full seconds of data -> consistency = 0.00%")
    void testConsistencyShort() {
        assertThat(MetricsCalculator.calculateConsistency(List.of(5, 5))).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(MetricsCalculator.calculateConsistency(List.of())).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
