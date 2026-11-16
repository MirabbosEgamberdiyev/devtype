package com.devtype.training.application;

import com.devtype.shared.Constants;
import com.devtype.shared.error.DomainException;
import com.devtype.training.infrastructure.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class StartSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionExerciseRepository sessionExerciseRepository;
    private final CompleteTokenService completeTokenService;

    // Vertikal kesim uchun kanonik fixture kod (docs/06 §2 ga mos Java klassi)
    private static final String FIXTURE_JAVA_CODE =
            "public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, DevType!\");\n    }\n}";
    private static final UUID FIXTURE_EXERCISE_KEY = UUID.fromString("0193a1f2-0000-7000-8000-000000000001");

    public StartSessionUseCase(
            SessionRepository sessionRepository,
            SessionExerciseRepository sessionExerciseRepository,
            CompleteTokenService completeTokenService) {
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.completeTokenService = completeTokenService;
    }

    @Transactional
    public StartSessionResult execute(StartSessionCommand cmd) {
        String langSlug = (cmd.language() != null && !cmd.language().isBlank())
                ? cmd.language().trim().toLowerCase()
                : "java";

        if (!Constants.ALL_SUPPORTED_LANGUAGES.contains(langSlug)) {
            throw new DomainException("LANGUAGE_NOT_AVAILABLE", "Til mavjud emas: " + langSlug, HttpStatus.CONFLICT);
        }

        String mode = (cmd.mode() != null && !cmd.mode().isBlank()) ? cmd.mode() : "PRACTICE";
        String difficulty = cmd.difficulty() != null ? cmd.difficulty() : "BEGINNER";
        boolean strictIndent = Boolean.TRUE.equals(cmd.strictIndent());

        UUID sessionId = UUID.randomUUID();
        Instant startedAt = Instant.now();
        Instant expiresAt = startedAt.plus(Constants.SESSION_EXPIRATION_HOURS, ChronoUnit.HOURS);

        // Language config (docs/00 va docs/07 §2)
        StartSessionResult.LanguageDto languageDto = resolveLanguage(langSlug);

        // Session Entity saqlash
        SessionEntity session = new SessionEntity();
        session.setId(sessionId);
        session.setStartedAt(startedAt);
        session.setGuestId(UUID.randomUUID()); // Mehmon sifatida
        session.setLanguageId((short) 1); // java
        session.setMode(mode);
        session.setDifficulty(difficulty);
        session.setStrictIndent(strictIndent);
        session.setInputDevice(cmd.inputDevice() != null ? cmd.inputDevice() : "PHYSICAL");
        session.setEngineVersion("1.0.0");
        session.setStatus("STARTED");

        sessionRepository.save(session);

        // Session Exercise saqlash (generatedCode orqali)
        SessionExerciseEntity exEntity = new SessionExerciseEntity();
        exEntity.setSessionId(sessionId);
        exEntity.setSeq((short) 0);
        exEntity.setGeneratedCode(FIXTURE_JAVA_CODE);
        exEntity.setStartedAt(startedAt);

        sessionExerciseRepository.save(exEntity);

        String completeToken = completeTokenService.generateToken(sessionId, startedAt, expiresAt);

        StartSessionResult.ExerciseDto exerciseDto = new StartSessionResult.ExerciseDto(
                0,
                FIXTURE_EXERCISE_KEY,
                "Hello World",
                "DevType da birinchi mashq",
                difficulty,
                FIXTURE_JAVA_CODE,
                FIXTURE_JAVA_CODE.length()
        );

        return new StartSessionResult(
                sessionId,
                startedAt,
                expiresAt,
                mode,
                "STOP",
                null,
                languageDto,
                List.of(exerciseDto),
                completeToken
        );
    }

    private StartSessionResult.LanguageDto resolveLanguage(String slug) {
        if ("javascript".equals(slug) || "typescript".equals(slug) || "html".equals(slug) || "css".equals(slug) || "bash".equals(slug)) {
            return new StartSessionResult.LanguageDto(
                    slug,
                    slug,
                    new StartSessionResult.IndentDto("SPACE", 2),
                    List.of("=>", "===", "!==", "==", "!=", "<=", ">=")
            );
        } else if ("go".equals(slug)) {
            return new StartSessionResult.LanguageDto(
                    slug,
                    slug,
                    new StartSessionResult.IndentDto("TAB", 1),
                    List.of(":=", "==", "!=", "<=", ">=")
            );
        } else if ("plsql".equals(slug) || "sql".equals(slug)) {
            return new StartSessionResult.LanguageDto(
                    slug,
                    slug,
                    new StartSessionResult.IndentDto("SPACE", 4),
                    List.of(":=", "%TYPE", "%ROWTYPE", "<=", ">=", "<>", "!=")
            );
        } else {
            // Java, Python, C++, C#, Kotlin, Rust, C, PHP
            return new StartSessionResult.LanguageDto(
                    slug,
                    slug,
                    new StartSessionResult.IndentDto("SPACE", 4),
                    List.of("->", "::", "==", "!=", "<=", ">=")
            );
        }
    }
}
