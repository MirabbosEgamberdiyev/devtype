package com.devtype.training.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_topic_progress", schema = "training")
@IdClass(UserTopicProgressKey.class)
public class UserTopicProgressEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "topic_id", nullable = false)
    private Integer topicId;

    @Id
    @Column(name = "difficulty", nullable = false, length = 16)
    private String difficulty;

    @Column(name = "state", nullable = false, length = 16)
    private String state = "AVAILABLE";

    @Column(name = "completed_cnt", nullable = false)
    private int completedCnt = 0;

    @Column(name = "mastered_cnt", nullable = false)
    private int masteredCnt = 0;

    @Column(name = "attempts_cnt", nullable = false)
    private int attemptsCnt = 0;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserTopicProgressEntity() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getCompletedCnt() { return completedCnt; }
    public void setCompletedCnt(int completedCnt) { this.completedCnt = completedCnt; }

    public int getMasteredCnt() { return masteredCnt; }
    public void setMasteredCnt(int masteredCnt) { this.masteredCnt = masteredCnt; }

    public int getAttemptsCnt() { return attemptsCnt; }
    public void setAttemptsCnt(int attemptsCnt) { this.attemptsCnt = attemptsCnt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
