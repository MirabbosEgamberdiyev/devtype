package com.devtype.gamification.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class PersonalRecordKey implements Serializable {
    private Long userId;
    private short languageId;
    private String mode;
    private short durationLimitS;

    public PersonalRecordKey() {}

    public PersonalRecordKey(Long userId, short languageId, String mode, short durationLimitS) {
        this.userId = userId;
        this.languageId = languageId;
        this.mode = mode;
        this.durationLimitS = durationLimitS;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public short getLanguageId() {
        return languageId;
    }

    public void setLanguageId(short languageId) {
        this.languageId = languageId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public short getDurationLimitS() {
        return durationLimitS;
    }

    public void setDurationLimitS(short durationLimitS) {
        this.durationLimitS = durationLimitS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalRecordKey that = (PersonalRecordKey) o;
        return languageId == that.languageId &&
                durationLimitS == that.durationLimitS &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(mode, that.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, languageId, mode, durationLimitS);
    }
}
