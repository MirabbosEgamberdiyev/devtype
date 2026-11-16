package com.devtype.training.application;

import java.util.List;
import java.util.UUID;

public record CompleteSessionResult(
        UUID sessionId,
        String status,
        boolean ranked,
        String rankedReason,
        MetricsDto metrics,
        List<WpmPointDto> wpmSeries,
        List<WeakTokenDto> weakTokens,
        List<SlowLineDto> slowLines,
        Object rewards,
        Object progress,
        Object coachTip,
        Object next
) {
    public record MetricsDto(
            double wpm,
            double rawWpm,
            double cpm,
            double accuracy,
            double consistency,
            int errors,
            int uncorrectedErrors,
            int backspaces,
            int correctChars,
            int elapsedMs,
            double completionPct,
            double specialAccuracy
    ) {}

    public record WpmPointDto(
            int t,
            double wpm,
            double raw,
            int errors
    ) {}

    public record WeakTokenDto(
            String token,
            int errors,
            int expected,
            int avgMs
    ) {}

    public record SlowLineDto(
            int line,
            int ms
    ) {}
}
