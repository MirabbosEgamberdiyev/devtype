package com.devtype.content.application;

import com.devtype.content.domain.*;
import com.devtype.content.presentation.ContentDtos.ExerciseResponse;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PublishExerciseUseCase {

    private final ExerciseRepository exerciseRepository;
    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;

    public PublishExerciseUseCase(
            ExerciseRepository exerciseRepository,
            LanguageRepository languageRepository,
            TopicRepository topicRepository) {
        this.exerciseRepository = exerciseRepository;
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public ExerciseResponse execute(UUID exerciseKey, int version, Long reviewerId) {
        ExerciseEntity exercise = exerciseRepository.findByExerciseKeyAndVersion(exerciseKey, version)
                .orElseThrow(() -> new DomainException("EXERCISE_NOT_FOUND", "Exercise not found: " + exerciseKey, HttpStatus.NOT_FOUND));

        if (exercise.getCreatedBy() != null && exercise.getCreatedBy().equals(reviewerId)) {
            throw new DomainException("CANNOT_SELF_REVIEW", "Exercise reviewer must be different from author", HttpStatus.FORBIDDEN);
        }

        exercise.setStatus(ExerciseStatus.PUBLISHED);
        exercise.setReviewedBy(reviewerId);
        exercise.setPublishedAt(Instant.now());

        exercise = exerciseRepository.save(exercise);

        LanguageEntity language = languageRepository.findById(exercise.getLanguageId()).orElseThrow();
        String topicSlug = null;
        if (exercise.getTopicId() != null) {
            topicSlug = topicRepository.findById(exercise.getTopicId()).map(TopicEntity::getSlug).orElse(null);
        }

        return new ExerciseResponse(
                exercise.getId(),
                exercise.getExerciseKey(),
                exercise.getVersion(),
                language.getSlug(),
                topicSlug,
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
