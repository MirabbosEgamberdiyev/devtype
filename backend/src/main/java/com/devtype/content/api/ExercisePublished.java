package com.devtype.content.api;

import java.time.Instant;

public record ExercisePublished(
    String exerciseKey,
    String languageSlug,
    String topicKey,
    String level,
    Instant timestamp
) {}
