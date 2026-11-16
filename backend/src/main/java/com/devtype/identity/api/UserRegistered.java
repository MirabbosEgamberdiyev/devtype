package com.devtype.identity.api;

import java.time.Instant;

public record UserRegistered(
    Long userId,
    String username,
    String email,
    String locale,
    Instant timestamp
) {}
