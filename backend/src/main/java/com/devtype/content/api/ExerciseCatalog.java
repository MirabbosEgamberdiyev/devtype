package com.devtype.content.api;

import java.util.List;
import java.util.Optional;

public interface ExerciseCatalog {
    Optional<ExerciseView> findByKey(String key);
    List<ExerciseView> findByTopicAndLevel(String topicKey, String level);
    List<ExerciseView> findRankedPool(String languageSlug);
}
