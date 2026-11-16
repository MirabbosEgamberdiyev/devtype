package com.devtype.training.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.shared.Constants;
import com.devtype.shared.error.DomainException;
import com.devtype.training.infrastructure.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StartSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionExerciseRepository sessionExerciseRepository;
    private final CompleteTokenService completeTokenService;
    private final LanguageCatalog languageCatalog;
    private final ExerciseCatalog exerciseCatalog;
    private final SnippetSelector snippetSelector;
    private final TopicProgressService topicProgressService;
    private final SpecialCharsGenerator specialCharsGenerator;

    public StartSessionUseCase(
            SessionRepository sessionRepository,
            SessionExerciseRepository sessionExerciseRepository,
            CompleteTokenService completeTokenService,
            LanguageCatalog languageCatalog,
            ExerciseCatalog exerciseCatalog,
            SnippetSelector snippetSelector,
            TopicProgressService topicProgressService,
            SpecialCharsGenerator specialCharsGenerator) {
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.completeTokenService = completeTokenService;
        this.languageCatalog = languageCatalog;
        this.exerciseCatalog = exerciseCatalog;
        this.snippetSelector = snippetSelector;
        this.topicProgressService = topicProgressService;
        this.specialCharsGenerator = specialCharsGenerator;
    }

    @Transactional
    public StartSessionResult execute(StartSessionCommand cmd) {
        String langSlug = (cmd.language() != null && !cmd.language().isBlank())
                ? cmd.language().trim().toLowerCase()
                : "java";

        if (!Constants.ALL_SUPPORTED_LANGUAGES.contains(langSlug)) {
            throw new DomainException("LANGUAGE_NOT_AVAILABLE", "Til qo'llab-quvvatlanmaydi: " + langSlug, HttpStatus.CONFLICT);
        }

        Short languageId = languageCatalog.findLanguageIdBySlug(langSlug).orElse((short) 1);

        String mode = (cmd.mode() != null && !cmd.mode().isBlank()) ? cmd.mode().toUpperCase().trim() : "PRACTICE";
        String difficulty = cmd.difficulty() != null ? cmd.difficulty().toUpperCase().trim() : "BEGINNER";
        boolean isPlacement = Boolean.TRUE.equals(cmd.placement());
        boolean strictIndent = Boolean.TRUE.equals(cmd.strictIndent());
        boolean accuracyMaster = Boolean.TRUE.equals(cmd.accuracyMaster());

        // Mehmon yoki User
        Long userId = cmd.userId();
        UUID guestId = (userId == null) ? (cmd.guestId() != null ? cmd.guestId() : UUID.randomUUID()) : null;

        // 1. Bir foydalanuvchida ko'pi bilan 3 ta STARTED sessiya; 4-sida eng eskisi ABANDONED bo'ladi (BL-TM-2)
        abandonOldSessionsIfNeeded(userId, guestId);

        // 2. Daraja tekshiruvi (BL-LS-2)
        Integer topicId = null;
        if (cmd.topic() != null && !cmd.topic().isBlank()) {
            topicId = exerciseCatalog.findTopicIdBySlug(langSlug, cmd.topic()).orElse(null);
            if (topicId != null && !isPlacement && "PRACTICE".equals(mode)) {
                topicProgressService.checkLevelAccessible(userId, topicId, difficulty);
            }
        }

        // 3. Mashqlarni tanlash / generatsiya qilish
        UUID sessionId = UUID.randomUUID();
        Instant startedAt = Instant.now();
        Instant expiresAt = startedAt.plus(Constants.SESSION_EXPIRATION_HOURS, ChronoUnit.HOURS);
        long seed = System.currentTimeMillis();

        List<StartSessionResult.ExerciseDto> exerciseDtos = new ArrayList<>();
        List<SessionExerciseEntity> sessionExerciseEntities = new ArrayList<>();

        if ("SPECIAL_CHARS".equals(mode)) {
            String genCode = specialCharsGenerator.generate(langSlug, cmd.specialCategory(), seed);
            UUID genKey = UUID.randomUUID();

            SessionExerciseEntity se = new SessionExerciseEntity();
            se.setSessionId(sessionId);
            se.setSeq((short) 0);
            se.setGeneratedCode(genCode);
            se.setStartedAt(startedAt);
            sessionExerciseEntities.add(se);

            exerciseDtos.add(new StartSessionResult.ExerciseDto(
                    0,
                    genKey,
                    "Special Characters (" + (cmd.specialCategory() != null ? cmd.specialCategory() : "BRACKETS") + ")",
                    "Maxsus belgilar mashqi",
                    difficulty,
                    genCode,
                    genCode.length()
            ));
        } else if (isPlacement) {
            // Placement testi: 3 ta snippet (BEGINNER, INTERMEDIATE, ADVANCED)
            List<String> levels = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
            short seq = 0;
            for (String lvl : levels) {
                List<ExerciseView> list = snippetSelector.pick(userId, guestId, langSlug, languageId, "PRACTICE", null, lvl, null, 1);
                if (list.isEmpty()) {
                    throw new DomainException("NO_EXERCISES_AVAILABLE", "Placement mashqi topilmadi: " + lvl, HttpStatus.CONFLICT);
                }
                ExerciseView ev = list.get(0);
                SessionExerciseEntity se = new SessionExerciseEntity();
                se.setSessionId(sessionId);
                se.setSeq(seq);
                se.setExerciseId(ev.id());
                se.setStartedAt(startedAt);
                sessionExerciseEntities.add(se);

                exerciseDtos.add(new StartSessionResult.ExerciseDto(
                        seq,
                        UUID.fromString(ev.exerciseKey()),
                        ev.title(),
                        "Placement " + lvl,
                        lvl,
                        ev.code(),
                        ev.charCount()
                ));
                seq++;
            }
        } else if ("SPEED_TEST".equals(mode)) {
            // SPEED_TEST: 3 ta snippet (boshlang'ich)
            List<ExerciseView> picked = snippetSelector.pick(userId, guestId, langSlug, languageId, mode, null, difficulty, null, 3);
            short seq = 0;
            for (ExerciseView ev : picked) {
                SessionExerciseEntity se = new SessionExerciseEntity();
                se.setSessionId(sessionId);
                se.setSeq(seq);
                se.setExerciseId(ev.id());
                se.setStartedAt(startedAt);
                sessionExerciseEntities.add(se);

                exerciseDtos.add(new StartSessionResult.ExerciseDto(
                        seq,
                        UUID.fromString(ev.exerciseKey()),
                        ev.title(),
                        "Speed Test",
                        ev.level(),
                        ev.code(),
                        ev.charCount()
                ));
                seq++;
            }
        } else {
            // PRACTICE, ACCURACY va boshqalar
            List<ExerciseView> picked = snippetSelector.pick(
                    userId, guestId, langSlug, languageId, mode, cmd.topic(), difficulty, cmd.exerciseKey(), 1);
            ExerciseView ev = picked.get(0);

            SessionExerciseEntity se = new SessionExerciseEntity();
            se.setSessionId(sessionId);
            se.setSeq((short) 0);
            se.setExerciseId(ev.id());
            se.setStartedAt(startedAt);
            sessionExerciseEntities.add(se);

            exerciseDtos.add(new StartSessionResult.ExerciseDto(
                    0,
                    UUID.fromString(ev.exerciseKey()),
                    ev.title(),
                    ev.topicKey() != null ? ev.topicKey() : "Practice",
                    ev.level(),
                    ev.code(),
                    ev.charCount()
            ));
        }

        // 4. Session Entity saqlash
        SessionEntity session = new SessionEntity();
        session.setId(sessionId);
        session.setStartedAt(startedAt);
        session.setUserId(userId);
        session.setGuestId(guestId);
        session.setLanguageId(languageId);
        session.setMode(mode);
        session.setTopicId(topicId);
        session.setDifficulty(difficulty);
        session.setAccuracyMaster(accuracyMaster);
        session.setPlacement(isPlacement);
        session.setSpecialCategory(cmd.specialCategory());
        session.setGenSeed(seed);
        session.setChallengeId(cmd.challengeId());
        session.setCompetitionId(cmd.competitionId());
        session.setStrictIndent(strictIndent);
        session.setInputDevice(cmd.inputDevice() != null ? cmd.inputDevice() : "PHYSICAL");
        session.setEngineVersion("1.0.0");
        session.setStatus("STARTED");

        if ("SPEED_TEST".equals(mode)) {
            int dur = (cmd.duration() != null && (cmd.duration() == 30 || cmd.duration() == 60 || cmd.duration() == 120))
                    ? cmd.duration()
                    : 60;
            session.setDurationLimitS((short) dur);
        }

        sessionRepository.save(session);
        sessionExerciseRepository.saveAll(sessionExerciseEntities);

        String completeToken = completeTokenService.generateToken(sessionId, startedAt, expiresAt);
        StartSessionResult.LanguageDto languageDto = resolveLanguage(langSlug);

        return new StartSessionResult(
                sessionId,
                startedAt,
                expiresAt,
                mode,
                "STOP",
                session.getDurationLimitS() != null ? (int) session.getDurationLimitS() : null,
                languageDto,
                exerciseDtos,
                completeToken
        );
    }

    private void abandonOldSessionsIfNeeded(Long userId, UUID guestId) {
        List<SessionEntity> activeSessions = (userId != null)
                ? sessionRepository.findByUserIdAndStatusOrderByStartedAtAsc(userId, "STARTED")
                : (guestId != null ? sessionRepository.findByGuestIdAndStatusOrderByStartedAtAsc(guestId, "STARTED") : List.of());

        if (activeSessions.size() >= 3) {
            // Eng eskilarini ABANDONED qilamiz
            int toAbandon = activeSessions.size() - 2;
            for (int i = 0; i < toAbandon; i++) {
                SessionEntity s = activeSessions.get(i);
                s.setStatus("ABANDONED");
                sessionRepository.save(s);
            }
        }
    }

    private StartSessionResult.LanguageDto resolveLanguage(String langSlug) {
        var viewOpt = languageCatalog.getLanguage(langSlug);
        if (viewOpt.isPresent()) {
            var v = viewOpt.get();
            return new StartSessionResult.LanguageDto(
                    v.slug(),
                    v.slug(),
                    new StartSessionResult.IndentDto(v.indentStyle(), v.indentSize()),
                    v.tokenPatterns()
            );
        }

        int indentSize = 4;
        String indentStyle = "SPACE";
        List<String> patterns = List.of("->", "::", "==", "!=", "<=", ">=");

        if ("plsql".equals(langSlug)) {
            indentSize = 2;
            patterns = List.of(":=", "=>", "||", "..", "%TYPE", "%ROWTYPE");
        } else if ("sql".equals(langSlug)) {
            indentSize = 2;
            patterns = List.of("<>", "<=", ">=", "!=", "||", "::");
        } else if ("python".equals(langSlug)) {
            indentSize = 4;
            patterns = List.of("->", "==", "!=", "<=", ">=", "**", "//");
        } else if ("javascript".equals(langSlug) || "typescript".equals(langSlug)) {
            indentSize = 2;
            patterns = List.of("=>", "===", "!==", "??", "?.", "&&", "||");
        }

        return new StartSessionResult.LanguageDto(
                langSlug,
                langSlug,
                new StartSessionResult.IndentDto(indentStyle, indentSize),
                patterns
        );
    }
}
