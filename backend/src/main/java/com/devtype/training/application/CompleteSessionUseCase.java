package com.devtype.training.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.shared.Constants;
import com.devtype.shared.error.DomainException;
import com.devtype.training.api.SessionCompleted;
import com.devtype.training.api.SessionOutcomeCollector;
import com.devtype.training.domain.*;
import com.devtype.training.infrastructure.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CompleteSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionExerciseRepository sessionExerciseRepository;
    private final SessionKeylogRepository sessionKeylogRepository;
    private final SessionCharStatsRepository sessionCharStatsRepository;
    private final CompleteTokenService completeTokenService;
    private final ExerciseCatalog exerciseCatalog;
    private final LanguageCatalog languageCatalog;
    private final TopicProgressService topicProgressService;
    private final ApplicationEventPublisher eventPublisher;

    public CompleteSessionUseCase(
            SessionRepository sessionRepository,
            SessionExerciseRepository sessionExerciseRepository,
            SessionKeylogRepository sessionKeylogRepository,
            SessionCharStatsRepository sessionCharStatsRepository,
            CompleteTokenService completeTokenService,
            ExerciseCatalog exerciseCatalog,
            LanguageCatalog languageCatalog,
            TopicProgressService topicProgressService,
            ApplicationEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.sessionKeylogRepository = sessionKeylogRepository;
        this.sessionCharStatsRepository = sessionCharStatsRepository;
        this.completeTokenService = completeTokenService;
        this.exerciseCatalog = exerciseCatalog;
        this.languageCatalog = languageCatalog;
        this.topicProgressService = topicProgressService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CompleteSessionResult execute(CompleteSessionCommand cmd) {
        SessionEntity session = sessionRepository.findFirstById(cmd.sessionId())
                .orElseThrow(() -> new DomainException("SESSION_NOT_FOUND", "Sessiya topilmadi: " + cmd.sessionId(), HttpStatus.NOT_FOUND));

        // Foydalanuvchi tekshiruvi (AC-TM-8)
        if (session.getUserId() != null && cmd.currentUserId() != null && !session.getUserId().equals(cmd.currentUserId())) {
            throw new DomainException("FORBIDDEN", "Sessiya boshqa foydalanuvchiga tegishli", HttpStatus.FORBIDDEN);
        }

        Instant expiresAt = session.getStartedAt().plus(Constants.SESSION_EXPIRATION_HOURS, ChronoUnit.HOURS);

        // 1. completeToken tekshiruvi (AC-TM-8)
        completeTokenService.verifyToken(session.getId(), cmd.completeToken(), session.getStartedAt(), expiresAt);

        // 2. Status tekshiruvi (idempotentlik - BL-TM-2, AC-TM-11)
        if ("COMPLETED".equals(session.getStatus())) {
            return buildExistingResult(session);
        }
        if ("ABANDONED".equals(session.getStatus())) {
            throw new DomainException("SESSION_NOT_ACTIVE", "Sessiya faol emas", HttpStatus.CONFLICT);
        }
        if ("REJECTED".equals(session.getStatus())) {
            throw new DomainException(session.getRejectReason() != null ? session.getRejectReason() : "KEYLOG_INVALID",
                    "Sessiya rad etilgan", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 3. Muddati o'tganligini tekshirish
        if (Instant.now().isAfter(expiresAt)) {
            session.setStatus("ABANDONED");
            session.setRejectReason("SESSION_EXPIRED");
            sessionRepository.save(session);
            throw new DomainException("SESSION_EXPIRED", "Sessiya muddati tugagan (24 soat)", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 4. Mashq kodlarini olish
        List<SessionExerciseEntity> exercises = sessionExerciseRepository.findBySessionIdOrderBySeqAsc(session.getId());
        if (exercises.isEmpty()) {
            throw new DomainException("SESSION_CORRUPTED", "Sessiyada mashq mavjud emas", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<String> targetCodes = new ArrayList<>();
        for (SessionExerciseEntity se : exercises) {
            if (se.getGeneratedCode() != null && !se.getGeneratedCode().isBlank()) {
                targetCodes.add(se.getGeneratedCode());
            } else if (se.getExerciseId() != null) {
                Optional<ExerciseView> ev = exerciseCatalog.findById(se.getExerciseId());
                if (ev.isEmpty()) {
                    throw new DomainException("EXERCISE_NOT_FOUND", "Mashq topilmadi: " + se.getExerciseId(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                targetCodes.add(ev.get().code());
            }
        }

        // 5. Replay config va ijro
        EngineMode engineMode;
        try {
            engineMode = EngineMode.valueOf(session.getMode());
        } catch (IllegalArgumentException e) {
            engineMode = EngineMode.PRACTICE;
        }

        KeylogReplayer.ReplaySessionConfig replayConfig = new KeylogReplayer.ReplaySessionConfig(
                engineMode,
                ErrorMode.STOP,
                session.getDurationLimitS() != null ? session.getDurationLimitS().intValue() : null,
                session.isStrictIndent()
        );

        String langSlug = languageCatalog.getPublishedLanguageSlugs().stream()
                .filter(slug -> languageCatalog.findLanguageIdBySlug(slug).orElse((short) -1) == session.getLanguageId())
                .findFirst()
                .orElse("java");

        LanguageConfig langConfig = resolveLanguageConfig(langSlug);

        ReplayResult replayResult;
        try {
            replayResult = KeylogReplayer.replay(
                    replayConfig,
                    targetCodes,
                    langConfig,
                    cmd.keylog(),
                    SuspicionRules.SuspicionContext.empty()
            );
        } catch (Exception e) {
            session.setStatus("REJECTED");
            session.setRejectReason("KEYLOG_INVALID");
            sessionRepository.save(session);
            throw new DomainException("KEYLOG_INVALID", "Replay xatosi: " + e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        EngineMetrics m = replayResult.metrics();

        // 6. Minimal sessiya tekshiruvi (BL-TM-3, AC-TM-11)
        int totalKeystrokes = m.correctKeys() + m.incorrectKeys();
        if (totalKeystrokes < 20 || m.elapsedMs() < 5000) {
            session.setStatus("ABANDONED");
            session.setRejectReason("SESSION_TOO_SHORT");
            sessionRepository.save(session);
            throw new DomainException("SESSION_TOO_SHORT", "Sessiya juda qisqa (kamida 20 klavish va 5 s)", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 7. Timing tekshiruvi (Constants.TIMING_TOLERANCE_MS = 2000)
        long timeDiff = Math.abs((long) m.elapsedMs() - cmd.clientElapsedMs());
        if (timeDiff > Constants.TIMING_TOLERANCE_MS) {
            session.setStatus("REJECTED");
            session.setRejectReason("TIMING_MISMATCH");
            sessionRepository.save(session);
            throw new DomainException("TIMING_MISMATCH", "Klient va server vaqtlari mos kelmadi", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 8. Shubha va reyting tekshiruvi (BL-TM-2, BL-TM-7)
        boolean isFlagged = replayResult.suspicion().score() >= 50;
        if (isFlagged) {
            session.setReviewStatus("PENDING");
            session.setRanked(false);
            session.setRankedReason("SUSPICIOUS_RHYTHM");
        } else {
            if ("SPEED_TEST".equals(session.getMode()) && session.getUserId() != null) {
                session.setRanked(true);
                session.setRankedReason(null);
            } else {
                session.setRanked(false);
                session.setRankedReason("NOT_RANKED_MODE");
            }
        }

        // 9. Sessiyani yangilash
        session.setStatus("COMPLETED");
        session.setFinishedAt(Instant.now());
        session.setElapsedMs(m.elapsedMs());
        session.setWpm(m.wpm());
        session.setRawWpm(m.rawWpm());
        session.setCpm(m.cpm());
        session.setAccuracy(m.accuracy());
        session.setConsistency(m.consistency());
        session.setCorrectChars(m.correctChars());
        session.setUncorrectedErrors(m.uncorrectedErrors());
        session.setCorrectKeys(m.correctKeys());
        session.setIncorrectKeys(m.incorrectKeys());
        session.setTotalTyped(m.totalTyped());
        session.setBackspaceCount(m.backspaceCount());
        session.setCompletionPct(m.completionPct());
        session.setClientWpm(cmd.clientWpm() != null ? BigDecimal.valueOf(cmd.clientWpm()) : null);
        session.setClientAccuracy(cmd.clientAccuracy() != null ? BigDecimal.valueOf(cmd.clientAccuracy()) : null);
        session.setSuspicionScore((short) replayResult.suspicion().score());
        session.setSuspicionReasons(replayResult.suspicion().reasons().toArray(new String[0]));

        sessionRepository.save(session);

        // 10. Keylog va Char Stats saqlash
        saveKeylogAndStats(session, cmd.keylog(), replayResult);

        // 11. Exercise best va Topic progress yangilash (BL-LS-1)
        if (session.getUserId() != null) {
            for (SessionExerciseEntity se : exercises) {
                se.setTypedChars(m.totalTyped());
                se.setCorrectChars(m.correctChars());
                se.setAccuracy(m.accuracy());
                se.setCompleted(m.accuracy().doubleValue() >= 95.0 && m.completionPct().doubleValue() >= 100.0);
                sessionExerciseRepository.save(se);

                if (se.getExerciseId() != null) {
                    topicProgressService.onSessionExerciseCompleted(
                            session.getUserId(),
                            langSlug,
                            se.getExerciseId(),
                            m.wpm().doubleValue(),
                            m.accuracy().doubleValue(),
                            session.getId()
                    );
                }
            }

            // Placement testi tekshiruvi (BL-LS-3)
            if (session.isPlacement() && exercises.size() >= 3) {
                boolean passedB = m.accuracy().doubleValue() >= 95.0 && m.completionPct().doubleValue() >= 100.0;
                topicProgressService.applyPlacementResult(
                        session.getUserId(),
                        session.getLanguageId(),
                        langSlug,
                        passedB,
                        passedB, // Sodda qilib placement testini o'tkazish
                        false
                );
            }
        }

        // 12. SessionCompleted event chiqarish
        SessionOutcomeCollector collector = new SessionOutcomeCollector();
        List<SessionCompleted.ExerciseSummary> exSummaries = exercises.stream()
                .map(se -> new SessionCompleted.ExerciseSummary(
                        se.getExerciseId() != null ? se.getExerciseId().toString() : "gen",
                        session.getDifficulty() != null ? session.getDifficulty() : "BEGINNER",
                        session.getTopicId() != null ? session.getTopicId().toString() : null,
                        se.getCorrectChars() != null ? se.getCorrectChars() : m.correctChars()
                )).toList();

        eventPublisher.publishEvent(new SessionCompleted(
                session.getId(),
                session.getUserId(),
                langSlug,
                session.getMode(),
                m.wpm().doubleValue(),
                m.rawWpm().doubleValue(),
                m.accuracy().doubleValue(),
                m.consistency().doubleValue(),
                m.elapsedMs(),
                session.isRanked(),
                session.getUserId() != null,
                exSummaries,
                collector,
                Instant.now()
        ));

        // 13. Natija DTO tayyorlash
        CompleteSessionResult.MetricsDto metricsDto = new CompleteSessionResult.MetricsDto(
                m.wpm().doubleValue(),
                m.rawWpm().doubleValue(),
                m.cpm().doubleValue(),
                m.accuracy().doubleValue(),
                m.consistency().doubleValue(),
                m.incorrectKeys(),
                m.uncorrectedErrors(),
                m.backspaceCount(),
                m.correctChars(),
                m.elapsedMs(),
                m.completionPct().doubleValue(),
                100.00
        );

        List<CompleteSessionResult.WpmPointDto> wpmSeries = generateWpmSeries(m);
        List<CompleteSessionResult.WeakTokenDto> weakTokens = extractWeakTokens(replayResult);
        List<CompleteSessionResult.SlowLineDto> slowLines = List.of();

        Object rewards = collector.get("rewards");
        Object progress = collector.get("progress");
        Object coachTip = collector.get("coachTip");

        return new CompleteSessionResult(
                session.getId(),
                "COMPLETED",
                session.isRanked(),
                session.getRankedReason(),
                metricsDto,
                wpmSeries,
                weakTokens,
                slowLines,
                rewards,
                progress,
                coachTip,
                null
        );
    }

    private void saveKeylogAndStats(SessionEntity session, String keylogBase64, ReplayResult replayResult) {
        try {
            byte[] rawBytes = Base64.getDecoder().decode(keylogBase64);
            SessionKeylogEntity keylogEntity = new SessionKeylogEntity();
            keylogEntity.setSessionId(session.getId());
            keylogEntity.setStartedAt(session.getStartedAt());
            keylogEntity.setEvents(rawBytes);
            keylogEntity.setEventCount(replayResult.metrics().totalTyped());
            sessionKeylogRepository.save(keylogEntity);
        } catch (Exception ignored) {}

        if (replayResult.charStats() != null) {
            List<SessionCharStatsEntity> charStatsList = new ArrayList<>();
            replayResult.charStats().forEach((token, stat) -> {
                SessionCharStatsEntity cse = new SessionCharStatsEntity();
                cse.setSessionId(session.getId());
                cse.setStartedAt(session.getStartedAt());
                cse.setToken(token);
                cse.setExpectedCnt(stat.expected());
                cse.setErrorCnt(stat.errors());
                cse.setTotalMs((int) Math.min(Integer.MAX_VALUE, stat.totalMs()));
                cse.setTimedCnt(stat.expected());
                charStatsList.add(cse);
            });
            sessionCharStatsRepository.saveAll(charStatsList);
        }
    }

    private List<CompleteSessionResult.WpmPointDto> generateWpmSeries(EngineMetrics m) {
        List<CompleteSessionResult.WpmPointDto> series = new ArrayList<>();
        int durationSec = Math.max(1, m.elapsedMs() / 1000);
        int step = Math.max(5, durationSec / 12);
        for (int t = step; t <= durationSec; t += step) {
            series.add(new CompleteSessionResult.WpmPointDto(
                    t,
                    m.wpm().doubleValue(),
                    m.rawWpm().doubleValue(),
                    0
            ));
        }
        if (series.isEmpty() || series.get(series.size() - 1).t() < durationSec) {
            series.add(new CompleteSessionResult.WpmPointDto(
                    durationSec,
                    m.wpm().doubleValue(),
                    m.rawWpm().doubleValue(),
                    m.incorrectKeys()
            ));
        }
        return series;
    }

    private List<CompleteSessionResult.WeakTokenDto> extractWeakTokens(ReplayResult replayResult) {
        if (replayResult.charStats() == null || replayResult.charStats().isEmpty()) {
            return Collections.emptyList();
        }
        List<CompleteSessionResult.WeakTokenDto> list = new ArrayList<>();
        replayResult.charStats().forEach((token, stat) -> {
            if (stat.errors() > 0) {
                int avg = stat.expected() > 0 ? (int) (stat.totalMs() / stat.expected()) : 0;
                list.add(new CompleteSessionResult.WeakTokenDto(token, stat.errors(), stat.expected(), avg));
            }
        });
        list.sort((a, b) -> Integer.compare(b.errors(), a.errors()));
        return list.stream().limit(5).toList();
    }

    private CompleteSessionResult buildExistingResult(SessionEntity session) {
        CompleteSessionResult.MetricsDto metricsDto = new CompleteSessionResult.MetricsDto(
                session.getWpm() != null ? session.getWpm().doubleValue() : 0.0,
                session.getRawWpm() != null ? session.getRawWpm().doubleValue() : 0.0,
                session.getCpm() != null ? session.getCpm().doubleValue() : 0.0,
                session.getAccuracy() != null ? session.getAccuracy().doubleValue() : 0.0,
                session.getConsistency() != null ? session.getConsistency().doubleValue() : 0.0,
                session.getIncorrectKeys() != null ? session.getIncorrectKeys() : 0,
                session.getUncorrectedErrors() != null ? session.getUncorrectedErrors() : 0,
                session.getBackspaceCount() != null ? session.getBackspaceCount() : 0,
                session.getCorrectChars() != null ? session.getCorrectChars() : 0,
                session.getElapsedMs() != null ? session.getElapsedMs() : 0,
                session.getCompletionPct() != null ? session.getCompletionPct().doubleValue() : 100.0,
                100.00
        );

        return new CompleteSessionResult(
                session.getId(),
                session.getStatus(),
                session.isRanked(),
                session.getRankedReason(),
                metricsDto,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                null,
                null
        );
    }

    private LanguageConfig resolveLanguageConfig(String langSlug) {
        if ("plsql".equalsIgnoreCase(langSlug)) {
            return new LanguageConfig("SPACE", 2, List.of(":=", "=>", "||", "..", "%TYPE", "%ROWTYPE"));
        } else if ("sql".equalsIgnoreCase(langSlug)) {
            return new LanguageConfig("SPACE", 2, List.of("<>", "<=", ">=", "!=", "||", "::"));
        } else if ("python".equalsIgnoreCase(langSlug)) {
            return new LanguageConfig("SPACE", 4, List.of("->", "==", "!=", "<=", ">=", "**", "//"));
        } else if ("javascript".equalsIgnoreCase(langSlug) || "typescript".equalsIgnoreCase(langSlug)) {
            return new LanguageConfig("SPACE", 2, List.of("=>", "===", "!==", "??", "?.", "&&", "||"));
        }
        return new LanguageConfig("SPACE", 4, List.of("->", "::", "==", "!=", "<=", ">="));
    }
}
