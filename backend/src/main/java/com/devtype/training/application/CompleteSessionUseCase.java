package com.devtype.training.application;

import com.devtype.shared.Constants;
import com.devtype.shared.error.DomainException;
import com.devtype.training.domain.*;
import com.devtype.training.infrastructure.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CompleteSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionExerciseRepository sessionExerciseRepository;
    private final CompleteTokenService completeTokenService;

    public CompleteSessionUseCase(
            SessionRepository sessionRepository,
            SessionExerciseRepository sessionExerciseRepository,
            CompleteTokenService completeTokenService) {
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.completeTokenService = completeTokenService;
    }

    @Transactional
    public CompleteSessionResult execute(CompleteSessionCommand cmd) {
        SessionEntity session = sessionRepository.findFirstById(cmd.sessionId())
                .orElseThrow(() -> new DomainException("SESSION_NOT_FOUND", "Sessiya topilmadi: " + cmd.sessionId(), HttpStatus.NOT_FOUND));

        Instant expiresAt = session.getStartedAt().plus(Constants.SESSION_EXPIRATION_HOURS, ChronoUnit.HOURS);

        // 1. completeToken tekshiruvi
        completeTokenService.verifyToken(session.getId(), cmd.completeToken(), session.getStartedAt(), expiresAt);

        // 2. Status tekshiruvi (idempotentlik)
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
            sessionRepository.save(session);
            throw new DomainException("SESSION_EXPIRED", "Sessiya muddati tugagan (24 soat)", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 4. Mashq kodini olish
        List<SessionExerciseEntity> exercises = sessionExerciseRepository.findBySessionIdOrderBySeqAsc(session.getId());
        if (exercises.isEmpty()) {
            throw new DomainException("SESSION_CORRUPTED", "Sessiyada mashq mavjud emas", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String code = exercises.get(0).getGeneratedCode();

        // 5. Replay config va ijro
        EngineMode engineMode = EngineMode.valueOf(session.getMode());
        KeylogReplayer.ReplaySessionConfig replayConfig = new KeylogReplayer.ReplaySessionConfig(
                engineMode,
                ErrorMode.STOP,
                session.getDurationLimitS() != null ? session.getDurationLimitS().intValue() : null,
                session.isStrictIndent()
        );

        LanguageConfig langConfig = new LanguageConfig(
                "SPACE",
                4,
                List.of("->", "::", "==", "!=", "<=", ">=")
        );

        ReplayResult replayResult;
        try {
            replayResult = KeylogReplayer.replay(
                    replayConfig,
                    List.of(code),
                    langConfig,
                    cmd.keylog(),
                    SuspicionRules.SuspicionContext.empty()
            );
        } catch (IllegalArgumentException e) {
            session.setStatus("REJECTED");
            session.setRejectReason("KEYLOG_INVALID");
            sessionRepository.save(session);
            throw new DomainException("KEYLOG_INVALID", "Replay xatosi: " + e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 6. Timing tekshiruvi (docs/00 §4.6)
        long timeDiff = Math.abs((long) replayResult.metrics().elapsedMs() - cmd.clientElapsedMs());
        if (timeDiff > Constants.TIMING_TOLERANCE_MS) {
            session.setStatus("REJECTED");
            session.setRejectReason("TIMING_MISMATCH");
            sessionRepository.save(session);
            throw new DomainException("TIMING_MISMATCH", "Klient va server vaqtlari mos kelmadi", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 7. Sessiyani yakunlash va saqlash
        EngineMetrics m = replayResult.metrics();
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
        session.setRanked(false);
        session.setRankedReason("NOT_RANKED_MODE");

        sessionRepository.save(session);

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

        List<CompleteSessionResult.WpmPointDto> wpmSeries = new ArrayList<>();
        wpmSeries.add(new CompleteSessionResult.WpmPointDto(
                Math.max(1, m.elapsedMs() / 1000),
                m.wpm().doubleValue(),
                m.rawWpm().doubleValue(),
                m.incorrectKeys()
        ));

        return new CompleteSessionResult(
                session.getId(),
                "COMPLETED",
                false,
                "NOT_RANKED_MODE",
                metricsDto,
                wpmSeries,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                null,
                null
        );
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
}
