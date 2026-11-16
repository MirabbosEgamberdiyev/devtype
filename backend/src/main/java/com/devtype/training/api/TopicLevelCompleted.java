package com.devtype.training.api;

import java.time.Instant;

public record TopicLevelCompleted(
    Long userId,
    String languageSlug,
    String topicKey,
    String level,
    Instant timestamp
) {}
