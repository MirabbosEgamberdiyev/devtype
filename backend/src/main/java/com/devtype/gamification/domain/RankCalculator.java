package com.devtype.gamification.domain;

public class RankCalculator {

    public static final int MIN_SESSIONS_FOR_RANK = 10;
    public static final double HYSTERESIS_WPM = 2.0;

    public record RankResult(
            LanguageRank rank,
            boolean changed,
            LanguageRank previousRank
    ) {}

    public static LanguageRank calculateRawRank(double recentWpm) {
        if (recentWpm >= LanguageRank.DIAMOND.getMinWpm()) {
            return LanguageRank.DIAMOND;
        }
        if (recentWpm >= LanguageRank.PLATINUM.getMinWpm()) {
            return LanguageRank.PLATINUM;
        }
        if (recentWpm >= LanguageRank.GOLD.getMinWpm()) {
            return LanguageRank.GOLD;
        }
        if (recentWpm >= LanguageRank.SILVER.getMinWpm()) {
            return LanguageRank.SILVER;
        }
        return LanguageRank.BRONZE;
    }

    public static RankResult evaluateRank(int coreSessionsCount, double recentWpm, LanguageRank currentRank) {
        if (coreSessionsCount < MIN_SESSIONS_FOR_RANK) {
            return new RankResult(LanguageRank.UNRANKED, currentRank != LanguageRank.UNRANKED, currentRank);
        }

        if (currentRank == null || currentRank == LanguageRank.UNRANKED) {
            LanguageRank newRank = calculateRawRank(recentWpm);
            return new RankResult(newRank, true, currentRank);
        }

        LanguageRank naturalRank = calculateRawRank(recentWpm);

        // Check upgrade: immediate
        if (naturalRank.ordinal() > currentRank.ordinal()) {
            return new RankResult(naturalRank, true, currentRank);
        }

        // Check downgrade: only if recentWpm drops at least 2.0 WPM below currentRank entry threshold
        if (naturalRank.ordinal() < currentRank.ordinal()) {
            double downgradeBoundary = currentRank.getMinWpm() - HYSTERESIS_WPM;
            if (recentWpm < downgradeBoundary) {
                // Determine new downgraded rank using hysteresis boundary
                LanguageRank target = naturalRank;
                return new RankResult(target, true, currentRank);
            }
        }

        // Maintain current rank
        return new RankResult(currentRank, false, currentRank);
    }
}
