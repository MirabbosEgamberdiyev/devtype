package com.devtype.content.application;

import com.devtype.content.domain.*;
import com.devtype.content.presentation.ContentDtos.CreateExerciseRequest;
import com.devtype.content.presentation.ContentDtos.ExerciseResponse;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CreateExerciseUseCase {

    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseValidator exerciseValidator;

    public CreateExerciseUseCase(
            LanguageRepository languageRepository,
            TopicRepository topicRepository,
            ExerciseRepository exerciseRepository,
            ExerciseValidator exerciseValidator) {
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
        this.exerciseRepository = exerciseRepository;
        this.exerciseValidator = exerciseValidator;
    }

    @Transactional
    public ExerciseResponse execute(CreateExerciseRequest request, Long createdBy) {
        LanguageEntity language = languageRepository.findBySlug(request.languageSlug().toLowerCase().trim())
                .orElseThrow(() -> new DomainException("LANGUAGE_NOT_FOUND", "Language not found: " + request.languageSlug(), HttpStatus.NOT_FOUND));

        Integer topicId = null;
        if (request.topicSlug() != null && !request.topicSlug().isBlank()) {
            TopicEntity topic = topicRepository.findByLanguageIdAndSlug(language.getId(), request.topicSlug().toLowerCase().trim())
                    .orElseThrow(() -> new DomainException("TOPIC_NOT_FOUND", "Topic not found: " + request.topicSlug(), HttpStatus.NOT_FOUND));
            topicId = topic.getId();
        }

        // Validate code syntax and constraints (BL-EM-1..4)
        ExerciseValidator.ValidationResult val = exerciseValidator.validate(request.code(), request.difficulty(), language);

        // Check duplicate code_hash within same language (BL-EM-4)
        if (exerciseRepository.existsByLanguageIdAndCodeHash(language.getId(), val.codeHash())) {
            throw new DomainException("EXERCISE_DUPLICATE", "An exercise with this exact code already exists in " + language.getSlug(), HttpStatus.CONFLICT);
        }

        ExerciseEntity exercise = new ExerciseEntity(
                UUID.randomUUID(),
                1,
                language.getId(),
                topicId,
                request.difficulty(),
                topicId == null ? ExerciseType.SPECIAL_CHARS : ExerciseType.SNIPPET,
                request.title(),
                request.code(),
                val.codeHash(),
                val.charCount(),
                val.lineCount(),
                BigDecimal.ZERO,
                val.nestingDepth(),
                createdBy
        );
        exercise.setNote(request.note());
        exercise.setRankedPool(request.rankedPool());

        exercise = exerciseRepository.save(exercise);

        return new ExerciseResponse(
                exercise.getId(),
                exercise.getExerciseKey(),
                exercise.getVersion(),
                language.getSlug(),
                request.topicSlug(),
                exercise.getDifficulty(),
                exercise.getTitle(),
                exercise.getNote(),
                exercise.getCode(),
                exercise.getCharCount(),
                exercise.getLineCount(),
                exercise.isRankedPool(),
                exercise.getStatus()
        );
    }
}
