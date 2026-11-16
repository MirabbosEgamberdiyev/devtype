package com.devtype.identity.api;

import java.time.Instant;

public record UserBlocked(
    Long userId,
    String reason,
    Instant timestamp
) {}
