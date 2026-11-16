package com.devtype.training.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SessionCompleted(
    UUID sessionId,
    Long userId,
    String languageSlug,
    String mode,
    double wpm,
    double rawWpm,
    double accuracy,
    double consistency,
    int activeMs,
    boolean ranked,
    boolean claimed,
    List<ExerciseSummary> exercises,
    SessionOutcomeCollector collector,
    Instant timestamp
) {
    public record ExerciseSummary(
        String exerciseKey,
        String level,
        String topicKey,
        int charCount
    ) {}
}
