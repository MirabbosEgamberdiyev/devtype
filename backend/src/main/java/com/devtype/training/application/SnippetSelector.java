package com.devtype.training.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.shared.error.DomainException;
import com.devtype.training.infrastructure.SessionEntity;
import com.devtype.training.infrastructure.SessionExerciseEntity;
import com.devtype.training.infrastructure.SessionExerciseRepository;
import com.devtype.training.infrastructure.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SnippetSelector {

    private final ExerciseCatalog exerciseCatalog;
    private final SessionRepository sessionRepository;
    private final SessionExerciseRepository sessionExerciseRepository;
    private final Random random = new Random();

    public SnippetSelector(
            ExerciseCatalog exerciseCatalog,
            SessionRepository sessionRepository,
            SessionExerciseRepository sessionExerciseRepository) {
        this.exerciseCatalog = exerciseCatalog;
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
    }

    @Transactional(readOnly = true)
    public List<ExerciseView> pick(
            Long userId,
            UUID guestId,
            String languageSlug,
            short languageId,
            String mode,
            String topicSlug,
            String difficulty,
            UUID specificExerciseKey,
            int count) {

        // Agar aniq mashq so'ralgan bo'lsa (PRACTICE)
        if (specificExerciseKey != null) {
            Optional<ExerciseView> exOpt = exerciseCatalog.findByKey(specificExerciseKey.toString());
            if (exOpt.isEmpty()) {
                throw new DomainException("EXERCISE_NOT_FOUND", "Mashq topilmadi: " + specificExerciseKey, HttpStatus.NOT_FOUND);
            }
            return List.of(exOpt.get());
        }

        List<ExerciseView> candidates;
        if ("SPEED_TEST".equalsIgnoreCase(mode)) {
            candidates = exerciseCatalog.findRankedPool(languageSlug);
        } else if (topicSlug != null && !topicSlug.isBlank()) {
            candidates = exerciseCatalog.findByTopicAndLevel(languageSlug + ":" + topicSlug, difficulty);
        } else {
            candidates = exerciseCatalog.findByLanguageAndLevel(languageSlug, difficulty);
        }

        if (candidates.isEmpty()) {
            throw new DomainException("NO_EXERCISES_AVAILABLE", "Ushbu parametrlar bo'yicha mashq topilmadi", HttpStatus.CONFLICT);
        }

        // Oxirgi 20 sessiyada ko'rilgan exercise_key lar
        List<SessionEntity> recentSessions = (userId != null)
                ? sessionRepository.findRecentByUserAndLanguage(userId, languageId)
                : (guestId != null ? sessionRepository.findRecentByGuestAndLanguage(guestId, languageId) : Collections.emptyList());

        List<UUID> recentSessionIds = recentSessions.stream().map(SessionEntity::getId).toList();
        List<SessionExerciseEntity> recentExercises = recentSessionIds.isEmpty()
                ? Collections.emptyList()
                : sessionExerciseRepository.findBySessionIdIn(recentSessionIds);

        // Exercise id -> ExerciseView map
        Map<Long, ExerciseView> candidateMap = new LinkedHashMap<>();
        for (ExerciseView ev : candidates) {
            candidateMap.put(ev.id(), ev);
        }

        // Ko'rilgan mashq id lari tartibi (eng yangisidan eng eskigacha)
        List<Long> seenIdsOrdered = new ArrayList<>();
        Set<Long> seenIdSet = new HashSet<>();
        for (SessionExerciseEntity se : recentExercises) {
            if (se.getExerciseId() != null && candidateMap.containsKey(se.getExerciseId())) {
                if (seenIdSet.add(se.getExerciseId())) {
                    seenIdsOrdered.add(se.getExerciseId());
                }
            }
        }

        // pool = candidates - recent
        List<ExerciseView> pool = new ArrayList<>();
        for (ExerciseView ev : candidates) {
            if (!seenIdSet.contains(ev.id())) {
                pool.add(ev);
            }
        }

        // Agar pool < count bo'lsa: candidates ∩ recent, eng eski ko'rilganidan boshlab qo'shamiz
        if (pool.size() < count) {
            // seenIdsOrdered yangidan eskiga qarab kelgan, bizga eng eskilari (oxiridagilar) kerak
            Collections.reverse(seenIdsOrdered);
            for (Long seenId : seenIdsOrdered) {
                if (pool.size() >= count) break;
                ExerciseView ev = candidateMap.get(seenId);
                if (ev != null && !pool.contains(ev)) {
                    pool.add(ev);
                }
            }
        }

        // Agar hali ham yetmasa (masalan pool < count)
        if (pool.isEmpty()) {
            pool.addAll(candidates);
        }

        // Tasodifiy tanlash
        Collections.shuffle(pool, random);
        return pool.subList(0, Math.min(count, pool.size()));
    }
}
