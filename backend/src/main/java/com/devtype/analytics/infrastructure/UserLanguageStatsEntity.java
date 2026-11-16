package com.devtype.analytics.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(schema = "analytics", name = "user_language_stats")
@IdClass(UserLanguageStatsKey.class)
public class UserLanguageStatsEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "language_id", nullable = false)
    private short languageId;

    @Id
    @Column(name = "mode_group", nullable = false, length = 8)
    private String modeGroup;

    @Column(name = "sessions_cnt", nullable = false)
    private int sessionsCnt = 0;

    @Column(name = "total_time_ms", nullable = false)
    private long totalTimeMs = 0L;

    @Column(name = "total_chars", nullable = false)
    private long totalChars = 0L;

    @Column(name = "sum_wpm", nullable = false, precision = 14, scale = 2)
    private BigDecimal sumWpm = BigDecimal.ZERO;

    @Column(name = "sum_cpm", nullable = false, precision = 14, scale = 2)
    private BigDecimal sumCpm = BigDecimal.ZERO;

    @Column(name = "max_wpm", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxWpm = BigDecimal.ZERO;

    @Column(name = "correct_keys", nullable = false)
    private long correctKeys = 0L;

    @Column(name = "total_keys", nullable = false)
    private long totalKeys = 0L;

    @Column(name = "error_cnt", nullable = false)
    private long errorCnt = 0L;

    @Column(name = "special_correct", nullable = false)
    private long specialCorrect = 0L;

    @Column(name = "special_total", nullable = false)
    private long specialTotal = 0L;

    @Column(name = "recent_wpm", precision = 6, scale = 2)
    private BigDecimal recentWpm;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserLanguageStatsEntity() {}

    public UserLanguageStatsEntity(Long userId, short languageId, String modeGroup) {
        this.userId = userId;
        this.languageId = languageId;
        this.modeGroup = modeGroup;
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

    public String getModeGroup() {
        return modeGroup;
    }

    public void setModeGroup(String modeGroup) {
        this.modeGroup = modeGroup;
    }

    public int getSessionsCnt() {
        return sessionsCnt;
    }

    public void setSessionsCnt(int sessionsCnt) {
        this.sessionsCnt = sessionsCnt;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public long getTotalChars() {
        return totalChars;
    }

    public void setTotalChars(long totalChars) {
        this.totalChars = totalChars;
    }

    public BigDecimal getSumWpm() {
        return sumWpm;
    }

    public void setSumWpm(BigDecimal sumWpm) {
        this.sumWpm = sumWpm;
    }

    public BigDecimal getSumCpm() {
        return sumCpm;
    }

    public void setSumCpm(BigDecimal sumCpm) {
        this.sumCpm = sumCpm;
    }

    public BigDecimal getMaxWpm() {
        return maxWpm;
    }

    public void setMaxWpm(BigDecimal maxWpm) {
        this.maxWpm = maxWpm;
    }

    public long getCorrectKeys() {
        return correctKeys;
    }

    public void setCorrectKeys(long correctKeys) {
        this.correctKeys = correctKeys;
    }

    public long getTotalKeys() {
        return totalKeys;
    }

    public void setTotalKeys(long totalKeys) {
        this.totalKeys = totalKeys;
    }

    public long getErrorCnt() {
        return errorCnt;
    }

    public void setErrorCnt(long errorCnt) {
        this.errorCnt = errorCnt;
    }

    public long getSpecialCorrect() {
        return specialCorrect;
    }

    public void setSpecialCorrect(long specialCorrect) {
        this.specialCorrect = specialCorrect;
    }

    public long getSpecialTotal() {
        return specialTotal;
    }

    public void setSpecialTotal(long specialTotal) {
        this.specialTotal = specialTotal;
    }

    public BigDecimal getRecentWpm() {
        return recentWpm;
    }

    public void setRecentWpm(BigDecimal recentWpm) {
        this.recentWpm = recentWpm;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
