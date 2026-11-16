package com.devtype.gamification.application;

import com.devtype.gamification.domain.XpBreakdown;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class GamificationDtos {

    private GamificationDtos() {}

    public record RewardsDto(
            int xp,
            XpBreakdown xpBreakdown,
            long totalXp,
            int level,
            Integer levelUp,
            StreakDto streak,
            RecordDto personalRecord,
            RankChangeDto rankChange
    ) {}

    public record StreakDto(
            int current,
            boolean extendedToday
    ) {}

    public record RecordDto(
            Double previous,
            Double current
    ) {}

    public record RankChangeDto(
            String from,
            String to
    ) {}

    public record GamificationMeResponse(
            long totalXp,
            int level,
            long xpIntoLevel,
            int xpToNext,
            StreakStatusDto streak,
            List<LanguageRankDto> ranks
    ) {}

    public record StreakStatusDto(
            int current,
            int longest,
            boolean doneToday,
            boolean atRisk,
            int freezesLeft
    ) {}

    public record LanguageRankDto(
            String slug,
            String rank,
            double recentWpm
    ) {}

    public record XpTransactionDto(
            Long id,
            int amount,
            String sourceType,
            String sourceId,
            String details,
            Instant createdAt
    ) {}

    public record PersonalRecordItemDto(
            String languageSlug,
            String mode,
            int durationLimitS,
            double bestWpm,
            double accuracy,
            UUID sessionId,
            Instant achievedAt
    ) {}
}
