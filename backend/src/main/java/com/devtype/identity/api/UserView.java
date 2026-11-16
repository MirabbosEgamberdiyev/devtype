package com.devtype.identity.api;

public record UserView(
    Long id,
    String username,
    String email,
    String role,
    boolean emailVerified,
    boolean blocked,
    String primaryLanguage
) {}
