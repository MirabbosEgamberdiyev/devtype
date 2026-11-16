package com.devtype.training.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_keylogs", schema = "training")
@IdClass(SessionKeylogKey.class)
public class SessionKeylogEntity {

    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Id
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "events", nullable = false)
    private byte[] events;

    @Column(name = "event_count", nullable = false)
    private int eventCount;

    @Column(name = "keep_forever", nullable = false)
    private boolean keepForever = false;

    public SessionKeylogEntity() {}

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public byte[] getEvents() { return events; }
    public void setEvents(byte[] events) { this.events = events; }

    public int getEventCount() { return eventCount; }
    public void setEventCount(int eventCount) { this.eventCount = eventCount; }

    public boolean isKeepForever() { return keepForever; }
    public void setKeepForever(boolean keepForever) { this.keepForever = keepForever; }
}
