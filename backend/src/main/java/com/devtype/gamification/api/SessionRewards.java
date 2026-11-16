package com.devtype.gamification.api;

import java.util.List;

public record SessionRewards(
    int earnedXp,
    int totalXp,
    int level,
    boolean levelUp,
    int streakDays,
    boolean streakMaintained,
    boolean personalRecordBroken,
    List<String> unlockedAchievements
) {}
