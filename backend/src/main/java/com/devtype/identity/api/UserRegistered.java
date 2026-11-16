package com.devtype.identity.api;

import java.time.Instant;
import java.util.UUID;

public record UserRegistered(
    Long userId,
    String username,
    String email,
    String locale,
    UUID guestId,
    Instant timestamp
) {}
