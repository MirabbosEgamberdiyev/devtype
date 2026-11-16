package com.devtype.gamification.api;

import java.time.Instant;

public record LevelUp(
    Long userId,
    int oldLevel,
    int newLevel,
    Instant timestamp
) {}
