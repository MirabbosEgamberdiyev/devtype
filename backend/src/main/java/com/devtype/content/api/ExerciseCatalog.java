package com.devtype.content.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseCatalog {
    Optional<ExerciseView> findByKey(String key);
    Optional<ExerciseView> findById(Long id);
    List<ExerciseView> findByTopicAndLevel(String topicKey, String level);
    List<ExerciseView> findRankedPool(String languageSlug);
    List<ExerciseView> findByLanguageAndLevel(String languageSlug, String level);
    long countPublishedByTopicAndDifficulty(Integer topicId, String level);
    List<UUID> findExerciseKeysByTopicAndDifficulty(Integer topicId, String level);
    Optional<Integer> findTopicIdBySlug(String languageSlug, String topicSlug);
    long countPublishedTopicsByLanguage(String languageSlug);
}
