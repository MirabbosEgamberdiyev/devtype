package com.devtype.training.infrastructure;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class SessionCharStatsKey implements Serializable {

    private UUID sessionId;
    private Instant startedAt;
    private String token;

    public SessionCharStatsKey() {}

    public SessionCharStatsKey(UUID sessionId, Instant startedAt, String token) {
        this.sessionId = sessionId;
        this.startedAt = startedAt;
        this.token = token;
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionCharStatsKey that = (SessionCharStatsKey) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(startedAt, that.startedAt) &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, startedAt, token);
    }
}
