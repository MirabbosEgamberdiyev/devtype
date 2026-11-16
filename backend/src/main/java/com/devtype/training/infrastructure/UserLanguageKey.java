package com.devtype.training.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class UserLanguageKey implements Serializable {

    private Long userId;
    private Short languageId;

    public UserLanguageKey() {}

    public UserLanguageKey(Long userId, Short languageId) {
        this.userId = userId;
        this.languageId = languageId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Short getLanguageId() { return languageId; }
    public void setLanguageId(Short languageId) { this.languageId = languageId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLanguageKey that = (UserLanguageKey) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(languageId, that.languageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, languageId);
    }
}
