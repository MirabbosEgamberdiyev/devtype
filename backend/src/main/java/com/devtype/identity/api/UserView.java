package com.devtype.identity.api;

public record UserView(
    Long id,
    String username,
    String email,
    String role,
    boolean emailVerified,
    boolean blocked,
    String primaryLanguage,
    String timezone
) {
    public UserView(Long id, String username, String email, String role, boolean emailVerified, boolean blocked, String primaryLanguage) {
        this(id, username, email, role, emailVerified, blocked, primaryLanguage, "UTC");
    }
}
