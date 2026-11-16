package com.devtype.gamification.api;

import java.time.Instant;

public record StreakUpdated(
    Long userId,
    int streakDays,
    boolean freezeUsed,
    Instant timestamp
) {}
