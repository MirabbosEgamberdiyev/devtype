package com.devtype.training.infrastructure;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SessionExerciseKey implements Serializable {

    private UUID sessionId;
    private short seq;

    public SessionExerciseKey() {
    }

    public SessionExerciseKey(UUID sessionId, short seq) {
        this.sessionId = sessionId;
        this.seq = seq;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public short getSeq() {
        return seq;
    }

    public void setSeq(short seq) {
        this.seq = seq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionExerciseKey that)) return false;
        return seq == that.seq && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, seq);
    }
}
