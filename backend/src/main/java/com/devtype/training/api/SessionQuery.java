package com.devtype.training.api;

import java.util.Optional;
import java.util.UUID;

public interface SessionQuery {
    Optional<SessionSummaryView> findSessionSummary(UUID sessionId);

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
}
