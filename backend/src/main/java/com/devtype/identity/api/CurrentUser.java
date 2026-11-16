package com.devtype.identity.api;

public record CurrentUser(
    Long id,
    String username,
    String role,
    boolean emailVerified
) {}
