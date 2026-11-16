package com.devtype.gamification.api;

public record GamificationSummary(
    Long userId,
    int totalXp,
    int level,
    int streakDays,
    String rankTitle
) {}
