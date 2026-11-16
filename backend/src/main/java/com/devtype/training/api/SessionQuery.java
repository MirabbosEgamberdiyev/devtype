package com.devtype.training.api;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionQuery {
    Optional<SessionSummaryView> findSessionSummary(UUID sessionId);

    long sumActiveMsBetween(Long userId, Instant from, Instant to);

    int countCompletedSessionsBetween(Long userId, Instant from, Instant to);

    int countCompletedForExerciseSince(Long userId, Long exerciseId, Instant since);

    List<Double> findRecentCoreWpms(Long userId, short languageId, int limit);

    List<SessionHistoryItemView> findSessions(
            Long userId,
            String langSlug,
            String mode,
            String status,
            Instant from,
            Instant to,
            int limit
    );

    record SessionSummaryView(
        UUID sessionId,
        Long userId,
        String languageSlug,
        String mode,
        String status,
        double wpm,
        double accuracy,
        boolean ranked
    ) {}

    record SessionHistoryItemView(
        UUID id,
        String language,
        String mode,
        String topic,
        String difficulty,
        Integer durationLimitS,
        String status,
        double wpm,
        double accuracy,
        long elapsedMs,
        int xp,
        boolean ranked,
        Instant finishedAt,
        boolean replayAvailable
    ) {}
}
