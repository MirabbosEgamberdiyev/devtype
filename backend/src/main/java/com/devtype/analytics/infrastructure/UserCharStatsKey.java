package com.devtype.analytics.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class UserCharStatsKey implements Serializable {
    private Long userId;
    private short languageId;
    private String token;

    public UserCharStatsKey() {}

    public UserCharStatsKey(Long userId, short languageId, String token) {
        this.userId = userId;
        this.languageId = languageId;
        this.token = token;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCharStatsKey that = (UserCharStatsKey) o;
        return languageId == that.languageId &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, languageId, token);
    }
}
