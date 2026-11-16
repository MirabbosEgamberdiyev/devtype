package com.devtype.analytics.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "analytics", name = "processed_sessions")
public class ProcessedSessionEntity {

    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();

    public ProcessedSessionEntity() {}

    public ProcessedSessionEntity(UUID sessionId) {
        this.sessionId = sessionId;
        this.processedAt = Instant.now();
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
