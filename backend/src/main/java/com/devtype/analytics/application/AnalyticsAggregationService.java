package com.devtype.analytics.application;

import com.devtype.analytics.domain.ModeGroup;
import com.devtype.analytics.infrastructure.*;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.identity.api.UserDirectory;
import com.devtype.identity.api.UserView;
import com.devtype.training.api.SessionCompleted;
import com.devtype.training.api.SessionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AnalyticsAggregationService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsAggregationService.class);

    private final ProcessedSessionRepository processedSessionRepository;
    private final UserLanguageStatsRepository languageStatsRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final UserCharStatsRepository charStatsRepository;
    private final UserBigramStatsRepository bigramStatsRepository;
    private final SessionQuery sessionQuery;
    private final LanguageCatalog languageCatalog;
    private final UserDirectory userDirectory;

    public AnalyticsAggregationService(
            ProcessedSessionRepository processedSessionRepository,
            UserLanguageStatsRepository languageStatsRepository,
            UserDailyStatsRepository dailyStatsRepository,
            UserCharStatsRepository charStatsRepository,
            UserBigramStatsRepository bigramStatsRepository,
            SessionQuery sessionQuery,
            LanguageCatalog languageCatalog,
            UserDirectory userDirectory
    ) {
        this.processedSessionRepository = processedSessionRepository;
        this.languageStatsRepository = languageStatsRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.charStatsRepository = charStatsRepository;
        this.bigramStatsRepository = bigramStatsRepository;
        this.sessionQuery = sessionQuery;
        this.languageCatalog = languageCatalog;
        this.userDirectory = userDirectory;
    }

    @EventListener
    @Transactional
    public void onSessionCompleted(SessionCompleted event) {
        if (event.userId() == null) {
            return;
        }

        // Idempotency check (BL-AN-1, docs/10 §A.2)
        if (processedSessionRepository.existsById(event.sessionId())) {
            log.info("Analytics: session {} already processed, skipping", event.sessionId());
            return;
        }

        Long userId = event.userId();
        short languageId = languageCatalog.findLanguageIdBySlug(event.languageSlug()).orElse((short) 1);
        String modeGroup = ModeGroup.fromMode(event.mode()).name();

        // 1. User Language Stats update
        UserLanguageStatsEntity langStats = languageStatsRepository
                .findByUserIdAndLanguageIdAndModeGroup(userId, languageId, modeGroup)
                .orElseGet(() -> new UserLanguageStatsEntity(userId, languageId, modeGroup));

        int sessionChars = (event.exercises() != null)
                ? event.exercises().stream().mapToInt(SessionCompleted.ExerciseSummary::charCount).sum()
                : 0;

        langStats.setSessionsCnt(langStats.getSessionsCnt() + 1);
        langStats.setTotalTimeMs(langStats.getTotalTimeMs() + event.activeMs());
        langStats.setTotalChars(langStats.getTotalChars() + sessionChars);
        langStats.setSumWpm(langStats.getSumWpm().add(BigDecimal.valueOf(event.wpm())));
        langStats.setSumCpm(langStats.getSumCpm().add(BigDecimal.valueOf(event.rawWpm() * 5.0)));

        if (event.accuracy() >= 95.0 && BigDecimal.valueOf(event.wpm()).compareTo(langStats.getMaxWpm()) > 0) {
            langStats.setMaxWpm(BigDecimal.valueOf(event.wpm()));
        }

        langStats.setCorrectKeys(langStats.getCorrectKeys() + sessionChars);
        long totalKeys = sessionChars;
        if (event.accuracy() > 0 && event.accuracy() < 100.0) {
            totalKeys = (long) Math.round(sessionChars / (event.accuracy() / 100.0));
        }
        langStats.setTotalKeys(langStats.getTotalKeys() + totalKeys);
        langStats.setErrorCnt(langStats.getErrorCnt() + Math.max(0, totalKeys - sessionChars));

        // Recent WPM (median of last 10 CORE sessions, null if < 3)
        List<Double> recentWpms = sessionQuery.findRecentCoreWpms(userId, languageId, 10);
        if (recentWpms.size() >= 3) {
            double median = computeMedian(recentWpms);
            langStats.setRecentWpm(BigDecimal.valueOf(Math.round(median * 100.0) / 100.0));
        }
        langStats.setUpdatedAt(Instant.now());
        languageStatsRepository.save(langStats);

        // 2. User Daily Stats update (timezone-aware)
        String tzStr = userDirectory.findById(userId)
                .map(UserView::timezone)
                .filter(tz -> tz != null && !tz.isBlank())
                .orElse("UTC");
        ZoneId zone = ZoneId.of(tzStr);
        LocalDate day = event.timestamp().atZone(zone).toLocalDate();

        UserDailyStatsEntity dailyStats = dailyStatsRepository
                .findByUserIdAndDayAndLanguageIdAndModeGroup(userId, day, languageId, modeGroup)
                .orElseGet(() -> new UserDailyStatsEntity(userId, day, languageId, modeGroup));

        dailyStats.setSessionsCnt(dailyStats.getSessionsCnt() + 1);
        dailyStats.setTimeMs(dailyStats.getTimeMs() + event.activeMs());
        dailyStats.setChars(dailyStats.getChars() + sessionChars);
        dailyStats.setSumWpm(dailyStats.getSumWpm().add(BigDecimal.valueOf(event.wpm())));

        if (event.accuracy() >= 95.0 && BigDecimal.valueOf(event.wpm()).compareTo(dailyStats.getMaxWpm()) > 0) {
            dailyStats.setMaxWpm(BigDecimal.valueOf(event.wpm()));
        }

        dailyStats.setCorrectKeys(dailyStats.getCorrectKeys() + sessionChars);
        dailyStats.setTotalKeys(dailyStats.getTotalKeys() + totalKeys);

        // Extract XP earned from rewards in collector if present
        if (event.collector() != null) {
            Object xpVal = event.collector().get("earnedXp");
            if (xpVal instanceof Integer xpInt) {
                dailyStats.setXp(dailyStats.getXp() + xpInt);
            }
        }
        dailyStatsRepository.save(dailyStats);

        // Mark processed for idempotency
        processedSessionRepository.save(new ProcessedSessionEntity(event.sessionId()));
    }

    private double computeMedian(List<Double> values) {
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
}
