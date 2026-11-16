package com.devtype.analytics.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DashboardDtos {

    private DashboardDtos() {}

    public record DashboardResponse(
            ResumeDto resume,
            DashboardGamificationDto gamification,
            List<DashboardLanguageDto> languages,
            List<DashboardRecentSessionDto> recentSessions,
            Object dailyChallenge,
            Object coachPlan,
            boolean isNewUser
    ) {}

    public record ResumeDto(
            String language,
            String topic,
            String topicName,
            String difficulty,
            String exerciseKey,
            String exerciseTitle,
            int completed,
            int total
    ) {}

    public record DashboardGamificationDto(
            long totalXp,
            int level,
            long xpIntoLevel,
            int xpToNext,
            DashboardStreakDto streak
    ) {}

    public record DashboardStreakDto(
            int current,
            int longest,
            boolean doneToday,
            boolean atRisk,
            int freezesLeft
    ) {}

    public record DashboardLanguageDto(
            String slug,
            String name,
            boolean primary,
            Double currentWpm,
            String rank,
            int progressPct,
            List<Double> sparkline
    ) {}

    public record DashboardRecentSessionDto(
            UUID id,
            String language,
            String mode,
            double wpm,
            double accuracy,
            Instant finishedAt,
            int xp
    ) {}
}
