package com.devtype.analytics.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class UserLanguageStatsKey implements Serializable {
    private Long userId;
    private short languageId;
    private String modeGroup;

    public UserLanguageStatsKey() {}

    public UserLanguageStatsKey(Long userId, short languageId, String modeGroup) {
        this.userId = userId;
        this.languageId = languageId;
        this.modeGroup = modeGroup;
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
        UserLanguageStatsKey that = (UserLanguageStatsKey) o;
        return languageId == that.languageId &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(modeGroup, that.modeGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, languageId, modeGroup);
    }
}
