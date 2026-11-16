package com.devtype.gamification.infrastructure;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StreakFreezeUsageKey implements Serializable {
    private Long userId;
    private LocalDate day;

    public StreakFreezeUsageKey() {}

    public StreakFreezeUsageKey(Long userId, LocalDate day) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreakFreezeUsageKey that = (StreakFreezeUsageKey) o;
        return Objects.equals(userId, that.userId) && Objects.equals(day, that.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, day);
    }
}
