package com.devtype.gamification.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(schema = "gamification", name = "streak_freeze_usages")
@IdClass(StreakFreezeUsageKey.class)
public class StreakFreezeUsageEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "day", nullable = false)
    private LocalDate day;

    public StreakFreezeUsageEntity() {}

    public StreakFreezeUsageEntity(Long userId, LocalDate day) {
        this.userId = userId;
        this.day = day;
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
}
