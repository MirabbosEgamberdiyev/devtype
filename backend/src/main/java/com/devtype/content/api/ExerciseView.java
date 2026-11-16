package com.devtype.content.api;

public record ExerciseView(
    Long id,
    String exerciseKey,
    String languageSlug,
    String topicKey,
    String level,
    String code,
    String title,
    int charCount,
    boolean rankedPool
) {}
