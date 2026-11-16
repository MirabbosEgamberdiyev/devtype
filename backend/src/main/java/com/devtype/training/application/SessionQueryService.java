package com.devtype.training.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.shared.error.DomainException;
import com.devtype.training.infrastructure.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SessionQueryService {

    private final SessionRepository sessionRepository;
    private final SessionExerciseRepository sessionExerciseRepository;
    private final SessionKeylogRepository sessionKeylogRepository;
    private final ExerciseCatalog exerciseCatalog;
    private final LanguageCatalog languageCatalog;

    public SessionQueryService(
            SessionRepository sessionRepository,
            SessionExerciseRepository sessionExerciseRepository,
            SessionKeylogRepository sessionKeylogRepository,
            ExerciseCatalog exerciseCatalog,
            LanguageCatalog languageCatalog) {
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.sessionKeylogRepository = sessionKeylogRepository;
        this.exerciseCatalog = exerciseCatalog;
        this.languageCatalog = languageCatalog;
    }

    public record SessionDetailDto(
            UUID id,
            String status,
            String mode,
            String languageSlug,
            String difficulty,
            Integer durationLimitS,
            boolean ranked,
            String rankedReason,
            CompleteSessionResult.MetricsDto metrics,
            List<StartSessionResult.ExerciseDto> exercises
    ) {}

    public record SessionReplayDto(
            UUID sessionId,
            String keylog,
            List<String> targetCodes,
            CompleteSessionResult.MetricsDto metrics
    ) {}

    @Transactional(readOnly = true)
    public SessionDetailDto getSession(UUID sessionId) {
        SessionEntity session = sessionRepository.findFirstById(sessionId)
                .orElseThrow(() -> new DomainException("SESSION_NOT_FOUND", "Sessiya topilmadi: " + sessionId, HttpStatus.NOT_FOUND));

        String langSlug = languageCatalog.getPublishedLanguageSlugs().stream()
                .filter(slug -> languageCatalog.findLanguageIdBySlug(slug).orElse((short) -1) == session.getLanguageId())
                .findFirst()
                .orElse("java");

        List<SessionExerciseEntity> exList = sessionExerciseRepository.findBySessionIdOrderBySeqAsc(sessionId);
        List<StartSessionResult.ExerciseDto> exDtos = new ArrayList<>();

        for (SessionExerciseEntity se : exList) {
            if (se.getExerciseId() != null) {
                Optional<ExerciseView> evOpt = exerciseCatalog.findById(se.getExerciseId());
                if (evOpt.isPresent()) {
                    ExerciseView ev = evOpt.get();
                    exDtos.add(new StartSessionResult.ExerciseDto(
                            se.getSeq(),
                            UUID.fromString(ev.exerciseKey()),
                            ev.title(),
                            "Exercise",
                            ev.level(),
                            ev.code(),
                            ev.charCount()
                    ));
                    continue;
                }
            }
            exDtos.add(new StartSessionResult.ExerciseDto(
                    se.getSeq(),
                    UUID.randomUUID(),
                    "Generated",
                    "Generated Exercise",
                    session.getDifficulty() != null ? session.getDifficulty() : "BEGINNER",
                    se.getGeneratedCode() != null ? se.getGeneratedCode() : "",
                    se.getGeneratedCode() != null ? se.getGeneratedCode().length() : 0
            ));
        }

        CompleteSessionResult.MetricsDto metrics = new CompleteSessionResult.MetricsDto(
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
                100.0
        );

        return new SessionDetailDto(
                session.getId(),
                session.getStatus(),
                session.getMode(),
                langSlug,
                session.getDifficulty(),
                session.getDurationLimitS() != null ? (int) session.getDurationLimitS() : null,
                session.isRanked(),
                session.getRankedReason(),
                metrics,
                exDtos
        );
    }

    @Transactional(readOnly = true)
    public SessionReplayDto getSessionReplay(UUID sessionId) {
        SessionEntity session = sessionRepository.findFirstById(sessionId)
                .orElseThrow(() -> new DomainException("SESSION_NOT_FOUND", "Sessiya topilmadi: " + sessionId, HttpStatus.NOT_FOUND));

        SessionKeylogEntity keylogEntity = sessionKeylogRepository.findFirstBySessionId(sessionId)
                .orElseThrow(() -> new DomainException("KEYLOG_NOT_FOUND", "Keylog topilmadi", HttpStatus.NOT_FOUND));

        String keylogBase64 = Base64.getEncoder().encodeToString(keylogEntity.getEvents());

        List<SessionExerciseEntity> exList = sessionExerciseRepository.findBySessionIdOrderBySeqAsc(sessionId);
        List<String> codes = new ArrayList<>();
        for (SessionExerciseEntity se : exList) {
            if (se.getGeneratedCode() != null && !se.getGeneratedCode().isBlank()) {
                codes.add(se.getGeneratedCode());
            } else if (se.getExerciseId() != null) {
                exerciseCatalog.findById(se.getExerciseId()).ifPresent(ev -> codes.add(ev.code()));
            }
        }

        CompleteSessionResult.MetricsDto metrics = new CompleteSessionResult.MetricsDto(
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
                100.0
        );

        return new SessionReplayDto(session.getId(), keylogBase64, codes, metrics);
    }
}
