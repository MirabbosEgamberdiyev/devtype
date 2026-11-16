package com.devtype.analytics.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    public record OverviewResponse(
            KpiDto kpi,
            List<LanguageSummaryDto> languages,
            WeakestLanguageDto weakestLanguage
    ) {}

    public record KpiDto(
            Double currentWpm,
            double avgWpm,
            double maxWpm,
            double avgCpm,
            double accuracy,
            double specialAccuracy,
            long totalTimeMs,
            long totalChars,
            int sessions,
            long errors
    ) {}

    public record LanguageSummaryDto(
            String slug,
            String name,
            Double currentWpm,
            double avgWpm,
            double maxWpm,
            double accuracy,
            int sessionsCnt,
            long totalTimeMs
    ) {}

    public record WeakestLanguageDto(
            String languageSlug,
            double recentWpm,
            double medianWpm,
            double ratio
    ) {}

    public record LanguageDetailResponse(
            KpiDto kpi,
            List<SeriesPointDto> series,
            boolean enoughData,
            int sessionsNeeded
    ) {}

    public record SeriesPointDto(
            LocalDate date,
            double avgWpm,
            double maxWpm,
            double accuracy,
            int sessions,
            long timeMs
    ) {}

    public record CharStatItemDto(
            String token,
            long expected,
            long errors,
            double errorRate,
            double avgMs,
            double ewmaError,
            double ewmaMs,
            Double weaknessScore,
            Map<String, Integer> confusions
    ) {}

    public record CategoryStatDto(
            String category,
            double accuracy,
            double avgMs,
            long totalKeys,
            long errorCount
    ) {}

    public record ActivityDayDto(
            LocalDate date,
            long timeMs,
            int sessions,
            long chars,
            int level
    ) {}

    public record RecommendationsResponse(
            List<Character> weakCharacters,
            List<String> weakBigrams,
            WeakestLanguageDto weakestLanguage,
            String recommendedMode
    ) {}
}
