package com.devtype.gamification.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "gamification", name = "personal_records")
@IdClass(PersonalRecordKey.class)
public class PersonalRecordEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "language_id", nullable = false)
    private short languageId;

    @Id
    @Column(name = "mode", nullable = false, length = 16)
    private String mode;

    @Id
    @Column(name = "duration_limit_s", nullable = false)
    private short durationLimitS = 0;

    @Column(name = "best_wpm", nullable = false, precision = 6, scale = 2)
    private BigDecimal bestWpm;

    @Column(name = "accuracy", nullable = false, precision = 5, scale = 2)
    private BigDecimal accuracy;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "achieved_at", nullable = false)
    private Instant achievedAt = Instant.now();

    public PersonalRecordEntity() {}

    public PersonalRecordEntity(Long userId, short languageId, String mode, short durationLimitS,
                                BigDecimal bestWpm, BigDecimal accuracy, UUID sessionId, Instant achievedAt) {
        this.userId = userId;
        this.languageId = languageId;
        this.mode = mode;
        this.durationLimitS = durationLimitS;
        this.bestWpm = bestWpm;
        this.accuracy = accuracy;
        this.sessionId = sessionId;
        this.achievedAt = achievedAt != null ? achievedAt : Instant.now();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public short getLanguageId() {
        return languageId;
    }

    public void setLanguageId(short languageId) {
        this.languageId = languageId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public short getDurationLimitS() {
        return durationLimitS;
    }

    public void setDurationLimitS(short durationLimitS) {
        this.durationLimitS = durationLimitS;
    }

    public BigDecimal getBestWpm() {
        return bestWpm;
    }

    public void setBestWpm(BigDecimal bestWpm) {
        this.bestWpm = bestWpm;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(Instant achievedAt) {
        this.achievedAt = achievedAt;
    }
}
