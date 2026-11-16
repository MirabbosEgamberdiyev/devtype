package com.devtype.training.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.shared.error.DomainException;
import com.devtype.training.api.TopicLevelCompleted;
import com.devtype.training.infrastructure.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class TopicProgressService {

    private final UserTopicProgressRepository userTopicProgressRepository;
    private final UserExerciseBestRepository userExerciseBestRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final ExerciseCatalog exerciseCatalog;
    private final ApplicationEventPublisher eventPublisher;

    private static final List<String> DIFFICULTY_ORDER = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT");

    public TopicProgressService(
            UserTopicProgressRepository userTopicProgressRepository,
            UserExerciseBestRepository userExerciseBestRepository,
            UserLanguageRepository userLanguageRepository,
            ExerciseCatalog exerciseCatalog,
            ApplicationEventPublisher eventPublisher) {
        this.userTopicProgressRepository = userTopicProgressRepository;
        this.userExerciseBestRepository = userExerciseBestRepository;
        this.userLanguageRepository = userLanguageRepository;
        this.exerciseCatalog = exerciseCatalog;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public void checkLevelAccessible(Long userId, Integer topicId, String difficulty) {
        if (userId == null) {
            return; // Mehmon uchun cheklov yo'q
        }
        if (topicId == null || difficulty == null) {
            return;
        }
        String diff = difficulty.toUpperCase().trim();
        if ("BEGINNER".equals(diff)) {
            return; // BEGINNER doim AVAILABLE (FR-LS-3)
        }

        Optional<UserTopicProgressEntity> progressOpt = userTopicProgressRepository
                .findByUserIdAndTopicIdAndDifficulty(userId, topicId, diff);

        String state = progressOpt.map(UserTopicProgressEntity::getState).orElse("LOCKED");
        if ("LOCKED".equals(state)) {
            throw new DomainException("LEVEL_LOCKED", "Daraja hali qulflangan: " + diff, HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public void onSessionExerciseCompleted(
            Long userId,
            String languageSlug,
            Long exerciseId,
            double wpm,
            double accuracy,
            UUID sessionId) {
        if (userId == null || exerciseId == null) {
            return;
        }

        Optional<ExerciseView> exOpt = exerciseCatalog.findById(exerciseId);
        if (exOpt.isEmpty()) {
            return;
        }
        ExerciseView ex = exOpt.get();
        UUID exerciseKey = UUID.fromString(ex.exerciseKey());

        // 1. Upsert user_exercise_best
        Optional<UserExerciseBestEntity> bestOpt = userExerciseBestRepository.findByUserIdAndExerciseKey(userId, exerciseKey);
        UserExerciseBestEntity best;
        boolean justCompleted = (accuracy >= 95.0);

        if (bestOpt.isPresent()) {
            best = bestOpt.get();
            best.setAttempts(best.getAttempts() + 1);
            if (BigDecimal.valueOf(wpm).compareTo(best.getBestWpm()) > 0) {
                best.setBestWpm(BigDecimal.valueOf(wpm));
            }
            if (BigDecimal.valueOf(accuracy).compareTo(best.getBestAccuracy()) > 0) {
                best.setBestAccuracy(BigDecimal.valueOf(accuracy));
            }
            if (justCompleted) {
                best.setCompleted(true);
            }
            best.setLastSessionId(sessionId);
            best.setUpdatedAt(Instant.now());
        } else {
            best = new UserExerciseBestEntity();
            best.setUserId(userId);
            best.setExerciseKey(exerciseKey);
            best.setBestWpm(BigDecimal.valueOf(wpm));
            best.setBestAccuracy(BigDecimal.valueOf(accuracy));
            best.setCompleted(justCompleted);
            best.setAttempts(1);
            best.setLastSessionId(sessionId);
            best.setUpdatedAt(Instant.now());
        }
        userExerciseBestRepository.save(best);

        // 2. Mavzu va daraja progressini hisoblash (BL-LS-1)
        if (ex.topicKey() == null) {
            return;
        }

        Optional<Integer> topicIdOpt = exerciseCatalog.findTopicIdBySlug(languageSlug, ex.topicKey());
        if (topicIdOpt.isEmpty()) {
            return;
        }
        Integer topicId = topicIdOpt.get();
        String diff = ex.level().toUpperCase();

        recomputeTopicProgress(userId, languageSlug, topicId, ex.topicKey(), diff);
    }

    @Transactional
    public void recomputeTopicProgress(Long userId, String languageSlug, Integer topicId, String topicSlug, String difficulty) {
        UserTopicProgressEntity progress = userTopicProgressRepository
                .findByUserIdAndTopicIdAndDifficulty(userId, topicId, difficulty)
                .orElseGet(() -> {
                    UserTopicProgressEntity p = new UserTopicProgressEntity();
                    p.setUserId(userId);
                    p.setTopicId(topicId);
                    p.setDifficulty(difficulty);
                    p.setState("BEGINNER".equals(difficulty) ? "AVAILABLE" : "LOCKED");
                    return p;
                });

        if ("LOCKED".equals(progress.getState())) {
            return; // Himoya
        }

        List<UUID> topicExerciseKeys = exerciseCatalog.findExerciseKeysByTopicAndDifficulty(topicId, difficulty);
        int total = topicExerciseKeys.size();
        if (total == 0) {
            return;
        }

        long completedCount = userExerciseBestRepository.countByUserIdAndExerciseKeyInAndCompletedTrue(userId, topicExerciseKeys);
        long masteredCount = userExerciseBestRepository.countByUserIdAndExerciseKeyInAndBestAccuracyGreaterThanEqual(
                userId, topicExerciseKeys, BigDecimal.valueOf(98.0));

        progress.setCompletedCnt((int) completedCount);
        progress.setMasteredCnt((int) masteredCount);
        progress.setAttemptsCnt(progress.getAttemptsCnt() + 1);
        progress.setUpdatedAt(Instant.now());

        String oldState = progress.getState();
        String newState;

        int requiredForCompleted = (int) Math.ceil(0.8 * total);

        if (masteredCount == total) {
            newState = "MASTERED";
        } else if (completedCount >= requiredForCompleted) {
            newState = "COMPLETED";
        } else if (completedCount >= 1 || progress.getAttemptsCnt() > 0) {
            // Holat hech qachon pasaymaydi (BL-LS-1): agar oldState COMPLETED bo'lsa, COMPLETED qoladi
            if ("COMPLETED".equals(oldState) || "MASTERED".equals(oldState)) {
                newState = "COMPLETED";
            } else {
                newState = "IN_PROGRESS";
            }
        } else {
            newState = "AVAILABLE";
        }

        progress.setState(newState);
        if (("COMPLETED".equals(newState) || "MASTERED".equals(newState)) && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }
        userTopicProgressRepository.save(progress);

        // Agar yangi state COMPLETED yoki MASTERED bo'lsa va oldin bo'lmagan bo'lsa:
        if (("COMPLETED".equals(newState) || "MASTERED".equals(newState)) &&
                !("COMPLETED".equals(oldState) || "MASTERED".equals(oldState))) {
            unlockNextDifficulty(userId, topicId, difficulty);
            eventPublisher.publishEvent(new TopicLevelCompleted(userId, languageSlug, topicSlug, difficulty, Instant.now()));
        }
    }

    private void unlockNextDifficulty(Long userId, Integer topicId, String currentDifficulty) {
        int idx = DIFFICULTY_ORDER.indexOf(currentDifficulty.toUpperCase());
        if (idx >= 0 && idx < DIFFICULTY_ORDER.size() - 1) {
            String nextDifficulty = DIFFICULTY_ORDER.get(idx + 1);
            UserTopicProgressEntity nextProgress = userTopicProgressRepository
                    .findByUserIdAndTopicIdAndDifficulty(userId, topicId, nextDifficulty)
                    .orElseGet(() -> {
                        UserTopicProgressEntity p = new UserTopicProgressEntity();
                        p.setUserId(userId);
                        p.setTopicId(topicId);
                        p.setDifficulty(nextDifficulty);
                        p.setState("LOCKED");
                        return p;
                    });

            if ("LOCKED".equals(nextProgress.getState())) {
                nextProgress.setState("AVAILABLE");
                nextProgress.setUpdatedAt(Instant.now());
                userTopicProgressRepository.save(nextProgress);
            }
        }
    }

    @Transactional
    public void applyPlacementResult(Long userId, Short languageId, String languageSlug,
                                     boolean passedB, boolean passedI, boolean passedA) {
        String highestUnlocked;
        List<String> unlockedLevels = new ArrayList<>();
        unlockedLevels.add("BEGINNER");

        if (passedB && passedI && passedA) {
            highestUnlocked = "EXPERT";
            unlockedLevels.add("INTERMEDIATE");
            unlockedLevels.add("ADVANCED");
            unlockedLevels.add("EXPERT");
        } else if (passedB && passedI) {
            highestUnlocked = "ADVANCED";
            unlockedLevels.add("INTERMEDIATE");
            unlockedLevels.add("ADVANCED");
        } else if (passedB) {
            highestUnlocked = "INTERMEDIATE";
            unlockedLevels.add("INTERMEDIATE");
        } else {
            highestUnlocked = "BEGINNER";
        }

        // user_languages ni yangilash
        Optional<UserLanguageEntity> userLangOpt = userLanguageRepository.findByUserIdAndLanguageId(userId, languageId);
        UserLanguageEntity userLang = userLangOpt.orElseGet(() -> {
            UserLanguageEntity ul = new UserLanguageEntity();
            ul.setUserId(userId);
            ul.setLanguageId(languageId);
            return ul;
        });
        userLang.setStartLevel(highestUnlocked);
        userLang.setPlacementDone(true);
        userLanguageRepository.save(userLang);
    }
}
