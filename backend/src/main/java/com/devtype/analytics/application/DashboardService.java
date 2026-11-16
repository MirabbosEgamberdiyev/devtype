package com.devtype.analytics.application;

import com.devtype.analytics.infrastructure.UserDailyStatsEntity;
import com.devtype.analytics.infrastructure.UserDailyStatsRepository;
import com.devtype.analytics.infrastructure.UserLanguageStatsEntity;
import com.devtype.analytics.infrastructure.UserLanguageStatsRepository;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.gamification.api.GamificationQuery;
import com.devtype.gamification.api.GamificationSummary;
import com.devtype.identity.api.UserDirectory;
import com.devtype.identity.api.UserView;
import com.devtype.training.api.SessionQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class DashboardService {

    private final UserLanguageStatsRepository languageStatsRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final SessionQuery sessionQuery;
    private final LanguageCatalog languageCatalog;
    private final UserDirectory userDirectory;
    private final GamificationQuery gamificationQuery;

    public DashboardService(
            UserLanguageStatsRepository languageStatsRepository,
            UserDailyStatsRepository dailyStatsRepository,
            SessionQuery sessionQuery,
            LanguageCatalog languageCatalog,
            UserDirectory userDirectory,
            GamificationQuery gamificationQuery
    ) {
        this.languageStatsRepository = languageStatsRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.sessionQuery = sessionQuery;
        this.languageCatalog = languageCatalog;
        this.userDirectory = userDirectory;
        this.gamificationQuery = gamificationQuery;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.DashboardResponse getDashboard(Long userId) {
        if (userId == null) {
            return emptyDashboard();
        }

        // 1. User info & Timezone
        UserView user = userDirectory.findById(userId).orElse(null);
        String tzStr = user != null && user.timezone() != null && !user.timezone().isBlank() ? user.timezone() : "UTC";
        ZoneId zone = ZoneId.of(tzStr);
        LocalDate today = LocalDate.now(zone);
        LocalTime now = LocalTime.now(zone);

        // 2. Gamification summary
        GamificationSummary gSummary = gamificationQuery.getSummary(userId);

        // Streak status
        boolean doneToday = false; // Check daily stats or sessions
        int todaySessions = sessionQuery.countCompletedSessionsBetween(
                userId,
                today.atStartOfDay(zone).toInstant(),
                today.plusDays(1).atStartOfDay(zone).toInstant()
        );
        doneToday = todaySessions > 0;
        boolean atRisk = !doneToday && now.getHour() >= 20 && gSummary.streakDays() > 0;

        DashboardDtos.DashboardStreakDto streakDto = new DashboardDtos.DashboardStreakDto(
                gSummary.streakDays(),
                gSummary.streakDays(),
                doneToday,
                atRisk,
                2
        );

        DashboardDtos.DashboardGamificationDto gamificationDto = new DashboardDtos.DashboardGamificationDto(
                gSummary.totalXp(),
                gSummary.level(),
                gSummary.xpIntoLevel(),
                gSummary.xpToNext(),
                streakDto
        );

        // 3. Languages with 14-day sparklines
        List<DashboardDtos.DashboardLanguageDto> langDtos = new ArrayList<>();
        List<String> publishedSlugs = languageCatalog.getPublishedLanguageSlugs();
        LocalDate sparklineStart = today.minusDays(13);

        int totalSessionsAll = 0;

        for (String slug : publishedSlugs) {
            short langId = languageCatalog.findLanguageIdBySlug(slug).orElse((short) -1);
            if (langId <= 0) continue;

            Optional<UserLanguageStatsEntity> coreOpt = languageStatsRepository
                    .findByUserIdAndLanguageIdAndModeGroup(userId, langId, "CORE");

            Double currentWpm = coreOpt.flatMap(c -> Optional.ofNullable(c.getRecentWpm())).map(java.math.BigDecimal::doubleValue).orElse(null);
            int sCnt = coreOpt.map(UserLanguageStatsEntity::getSessionsCnt).orElse(0);
            totalSessionsAll += sCnt;

            // 14-day sparkline
            List<UserDailyStatsEntity> dailyStats = dailyStatsRepository
                    .findByUserIdAndLanguageIdAndDayBetweenOrderByDayAsc(userId, langId, sparklineStart, today);

            Map<LocalDate, Double> dayWpmMap = new HashMap<>();
            for (UserDailyStatsEntity d : dailyStats) {
                if ("CORE".equalsIgnoreCase(d.getModeGroup()) && d.getSessionsCnt() > 0) {
                    double dayWpm = d.getSumWpm().doubleValue() / d.getSessionsCnt();
                    dayWpmMap.put(d.getDay(), Math.round(dayWpm * 10.0) / 10.0);
                }
            }

            List<Double> sparkline = new ArrayList<>();
            LocalDate cursor = sparklineStart;
            while (!cursor.isAfter(today)) {
                sparkline.add(dayWpmMap.get(cursor));
                cursor = cursor.plusDays(1);
            }

            boolean isPrimary = (user != null && slug.equalsIgnoreCase(user.primaryLanguage())) ||
                    (user != null && user.primaryLanguage() == null && "java".equalsIgnoreCase(slug));

            langDtos.add(new DashboardDtos.DashboardLanguageDto(
                    slug,
                    slug.toUpperCase(),
                    isPrimary,
                    currentWpm,
                    currentWpm != null ? (currentWpm >= 80 ? "DIAMOND" : currentWpm >= 60 ? "PLATINUM" : currentWpm >= 45 ? "GOLD" : currentWpm >= 30 ? "SILVER" : "BRONZE") : "UNRANKED",
                    0,
                    sparkline
            ));
        }

        // 4. Recent sessions (last 5 COMPLETED, AC-UD-6)
        var recentSessionsList = sessionQuery.findSessions(userId, null, null, "COMPLETED", null, null, 5);
        List<DashboardDtos.DashboardRecentSessionDto> recentSessionDtos = recentSessionsList.stream()
                .map(s -> new DashboardDtos.DashboardRecentSessionDto(
                        s.id(),
                        s.language(),
                        s.mode(),
                        s.wpm(),
                        s.accuracy(),
                        s.finishedAt(),
                        s.xp()
                ))
                .toList();

        boolean isNewUser = (totalSessionsAll == 0 && recentSessionDtos.isEmpty());

        return new DashboardDtos.DashboardResponse(
                null, // resume
                gamificationDto,
                langDtos,
                recentSessionDtos,
                null, // dailyChallenge
                null, // coachPlan
                isNewUser
        );
    }

    private DashboardDtos.DashboardResponse emptyDashboard() {
        return new DashboardDtos.DashboardResponse(
                null,
                new DashboardDtos.DashboardGamificationDto(0, 1, 0, 100, new DashboardDtos.DashboardStreakDto(0, 0, false, false, 2)),
                List.of(),
                List.of(),
                null,
                null,
                true
        );
    }
}
