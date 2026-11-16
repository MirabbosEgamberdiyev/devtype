package com.devtype.training.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class UserTopicProgressKey implements Serializable {

    private Long userId;
    private Integer topicId;
    private String difficulty;

    public UserTopicProgressKey() {}

    public UserTopicProgressKey(Long userId, Integer topicId, String difficulty) {
        this.userId = userId;
        this.topicId = topicId;
        this.difficulty = difficulty;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTopicProgressKey that = (UserTopicProgressKey) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(topicId, that.topicId) &&
                Objects.equals(difficulty, that.difficulty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, topicId, difficulty);
    }
}
