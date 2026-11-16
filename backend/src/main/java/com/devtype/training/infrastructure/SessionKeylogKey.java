package com.devtype.training.infrastructure;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class SessionKeylogKey implements Serializable {

    private UUID sessionId;
    private Instant startedAt;

    public SessionKeylogKey() {}

    public SessionKeylogKey(UUID sessionId, Instant startedAt) {
        this.sessionId = sessionId;
        this.startedAt = startedAt;
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionKeylogKey that = (SessionKeylogKey) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(startedAt, that.startedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, startedAt);
    }
}
