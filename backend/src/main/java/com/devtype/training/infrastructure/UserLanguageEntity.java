package com.devtype.training.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_languages", schema = "training")
@IdClass(UserLanguageKey.class)
public class UserLanguageEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "language_id", nullable = false)
    private Short languageId;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "start_level", nullable = false, length = 16)
    private String startLevel = "BEGINNER";

    @Column(name = "placement_done", nullable = false)
    private boolean placementDone = false;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt = Instant.now();

    public UserLanguageEntity() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Short getLanguageId() { return languageId; }
    public void setLanguageId(Short languageId) { this.languageId = languageId; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getStartLevel() { return startLevel; }
    public void setStartLevel(String startLevel) { this.startLevel = startLevel; }

    public boolean isPlacementDone() { return placementDone; }
    public void setPlacementDone(boolean placementDone) { this.placementDone = placementDone; }

    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
