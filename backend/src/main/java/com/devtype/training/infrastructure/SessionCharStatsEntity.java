package com.devtype.training.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_char_stats", schema = "training")
@IdClass(SessionCharStatsKey.class)
public class SessionCharStatsEntity {

    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Id
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Id
    @Column(name = "token", nullable = false, length = 12)
    private String token;

    @Column(name = "is_pattern", nullable = false)
    private boolean isPattern = false;

    @Column(name = "expected_cnt", nullable = false)
    private int expectedCnt;

    @Column(name = "error_cnt", nullable = false)
    private int errorCnt;

    @Column(name = "total_ms", nullable = false)
    private int totalMs;

    @Column(name = "timed_cnt", nullable = false)
    private int timedCnt;

    public SessionCharStatsEntity() {}

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isPattern() { return isPattern; }
    public void setPattern(boolean pattern) { isPattern = pattern; }

    public int getExpectedCnt() { return expectedCnt; }
    public void setExpectedCnt(int expectedCnt) { this.expectedCnt = expectedCnt; }

    public int getErrorCnt() { return errorCnt; }
    public void setErrorCnt(int errorCnt) { this.errorCnt = errorCnt; }

    public int getTotalMs() { return totalMs; }
    public void setTotalMs(int totalMs) { this.totalMs = totalMs; }

    public int getTimedCnt() { return timedCnt; }
    public void setTimedCnt(int timedCnt) { this.timedCnt = timedCnt; }
}
