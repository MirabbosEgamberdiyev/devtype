package com.devtype.training.application;

import java.util.UUID;

public record CompleteSessionCommand(
        UUID sessionId,
        Long currentUserId,
        String completeToken,
        String keylog,
        int clientElapsedMs,
        Double clientWpm,
        Double clientAccuracy
) {}
