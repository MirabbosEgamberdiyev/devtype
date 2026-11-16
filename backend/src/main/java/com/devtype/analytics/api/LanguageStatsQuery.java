package com.devtype.analytics.api;

public interface LanguageStatsQuery {
    LanguageStatsView getStats(Long userId, String languageSlug);

    record LanguageStatsView(
        long totalSessions,
        double medianWpm,
        double bestWpm,
        double averageAccuracy,
        long totalSeconds
    ) {}
}
