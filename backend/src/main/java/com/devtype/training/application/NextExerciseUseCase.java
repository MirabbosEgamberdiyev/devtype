package com.devtype.training.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.shared.error.DomainException;
import com.devtype.training.infrastructure.SessionEntity;
import com.devtype.training.infrastructure.SessionExerciseEntity;
import com.devtype.training.infrastructure.SessionExerciseRepository;
import com.devtype.training.infrastructure.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class NextExerciseUseCase {

    private final SessionRepository sessionRepository;
    private final SessionExerciseRepository sessionExerciseRepository;
    private final ExerciseCatalog exerciseCatalog;
    private final LanguageCatalog languageCatalog;
    private final SnippetSelector snippetSelector;

    private static final int MAX_EXERCISES_PER_SESSION = 40;

    public NextExerciseUseCase(
            SessionRepository sessionRepository,
            SessionExerciseRepository sessionExerciseRepository,
            ExerciseCatalog exerciseCatalog,
            LanguageCatalog languageCatalog,
            SnippetSelector snippetSelector) {
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.exerciseCatalog = exerciseCatalog;
        this.languageCatalog = languageCatalog;
        this.snippetSelector = snippetSelector;
    }

    public record NextExerciseResult(List<StartSessionResult.ExerciseDto> exercises) {}

    @Transactional
    public NextExerciseResult execute(UUID sessionId, int afterSeq) {
        SessionEntity session = sessionRepository.findFirstById(sessionId)
                .orElseThrow(() -> new DomainException("SESSION_NOT_FOUND", "Sessiya topilmadi: " + sessionId, HttpStatus.NOT_FOUND));

        if (!"STARTED".equals(session.getStatus())) {
            throw new DomainException("SESSION_NOT_ACTIVE", "Sessiya faol emas", HttpStatus.CONFLICT);
        }

        if (!"SPEED_TEST".equalsIgnoreCase(session.getMode())) {
            throw new DomainException("INVALID_MODE", "Next amali faqat SPEED_TEST uchun amal qiladi", HttpStatus.BAD_REQUEST);
        }

        List<SessionExerciseEntity> existing = sessionExerciseRepository.findBySessionIdOrderBySeqAsc(sessionId);

        // Idempotentlik tekshiruvi: agar afterSeq dan keyingi mashqlar mavjud bo'lsa
        List<SessionExerciseEntity> afterList = existing.stream()
                .filter(se -> se.getSeq() > afterSeq)
                .toList();

        if (!afterList.isEmpty()) {
            return new NextExerciseResult(afterList.stream().map(this::toDto).toList());
        }

        if (existing.size() >= MAX_EXERCISES_PER_SESSION) {
            throw new DomainException("SESSION_EXERCISE_LIMIT", "Sessiyada maksimal mashqlar soniga yetildi", HttpStatus.CONFLICT);
        }

        String langSlug = languageCatalog.getPublishedLanguageSlugs().stream()
                .filter(slug -> languageCatalog.findLanguageIdBySlug(slug).orElse((short) -1) == session.getLanguageId())
                .findFirst()
                .orElse("java");

        List<ExerciseView> picked = snippetSelector.pick(
                session.getUserId(),
                session.getGuestId(),
                langSlug,
                session.getLanguageId(),
                "SPEED_TEST",
                null,
                session.getDifficulty() != null ? session.getDifficulty() : "BEGINNER",
                null,
                2
        );

        short maxSeq = existing.isEmpty() ? -1 : existing.get(existing.size() - 1).getSeq();
        List<SessionExerciseEntity> newEntities = new ArrayList<>();
        List<StartSessionResult.ExerciseDto> dtos = new ArrayList<>();

        for (ExerciseView ev : picked) {
            maxSeq++;
            SessionExerciseEntity se = new SessionExerciseEntity();
            se.setSessionId(sessionId);
            se.setSeq(maxSeq);
            se.setExerciseId(ev.id());
            se.setStartedAt(Instant.now());
            newEntities.add(se);

            dtos.add(new StartSessionResult.ExerciseDto(
                    maxSeq,
                    UUID.fromString(ev.exerciseKey()),
                    ev.title(),
                    "Speed Test",
                    ev.level(),
                    ev.code(),
                    ev.charCount()
            ));
        }

        sessionExerciseRepository.saveAll(newEntities);
        return new NextExerciseResult(dtos);
    }

    private StartSessionResult.ExerciseDto toDto(SessionExerciseEntity se) {
        if (se.getExerciseId() != null) {
            Optional<ExerciseView> evOpt = exerciseCatalog.findById(se.getExerciseId());
            if (evOpt.isPresent()) {
                ExerciseView ev = evOpt.get();
                return new StartSessionResult.ExerciseDto(
                        se.getSeq(),
                        UUID.fromString(ev.exerciseKey()),
                        ev.title(),
                        "Speed Test",
                        ev.level(),
                        ev.code(),
                        ev.charCount()
                );
            }
        }
        return new StartSessionResult.ExerciseDto(
                se.getSeq(),
                UUID.randomUUID(),
                "Generated Exercise",
                "Speed Test",
                "BEGINNER",
                se.getGeneratedCode() != null ? se.getGeneratedCode() : "",
                se.getGeneratedCode() != null ? se.getGeneratedCode().length() : 0
        );
    }
}
