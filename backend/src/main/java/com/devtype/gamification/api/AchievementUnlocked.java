package com.devtype.gamification.api;

import java.time.Instant;

public record AchievementUnlocked(
    Long userId,
    String achievementKey,
    int xpReward,
    Instant timestamp
) {}
