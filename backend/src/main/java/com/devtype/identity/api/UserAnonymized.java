package com.devtype.identity.api;

import java.time.Instant;

public record UserAnonymized(
    Long userId,
    Instant timestamp
) {}
