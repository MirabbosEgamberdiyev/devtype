package com.devtype.training.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StartSessionResult(
        UUID sessionId,
        Instant startedAt,
        Instant expiresAt,
        String mode,
        String errorMode,
        Integer durationLimitS,
        LanguageDto language,
        List<ExerciseDto> exercises,
        String completeToken
) {
    public record LanguageDto(
            String slug,
            String grammarId,
            IndentDto indent,
            List<String> tokenPatterns
    ) {}

    public record IndentDto(
            String style,
            int size
    ) {}

    public record ExerciseDto(
            int seq,
            UUID exerciseKey,
            String title,
            String note,
            String difficulty,
            String code,
            int charCount
    ) {}
}
