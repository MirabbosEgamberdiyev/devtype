package com.devtype.gamification.api;

import java.time.Instant;

public record RecordBroken(
    Long userId,
    String languageSlug,
    String mode,
    double oldWpm,
    double newWpm,
    Instant timestamp
) {}
