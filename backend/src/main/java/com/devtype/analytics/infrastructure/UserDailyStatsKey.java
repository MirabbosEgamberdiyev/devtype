package com.devtype.analytics.infrastructure;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class UserDailyStatsKey implements Serializable {
    private Long userId;
    private LocalDate day;
    private short languageId;
    private String modeGroup;

    public UserDailyStatsKey() {}

    public UserDailyStatsKey(Long userId, LocalDate day, short languageId, String modeGroup) {
        this.userId = userId;
        this.day = day;
        this.languageId = languageId;
        this.modeGroup = modeGroup;
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

    public short getLanguageId() {
        return languageId;
    }

    public void setLanguageId(short languageId) {
        this.languageId = languageId;
    }

    public String getModeGroup() {
        return modeGroup;
    }

    public void setModeGroup(String modeGroup) {
        this.modeGroup = modeGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDailyStatsKey that = (UserDailyStatsKey) o;
        return languageId == that.languageId &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(day, that.day) &&
                Objects.equals(modeGroup, that.modeGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, day, languageId, modeGroup);
    }
}
