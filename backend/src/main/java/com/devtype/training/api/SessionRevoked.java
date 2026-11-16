package com.devtype.training.api;

import java.time.Instant;
import java.util.UUID;

public record SessionRevoked(
    UUID sessionId,
    Long userId,
    String reason,
    Instant timestamp
) {}
