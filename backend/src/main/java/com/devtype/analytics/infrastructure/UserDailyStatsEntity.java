package com.devtype.analytics.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(schema = "analytics", name = "user_daily_stats")
@IdClass(UserDailyStatsKey.class)
public class UserDailyStatsEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Id
    @Column(name = "language_id", nullable = false)
    private short languageId;

    @Id
    @Column(name = "mode_group", nullable = false, length = 8)
    private String modeGroup;

    @Column(name = "sessions_cnt", nullable = false)
    private int sessionsCnt = 0;

    @Column(name = "time_ms", nullable = false)
    private long timeMs = 0L;

    @Column(name = "chars", nullable = false)
    private long chars = 0L;

    @Column(name = "sum_wpm", nullable = false, precision = 12, scale = 2)
    private BigDecimal sumWpm = BigDecimal.ZERO;

    @Column(name = "max_wpm", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxWpm = BigDecimal.ZERO;

    @Column(name = "correct_keys", nullable = false)
    private long correctKeys = 0L;

    @Column(name = "total_keys", nullable = false)
    private long totalKeys = 0L;

    @Column(name = "xp", nullable = false)
    private int xp = 0;

    public UserDailyStatsEntity() {}

    public UserDailyStatsEntity(Long userId, LocalDate day, short languageId, String modeGroup) {
        this.userId = userId;
        this.day = day;
        this.languageId = languageId;
        this.modeGroup = modeGroup;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
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

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public long getChars() {
        return chars;
    }

    public void setChars(long chars) {
        this.chars = chars;
    }

    public BigDecimal getSumWpm() {
        return sumWpm;
    }

    public void setSumWpm(BigDecimal sumWpm) {
        this.sumWpm = sumWpm;
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

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}
