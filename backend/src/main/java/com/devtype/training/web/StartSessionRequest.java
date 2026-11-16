package com.devtype.training.web;

import java.util.UUID;

public record StartSessionRequest(
        String language,
        String mode,
        String topic,
        String difficulty,
        UUID exerciseKey,
        Integer duration,
        Boolean accuracyMaster,
        String specialCategory,
        Long challengeId,
        Long competitionId,
        Boolean placement,
        String inputDevice,
        Boolean strictIndent
) {}
