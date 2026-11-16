package com.devtype.gamification.application;

import com.devtype.content.api.LanguageCatalog;
import com.devtype.gamification.api.GamificationQuery;
import com.devtype.gamification.api.GamificationSummary;
import com.devtype.gamification.api.SessionRewards;
import com.devtype.gamification.domain.*;
import com.devtype.gamification.infrastructure.*;
import com.devtype.identity.api.UserDirectory;
import com.devtype.identity.api.UserView;
import com.devtype.shared.Constants;
import com.devtype.training.api.SessionCompleted;
import com.devtype.training.api.SessionQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class GamificationService implements GamificationQuery {

    private static final Logger log = LoggerFactory.getLogger(GamificationService.class);

    private final UserProgressRepository userProgressRepository;
    private final XpTransactionRepository xpTransactionRepository;
    private final StreakFreezeUsageRepository streakFreezeUsageRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final SessionQuery sessionQuery;
    private final LanguageCatalog languageCatalog;
    private final UserDirectory userDirectory;
    private final ObjectMapper objectMapper;

    public GamificationService(
            UserProgressRepository userProgressRepository,
            XpTransactionRepository xpTransactionRepository,
            StreakFreezeUsageRepository streakFreezeUsageRepository,
            PersonalRecordRepository personalRecordRepository,
            SessionQuery sessionQuery,
            LanguageCatalog languageCatalog,
            UserDirectory userDirectory,
            ObjectMapper objectMapper
    ) {
        this.userProgressRepository = userProgressRepository;
        this.xpTransactionRepository = xpTransactionRepository;
        this.streakFreezeUsageRepository = streakFreezeUsageRepository;
        this.personalRecordRepository = personalRecordRepository;
        this.sessionQuery = sessionQuery;
        this.languageCatalog = languageCatalog;
        this.userDirectory = userDirectory;
        this.objectMapper = objectMapper;
    }

    @EventListener
    @Transactional
    public void onSessionCompleted(SessionCompleted event) {
        if (event.userId() == null) {
            return; // Guest sessions do not earn XP until claimed
        }

        Long userId = event.userId();
        String sessionIdStr = event.sessionId().toString();

        // Idempotency: don't process same session twice
        if (xpTransactionRepository.findByUserIdAndSourceTypeAndSourceId(userId, "SESSION", sessionIdStr).isPresent()) {
            log.info("Session {} already processed for XP, skipping", event.sessionId());
            return;
        }

        // Determine user timezone and local day
        String tzStr = userDirectory.findById(userId)
                .map(UserView::timezone)
                .filter(tz -> tz != null && !tz.isBlank())
                .orElse("UTC");
        ZoneId zone = ZoneId.of(tzStr);
        LocalDate today = event.timestamp().atZone(zone).toLocalDate();
        Instant dayStart = today.atStartOfDay(zone).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant();

        // Lock user progress row
        UserProgressEntity progress = userProgressRepository.findByIdForUpdate(userId)
                .orElseGet(() -> {
                    UserProgressEntity p = new UserProgressEntity(userId);
                    return userProgressRepository.save(p);
                });

        // Reset xpToday if new day
        if (progress.getXpTodayDay() == null || !progress.getXpTodayDay().equals(today)) {
            progress.setXpToday(0);
            progress.setXpTodayDay(today);
        }

        // 1. Streak update (docs/11 §4)
        boolean streakExtendedToday = false;
        long activeTodayMs = sessionQuery.sumActiveMsBetween(userId, dayStart, dayEnd);
        if (activeTodayMs >= Constants.STREAK_MIN_ACTIVE_SECONDS * 1000L) {
            if (progress.getLastActiveDay() == null) {
                progress.setCurrentStreak(1);
                progress.setStreakStartDay(today);
                progress.setLastActiveDay(today);
                progress.setLongestStreak(Math.max(progress.getLongestStreak(), 1));
                streakExtendedToday = true;
            } else if (!progress.getLastActiveDay().equals(today)) {
                long gap = ChronoUnit.DAYS.between(progress.getLastActiveDay(), today);
                if (gap == 1) {
                    progress.setCurrentStreak(progress.getCurrentStreak() + 1);
                    streakExtendedToday = true;
                } else if (gap > 1 && (gap - 1) <= progress.getFreezesLeft()) {
                    int used = (int) (gap - 1);
                    progress.setFreezesLeft((short) (progress.getFreezesLeft() - used));
                    progress.setCurrentStreak(progress.getCurrentStreak() + 1);
                    for (int d = 1; d <= used; d++) {
                        LocalDate freezeDay = progress.getLastActiveDay().plusDays(d);
                        streakFreezeUsageRepository.save(new StreakFreezeUsageEntity(userId, freezeDay));
                    }
                    streakExtendedToday = true;
                } else {
                    progress.setCurrentStreak(1);
                    progress.setStreakStartDay(today);
                    streakExtendedToday = true;
                }
                progress.setLastActiveDay(today);
                progress.setLongestStreak(Math.max(progress.getLongestStreak(), progress.getCurrentStreak()));

                // Check milestone bonuses (7, 30, 100 days)
                int streak = progress.getCurrentStreak();
                if (streak == 7 || streak == 30 || streak == 100) {
                    int milestoneXp = (streak == 7) ? 50 : (streak == 30 ? 300 : 1500);
                    String streakSourceId = "streak:" + streak + ":" + progress.getStreakStartDay();
                    if (xpTransactionRepository.findByUserIdAndSourceTypeAndSourceId(userId, "STREAK", streakSourceId).isEmpty()) {
                        xpTransactionRepository.save(new XpTransactionEntity(
                                userId,
                                milestoneXp,
                                "STREAK",
                                streakSourceId,
                                "{\"milestone\":" + streak + "}"
                        ));
                        progress.setTotalXp(progress.getTotalXp() + milestoneXp);
                    }
                }
            }
        }

        // 2. Personal records (docs/11 §5)
        short langId = languageCatalog.findLanguageIdBySlug(event.languageSlug()).orElse((short) 1);
        short durationLimitS = 0;
        boolean recordEligible = isRecordEligible(event);
        boolean isRecordBroken = false;
        GamificationDtos.RecordDto recordDto = null;

        if (recordEligible) {
            Optional<PersonalRecordEntity> recordOpt = personalRecordRepository
                    .findByUserIdAndLanguageIdAndModeAndDurationLimitS(userId, langId, event.mode(), durationLimitS);

            if (recordOpt.isPresent()) {
                PersonalRecordEntity rec = recordOpt.get();
                if (event.wpm() > rec.getBestWpm().doubleValue()) {
                    recordDto = new GamificationDtos.RecordDto(rec.getBestWpm().doubleValue(), event.wpm());
                    rec.setBestWpm(BigDecimal.valueOf(event.wpm()));
                    rec.setAccuracy(BigDecimal.valueOf(event.accuracy()));
                    rec.setSessionId(event.sessionId());
                    rec.setAchievedAt(event.timestamp());
                    personalRecordRepository.save(rec);
                    isRecordBroken = true;
                }
            } else {
                PersonalRecordEntity rec = new PersonalRecordEntity(
                        userId,
                        langId,
                        event.mode(),
                        durationLimitS,
                        BigDecimal.valueOf(event.wpm()),
                        BigDecimal.valueOf(event.accuracy()),
                        event.sessionId(),
                        event.timestamp()
                );
                personalRecordRepository.save(rec);
                recordDto = new GamificationDtos.RecordDto(null, event.wpm());
                isRecordBroken = false; // First record does not get bonus
            }
        }

        // 3. XP calculation (docs/11 §1, docs/00 §4.7)
        int totalCorrectChars = (event.exercises() != null)
                ? event.exercises().stream().mapToInt(SessionCompleted.ExerciseSummary::charCount).sum()
                : 0;

        List<XpCalculator.SnippetWeight> snippetWeights = (event.exercises() != null)
                ? event.exercises().stream()
                .map(e -> new XpCalculator.SnippetWeight(e.level(), e.charCount()))
                .toList()
                : List.of();

        // Repeat factor in 24h
        int repeatCount = 0;
        if (event.exercises() != null && !event.exercises().isEmpty()) {
            String exKeyStr = event.exercises().get(0).exerciseKey();
            try {
                Long exId = Long.parseLong(exKeyStr);
                Instant last24h = event.timestamp().minus(24, ChronoUnit.HOURS);
                repeatCount = Math.max(0, sessionQuery.countCompletedForExerciseSince(userId, exId, last24h) - 1);
            } catch (NumberFormatException ignored) {}
        }

        // First of day bonus
        boolean isFirstOfDay = sessionQuery.countCompletedSessionsBetween(userId, dayStart, dayEnd) <= 1;
        boolean isSprintComplete = "CODE_SPRINT".equalsIgnoreCase(event.mode()) && event.exercises() != null && event.exercises().size() >= 5;

        var xpCalc = XpCalculator.calculate(
                totalCorrectChars,
                event.mode(),
                snippetWeights,
                event.accuracy(),
                100.0,
                repeatCount,
                isRecordBroken,
                isFirstOfDay,
                isSprintComplete,
                progress.getXpToday()
        );

        int earnedXp = xpCalc.earnedXp();
        String detailsJson = "{}";
        try {
            detailsJson = objectMapper.writeValueAsString(xpCalc.breakdown());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize XP breakdown: {}", e.getMessage());
        }

        xpTransactionRepository.save(new XpTransactionEntity(
                userId,
                earnedXp,
                "SESSION",
                sessionIdStr,
                detailsJson
        ));

        // Update progress XP and Level
        progress.setXpToday(progress.getXpToday() + earnedXp);
        progress.setTotalXp(progress.getTotalXp() + earnedXp);

        int oldLevel = progress.getLevel();
        int newLevel = LevelCalculator.calculateLevel(progress.getTotalXp());
        Integer levelUp = (newLevel > oldLevel) ? newLevel : null;
        progress.setLevel(newLevel);
        userProgressRepository.save(progress);

        // 4. Language rank evaluation
        GamificationDtos.RankChangeDto rankChange = null;
        if (isCoreMode(event.mode())) {
            List<Double> recentWpms = sessionQuery.findRecentCoreWpms(userId, langId, 10);
            if (recentWpms.size() >= RankCalculator.MIN_SESSIONS_FOR_RANK) {
                double medianWpm = computeMedian(recentWpms);
                // Currently stored rank can be calculated against previous
                var rankEval = RankCalculator.evaluateRank(recentWpms.size(), medianWpm, LanguageRank.UNRANKED);
                if (rankEval.changed()) {
                    rankChange = new GamificationDtos.RankChangeDto(
                            rankEval.previousRank() != null ? rankEval.previousRank().name() : "UNRANKED",
                            rankEval.rank().name()
                    );
                }
            }
        }

        // 5. Build DTOs and populate collector
        GamificationDtos.RewardsDto rewardsDto = new GamificationDtos.RewardsDto(
                earnedXp,
                xpCalc.breakdown(),
                progress.getTotalXp(),
                progress.getLevel(),
                levelUp,
                new GamificationDtos.StreakDto(progress.getCurrentStreak(), streakExtendedToday),
                recordDto,
                rankChange
        );

        if (event.collector() != null) {
            event.collector().set("rewards", rewardsDto);
            event.collector().set("earnedXp", earnedXp);
            event.collector().set("gamificationSummary", new SessionRewards(
                    earnedXp,
                    (int) progress.getTotalXp(),
                    progress.getLevel(),
                    levelUp != null,
                    progress.getCurrentStreak(),
                    streakExtendedToday,
                    isRecordBroken,
                    List.of()
            ));
        }
    }

    @Transactional(readOnly = true)
    public GamificationDtos.GamificationMeResponse getGamificationMe(Long userId) {
        UserProgressEntity p = userProgressRepository.findById(userId)
                .orElseGet(() -> new UserProgressEntity(userId));

        var levelInfo = LevelCalculator.getLevelInfo(p.getTotalXp());

        // Check if streak is at risk (after 20:00 local time, not completed today)
        String tzStr = userDirectory.findById(userId).map(UserView::timezone).orElse("UTC");
        ZoneId zone = ZoneId.of(tzStr != null && !tzStr.isBlank() ? tzStr : "UTC");
        LocalDate today = LocalDate.now(zone);
        LocalTime nowTime = LocalTime.now(zone);
        boolean doneToday = today.equals(p.getLastActiveDay());
        boolean atRisk = !doneToday && nowTime.getHour() >= 20 && p.getCurrentStreak() > 0;

        GamificationDtos.StreakStatusDto streakDto = new GamificationDtos.StreakStatusDto(
                p.getCurrentStreak(),
                p.getLongestStreak(),
                doneToday,
                atRisk,
                p.getFreezesLeft()
        );

        // Language ranks
        List<GamificationDtos.LanguageRankDto> ranks = new ArrayList<>();
        for (String slug : languageCatalog.getPublishedLanguageSlugs()) {
            short langId = languageCatalog.findLanguageIdBySlug(slug).orElse((short) -1);
            if (langId > 0) {
                List<Double> recentWpms = sessionQuery.findRecentCoreWpms(userId, langId, 10);
                if (recentWpms.size() >= RankCalculator.MIN_SESSIONS_FOR_RANK) {
                    double med = computeMedian(recentWpms);
                    LanguageRank r = RankCalculator.calculateRawRank(med);
                    ranks.add(new GamificationDtos.LanguageRankDto(slug, r.name(), med));
                } else if (!recentWpms.isEmpty()) {
                    double med = computeMedian(recentWpms);
                    ranks.add(new GamificationDtos.LanguageRankDto(slug, "UNRANKED", med));
                }
            }
        }

        return new GamificationDtos.GamificationMeResponse(
                p.getTotalXp(),
                p.getLevel(),
                levelInfo.xpIntoLevel(),
                levelInfo.xpToNext(),
                streakDto,
                ranks
        );
    }

    @Transactional(readOnly = true)
    public List<GamificationDtos.XpTransactionDto> getXpHistory(Long userId, int limit) {
        int maxLimit = Math.min(Math.max(1, limit), 100);
        var pageable = org.springframework.data.domain.PageRequest.of(0, maxLimit);
        return xpTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).stream()
                .map(t -> new GamificationDtos.XpTransactionDto(
                        t.getId(),
                        t.getAmount(),
                        t.getSourceType(),
                        t.getSourceId(),
                        t.getDetails(),
                        t.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GamificationDtos.PersonalRecordItemDto> getPersonalRecords(Long userId) {
        return personalRecordRepository.findByUserId(userId).stream()
                .map(r -> {
                    String slug = languageCatalog.getPublishedLanguageSlugs().stream()
                            .filter(s -> languageCatalog.findLanguageIdBySlug(s).orElse((short) -1) == r.getLanguageId())
                            .findFirst()
                            .orElse("unknown");
                    return new GamificationDtos.PersonalRecordItemDto(
                            slug,
                            r.getMode(),
                            r.getDurationLimitS(),
                            r.getBestWpm().doubleValue(),
                            r.getAccuracy().doubleValue(),
                            r.getSessionId(),
                            r.getAchievedAt()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GamificationSummary getSummary(Long userId) {
        if (userId == null) {
            return new GamificationSummary(0L, 0L, 1, 0L, 100, 0, "UNRANKED");
        }
        UserProgressEntity p = userProgressRepository.findById(userId)
                .orElseGet(() -> new UserProgressEntity(userId));
        var levelInfo = LevelCalculator.getLevelInfo(p.getTotalXp());
        return new GamificationSummary(
                userId,
                p.getTotalXp(),
                p.getLevel(),
                levelInfo.xpIntoLevel(),
                levelInfo.xpToNext(),
                p.getCurrentStreak(),
                "UNRANKED"
        );
    }

    private boolean isRecordEligible(SessionCompleted s) {
        if (s.accuracy() < Constants.COMPLETION_ACCURACY_THRESHOLD) {
            return false;
        }
        String mode = s.mode() != null ? s.mode().toUpperCase() : "";
        return switch (mode) {
            case "SPEED_TEST", "PRACTICE", "CHALLENGE", "CODE_SPRINT" -> true;
            default -> false;
        };
    }

    private boolean isCoreMode(String mode) {
        if (mode == null) return false;
        return switch (mode.toUpperCase()) {
            case "PRACTICE", "SPEED_TEST", "ACCURACY", "CHALLENGE", "CODE_SPRINT" -> true;
            default -> false;
        };
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
