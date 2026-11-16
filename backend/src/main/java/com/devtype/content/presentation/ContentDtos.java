package com.devtype.content.presentation;

import com.devtype.content.domain.DifficultyLevel;
import com.devtype.content.domain.ExerciseStatus;
import com.devtype.content.domain.LanguageStatus;
import com.devtype.content.domain.TopicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class ContentDtos {

    private ContentDtos() {
    }

    public record LanguageResponse(
            String slug,
            String name,
            String grammarId,
            String indentStyle,
            short indentSize,
            String keywordCase,
            short release,
            LanguageStatus status,
            short sortOrder
    ) {}

    public record TopicResponse(
            String slug,
            String name,
            String description,
            short sortOrder,
            TopicStatus status
    ) {}

    public record ExerciseResponse(
            Long id,
            UUID exerciseKey,
            int version,
            String languageSlug,
            String topicSlug,
            DifficultyLevel difficulty,
            String title,
            String note,
            String code,
            int charCount,
            int lineCount,
            boolean rankedPool,
            ExerciseStatus status
    ) {}

    public record CreateExerciseRequest(
            @NotBlank String languageSlug,
            String topicSlug,
            @NotNull DifficultyLevel difficulty,
            @NotBlank @Size(max = 150) String title,
            @Size(max = 400) String note,
            @NotBlank String code,
            boolean rankedPool
    ) {}
}
