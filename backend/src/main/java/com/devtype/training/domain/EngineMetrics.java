package com.devtype.training.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculated session performance metrics — docs/00 §4.5, docs/07 §6.
 */
public record EngineMetrics(
        BigDecimal wpm,
        BigDecimal rawWpm,
        BigDecimal cpm,
        BigDecimal accuracy,
        BigDecimal consistency,
        int elapsedMs,
        int totalTyped,
        int correctChars,
        int uncorrectedErrors,
        int correctKeys,
        int incorrectKeys,
        int backspaceCount,
        BigDecimal completionPct,
        BigDecimal progressPct
) {
    public static BigDecimal roundHalfUp(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return BigDecimal.ZERO.setScale(decimals, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP);
    }
}
