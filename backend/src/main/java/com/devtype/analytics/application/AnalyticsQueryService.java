package com.devtype.analytics.application;

import com.devtype.analytics.infrastructure.*;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.identity.api.UserDirectory;
import com.devtype.identity.api.UserView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class AnalyticsQueryService {

    private final UserLanguageStatsRepository languageStatsRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final UserCharStatsRepository charStatsRepository;
    private final UserBigramStatsRepository bigramStatsRepository;
    private final WeaknessProfileService weaknessProfileService;
    private final WeakestLanguageService weakestLanguageService;
    private final LanguageCatalog languageCatalog;
    private final UserDirectory userDirectory;
    private final ObjectMapper objectMapper;

    public AnalyticsQueryService(
            UserLanguageStatsRepository languageStatsRepository,
            UserDailyStatsRepository dailyStatsRepository,
            UserCharStatsRepository charStatsRepository,
            UserBigramStatsRepository bigramStatsRepository,
            WeaknessProfileService weaknessProfileService,
            WeakestLanguageService weakestLanguageService,
            LanguageCatalog languageCatalog,
            UserDirectory userDirectory,
            ObjectMapper objectMapper
    ) {
        this.languageStatsRepository = languageStatsRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.charStatsRepository = charStatsRepository;
        this.bigramStatsRepository = bigramStatsRepository;
        this.weaknessProfileService = weaknessProfileService;
        this.weakestLanguageService = weakestLanguageService;
        this.languageCatalog = languageCatalog;
        this.userDirectory = userDirectory;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AnalyticsDtos.OverviewResponse getOverview(Long userId, String group) {
        String modeGroup = (group != null && !group.isBlank()) ? group.toUpperCase() : "CORE";
        List<UserLanguageStatsEntity> list = languageStatsRepository.findByUserIdAndModeGroup(userId, modeGroup);

        int totalSessions = 0;
        long totalTimeMs = 0L;
        long totalChars = 0L;
        double sumWpm = 0.0;
        double sumCpm = 0.0;
        double maxWpm = 0.0;
        long correctKeys = 0L;
        long totalKeys = 0L;
        long errorCnt = 0L;
        long specialCorrect = 0L;
        long specialTotal = 0L;
        List<Double> recentWpms = new ArrayList<>();

        List<AnalyticsDtos.LanguageSummaryDto> langDtos = new ArrayList<>();

        for (UserLanguageStatsEntity s : list) {
            String slug = languageCatalog.getPublishedLanguageSlugs().stream()
                    .filter(l -> languageCatalog.findLanguageIdBySlug(l).orElse((short) -1) == s.getLanguageId())
                    .findFirst()
                    .orElse("unknown");

            double avgWpm = s.getSessionsCnt() > 0 ? s.getSumWpm().doubleValue() / s.getSessionsCnt() : 0.0;
            double acc = s.getTotalKeys() > 0 ? (s.getCorrectKeys() * 100.0 / s.getTotalKeys()) : 100.0;
            Double rWpm = s.getRecentWpm() != null ? s.getRecentWpm().doubleValue() : null;
            if (rWpm != null) {
                recentWpms.add(rWpm);
            }

            langDtos.add(new AnalyticsDtos.LanguageSummaryDto(
                    slug,
                    slug.toUpperCase(),
                    rWpm,
                    round2(avgWpm),
                    s.getMaxWpm().doubleValue(),
                    round2(acc),
                    s.getSessionsCnt(),
                    s.getTotalTimeMs()
            ));

            totalSessions += s.getSessionsCnt();
            totalTimeMs += s.getTotalTimeMs();
            totalChars += s.getTotalChars();
            sumWpm += s.getSumWpm().doubleValue();
            sumCpm += s.getSumCpm().doubleValue();
            maxWpm = Math.max(maxWpm, s.getMaxWpm().doubleValue());
            correctKeys += s.getCorrectKeys();
            totalKeys += s.getTotalKeys();
            errorCnt += s.getErrorCnt();
            specialCorrect += s.getSpecialCorrect();
            specialTotal += s.getSpecialTotal();
        }

        Double overallCurrentWpm = recentWpms.isEmpty() ? null : round2(computeMedian(recentWpms));
        double overallAvgWpm = totalSessions > 0 ? round2(sumWpm / totalSessions) : 0.0;
        double overallAvgCpm = totalSessions > 0 ? round2(sumCpm / totalSessions) : 0.0;
        double overallAcc = totalKeys > 0 ? round2(correctKeys * 100.0 / totalKeys) : 100.0;
        double overallSpecialAcc = specialTotal > 0 ? round2(specialCorrect * 100.0 / specialTotal) : 100.0;

        AnalyticsDtos.KpiDto kpi = new AnalyticsDtos.KpiDto(
                overallCurrentWpm,
                overallAvgWpm,
                maxWpm,
                overallAvgCpm,
                overallAcc,
                overallSpecialAcc,
                totalTimeMs,
                totalChars,
                totalSessions,
                errorCnt
        );

        AnalyticsDtos.WeakestLanguageDto weakestDto = weakestLanguageService.findWeakestLanguage(userId)
                .map(w -> new AnalyticsDtos.WeakestLanguageDto(w.languageSlug(), w.recentWpm(), w.medianWpm(), w.ratio()))
                .orElse(null);

        return new AnalyticsDtos.OverviewResponse(kpi, langDtos, weakestDto);
    }

    @Transactional(readOnly = true)
    public AnalyticsDtos.LanguageDetailResponse getLanguageAnalytics(
            Long userId,
            String slug,
            String period,
            String bucket,
            String group
    ) {
        short langId = languageCatalog.findLanguageIdBySlug(slug).orElse((short) 1);
        String modeGroup = (group != null && !group.isBlank()) ? group.toUpperCase() : "CORE";

        Optional<UserLanguageStatsEntity> statsOpt = languageStatsRepository
                .findByUserIdAndLanguageIdAndModeGroup(userId, langId, modeGroup);

        int totalSessions = statsOpt.map(UserLanguageStatsEntity::getSessionsCnt).orElse(0);
        long totalTimeMs = statsOpt.map(UserLanguageStatsEntity::getTotalTimeMs).orElse(0L);
        long totalChars = statsOpt.map(UserLanguageStatsEntity::getTotalChars).orElse(0L);
        double maxWpm = statsOpt.map(s -> s.getMaxWpm().doubleValue()).orElse(0.0);
        Double currentWpm = statsOpt.map(s -> s.getRecentWpm() != null ? s.getRecentWpm().doubleValue() : null).orElse(null);
        double avgWpm = (statsOpt.isPresent() && totalSessions > 0) ? round2(statsOpt.get().getSumWpm().doubleValue() / totalSessions) : 0.0;
        double avgCpm = (statsOpt.isPresent() && totalSessions > 0) ? round2(statsOpt.get().getSumCpm().doubleValue() / totalSessions) : 0.0;
        long correctKeys = statsOpt.map(UserLanguageStatsEntity::getCorrectKeys).orElse(0L);
        long totalKeys = statsOpt.map(UserLanguageStatsEntity::getTotalKeys).orElse(0L);
        double acc = totalKeys > 0 ? round2(correctKeys * 100.0 / totalKeys) : 100.0;
        long specialCorrect = statsOpt.map(UserLanguageStatsEntity::getSpecialCorrect).orElse(0L);
        long specialTotal = statsOpt.map(UserLanguageStatsEntity::getSpecialTotal).orElse(0L);
        double specialAcc = specialTotal > 0 ? round2(specialCorrect * 100.0 / specialTotal) : 100.0;
        long errors = statsOpt.map(UserLanguageStatsEntity::getErrorCnt).orElse(0L);

        AnalyticsDtos.KpiDto kpi = new AnalyticsDtos.KpiDto(
                currentWpm,
                avgWpm,
                maxWpm,
                avgCpm,
                acc,
                specialAcc,
                totalTimeMs,
                totalChars,
                totalSessions,
                errors
        );

        boolean enoughData = totalSessions >= 5;
        int sessionsNeeded = Math.max(0, 5 - totalSessions);

        if (!enoughData) {
            return new AnalyticsDtos.LanguageDetailResponse(kpi, List.of(), false, sessionsNeeded);
        }

        // Determine date range
        LocalDate now = LocalDate.now();
        LocalDate fromDate = switch (period != null ? period.toLowerCase() : "30d") {
            case "7d" -> now.minusDays(7);
            case "90d" -> now.minusDays(90);
            case "all" -> now.minusYears(10);
            default -> now.minusDays(30);
        };

        List<UserDailyStatsEntity> dailyList = dailyStatsRepository
                .findByUserIdAndLanguageIdAndDayBetweenOrderByDayAsc(userId, langId, fromDate, now);

        List<AnalyticsDtos.SeriesPointDto> series = new ArrayList<>();
        for (UserDailyStatsEntity d : dailyList) {
            if (d.getModeGroup().equalsIgnoreCase(modeGroup) && d.getSessionsCnt() > 0) {
                double dayAvgWpm = round2(d.getSumWpm().doubleValue() / d.getSessionsCnt());
                double dayAcc = d.getTotalKeys() > 0 ? round2(d.getCorrectKeys() * 100.0 / d.getTotalKeys()) : 100.0;
                series.add(new AnalyticsDtos.SeriesPointDto(
                        d.getDay(),
                        dayAvgWpm,
                        d.getMaxWpm().doubleValue(),
                        dayAcc,
                        d.getSessionsCnt(),
                        d.getTimeMs()
                ));
            }
        }

        return new AnalyticsDtos.LanguageDetailResponse(kpi, series, true, 0);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDtos.CharStatItemDto> getCharAnalytics(Long userId, String slug) {
        short langId = languageCatalog.findLanguageIdBySlug(slug).orElse((short) 1);
        List<UserCharStatsEntity> charList = charStatsRepository.findByUserIdAndLanguageId(userId, langId);

        List<AnalyticsDtos.CharStatItemDto> result = new ArrayList<>();
        for (UserCharStatsEntity c : charList) {
            double errorRate = (c.getExpectedCnt() > 0) ? (c.getErrorCnt() * 1.0 / c.getExpectedCnt()) : 0.0;
            double avgMs = (c.getTimedCnt() > 0) ? (c.getTotalMs() * 1.0 / c.getTimedCnt()) : 0.0;

            Map<String, Integer> confusionsMap = Map.of();
            if (c.getConfusions() != null && !c.getConfusions().isBlank() && !c.getConfusions().equals("{}")) {
                try {
                    confusionsMap = objectMapper.readValue(c.getConfusions(), new TypeReference<>() {});
                } catch (Exception ignored) {}
            }

            result.add(new AnalyticsDtos.CharStatItemDto(
                    c.getToken(),
                    c.getExpectedCnt(),
                    c.getErrorCnt(),
                    round4(errorRate),
                    round2(avgMs),
                    c.getEwmaError() != null ? round4(c.getEwmaError().doubleValue()) : 0.0,
                    c.getEwmaMs() != null ? round2(c.getEwmaMs().doubleValue()) : 0.0,
                    null,
                    confusionsMap
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDtos.ActivityDayDto> getActivityCalendar(Long userId, int year) {
        LocalDate fromDate = LocalDate.of(year, 1, 1);
        LocalDate toDate = LocalDate.of(year, 12, 31);

        List<Object[]> queryResults = dailyStatsRepository.sumActivityCalendarByUserId(userId, fromDate, toDate);
        Map<LocalDate, long[]> dayMap = new HashMap<>();

        for (Object[] row : queryResults) {
            LocalDate d = (LocalDate) row[0];
            long timeMs = ((Number) row[1]).longValue();
            int sessions = ((Number) row[2]).intValue();
            long chars = ((Number) row[3]).longValue();
            dayMap.put(d, new long[]{timeMs, sessions, chars});
        }

        List<AnalyticsDtos.ActivityDayDto> calendar = new ArrayList<>();
        LocalDate curr = fromDate;
        while (!curr.isAfter(toDate)) {
            long[] val = dayMap.get(curr);
            long timeMs = val != null ? val[0] : 0L;
            int sessions = val != null ? (int) val[1] : 0;
            long chars = val != null ? val[2] : 0L;

            int level = computeActivityLevel(timeMs);
            calendar.add(new AnalyticsDtos.ActivityDayDto(curr, timeMs, sessions, chars, level));
            curr = curr.plusDays(1);
        }

        return calendar;
    }

    @Transactional(readOnly = true)
    public AnalyticsDtos.RecommendationsResponse getRecommendations(Long userId) {
        String primaryLang = "java"; // default
        var profile = weaknessProfileService.getProfile(userId, primaryLang);
        var weakest = weakestLanguageService.findWeakestLanguage(userId)
                .map(w -> new AnalyticsDtos.WeakestLanguageDto(w.languageSlug(), w.recentWpm(), w.medianWpm(), w.ratio()))
                .orElse(null);

        String recommendedMode = "PRACTICE";
        if (profile.enoughData() && !profile.weakCharacters().isEmpty()) {
            recommendedMode = "SPECIAL_CHARS";
        }

        return new AnalyticsDtos.RecommendationsResponse(
                profile.weakCharacters(),
                profile.weakBigrams(),
                weakest,
                recommendedMode
        );
    }

    private static int computeActivityLevel(long timeMs) {
        if (timeMs <= 0L) return 0;
        if (timeMs < 180_000L) return 1;       // < 3 min
        if (timeMs < 600_000L) return 2;       // 3-10 min
        if (timeMs < 1_800_000L) return 3;     // 10-30 min
        return 4;                              // > 30 min
    }

    private static double computeMedian(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int mid = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(mid);
        } else {
            return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
