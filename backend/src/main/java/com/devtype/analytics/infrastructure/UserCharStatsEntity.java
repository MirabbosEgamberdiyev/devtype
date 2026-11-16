package com.devtype.analytics.infrastructure;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(schema = "analytics", name = "user_char_stats")
@IdClass(UserCharStatsKey.class)
public class UserCharStatsEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "language_id", nullable = false)
    private short languageId;

    @Id
    @Column(name = "token", nullable = false, length = 4)
    private String token;

    @Column(name = "expected_cnt", nullable = false)
    private long expectedCnt = 0L;

    @Column(name = "error_cnt", nullable = false)
    private long errorCnt = 0L;

    @Column(name = "total_ms", nullable = false)
    private long totalMs = 0L;

    @Column(name = "timed_cnt", nullable = false)
    private long timedCnt = 0L;

    @Column(name = "ewma_error", nullable = false, precision = 6, scale = 4)
    private BigDecimal ewmaError = BigDecimal.ZERO;

    @Column(name = "ewma_ms", nullable = false, precision = 8, scale = 2)
    private BigDecimal ewmaMs = BigDecimal.ZERO;

    @Column(name = "observed_sessions", nullable = false)
    private int observedSessions = 0;

    @Column(name = "confusions", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String confusions = "{}";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserCharStatsEntity() {}

    public UserCharStatsEntity(Long userId, short languageId, String token) {
        this.userId = userId;
        this.languageId = languageId;
        this.token = token;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpectedCnt() {
        return expectedCnt;
    }

    public void setExpectedCnt(long expectedCnt) {
        this.expectedCnt = expectedCnt;
    }

    public long getErrorCnt() {
        return errorCnt;
    }

    public void setErrorCnt(long errorCnt) {
        this.errorCnt = errorCnt;
    }

    public long getTotalMs() {
        return totalMs;
    }

    public void setTotalMs(long totalMs) {
        this.totalMs = totalMs;
    }

    public long getTimedCnt() {
        return timedCnt;
    }

    public void setTimedCnt(long timedCnt) {
        this.timedCnt = timedCnt;
    }

    public BigDecimal getEwmaError() {
        return ewmaError;
    }

    public void setEwmaError(BigDecimal ewmaError) {
        this.ewmaError = ewmaError;
    }

    public BigDecimal getEwmaMs() {
        return ewmaMs;
    }

    public void setEwmaMs(BigDecimal ewmaMs) {
        this.ewmaMs = ewmaMs;
    }

    public int getObservedSessions() {
        return observedSessions;
    }

    public void setObservedSessions(int observedSessions) {
        this.observedSessions = observedSessions;
    }

    public String getConfusions() {
        return confusions;
    }

    public void setConfusions(String confusions) {
        this.confusions = confusions;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
