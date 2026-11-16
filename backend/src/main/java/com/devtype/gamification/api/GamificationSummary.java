package com.devtype.gamification.api;

public record GamificationSummary(
    Long userId,
    long totalXp,
    int level,
    long xpIntoLevel,
    int xpToNext,
    int streakDays,
    String rankTitle
) {}
