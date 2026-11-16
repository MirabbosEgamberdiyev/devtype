package com.devtype.training.api;

public interface TopicProgressQuery {
    boolean isLevelUnlocked(Long userId, String languageSlug, String topicKey, String level);
}
