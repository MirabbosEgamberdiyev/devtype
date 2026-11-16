package com.devtype.identity.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(schema = "identity", name = "user_settings")
public class UserSettingsEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "settings", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String settings = "{}";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserSettingsEntity() {}

    public UserSettingsEntity(Long userId, String settings) {
        this.userId = userId;
        this.settings = (settings != null && !settings.isBlank()) ? settings : "{}";
        this.updatedAt = Instant.now();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
