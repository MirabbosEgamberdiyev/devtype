package com.devtype.training.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_exercise_best", schema = "training")
@IdClass(UserExerciseBestKey.class)
public class UserExerciseBestEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "exercise_key", nullable = false)
    private UUID exerciseKey;

    @Column(name = "best_wpm", nullable = false, precision = 6, scale = 2)
    private BigDecimal bestWpm;

    @Column(name = "best_accuracy", nullable = false, precision = 5, scale = 2)
    private BigDecimal bestAccuracy;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "attempts", nullable = false)
    private int attempts = 1;

    @Column(name = "last_session_id", nullable = false)
    private UUID lastSessionId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserExerciseBestEntity() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public UUID getExerciseKey() { return exerciseKey; }
    public void setExerciseKey(UUID exerciseKey) { this.exerciseKey = exerciseKey; }

    public BigDecimal getBestWpm() { return bestWpm; }
    public void setBestWpm(BigDecimal bestWpm) { this.bestWpm = bestWpm; }

    public BigDecimal getBestAccuracy() { return bestAccuracy; }
    public void setBestAccuracy(BigDecimal bestAccuracy) { this.bestAccuracy = bestAccuracy; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public UUID getLastSessionId() { return lastSessionId; }
    public void setLastSessionId(UUID lastSessionId) { this.lastSessionId = lastSessionId; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
