package com.devtype.training.infrastructure;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class UserExerciseBestKey implements Serializable {

    private Long userId;
    private UUID exerciseKey;

    public UserExerciseBestKey() {}

    public UserExerciseBestKey(Long userId, UUID exerciseKey) {
        this.userId = userId;
        this.exerciseKey = exerciseKey;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public UUID getExerciseKey() { return exerciseKey; }
    public void setExerciseKey(UUID exerciseKey) { this.exerciseKey = exerciseKey; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserExerciseBestKey that = (UserExerciseBestKey) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(exerciseKey, that.exerciseKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, exerciseKey);
    }
}
