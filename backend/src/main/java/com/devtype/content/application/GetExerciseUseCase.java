package com.devtype.content.application;

import com.devtype.content.domain.*;
import com.devtype.content.presentation.ContentDtos.ExerciseResponse;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetExerciseUseCase {

    private final ExerciseRepository exerciseRepository;
    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;

    public GetExerciseUseCase(
            ExerciseRepository exerciseRepository,
            LanguageRepository languageRepository,
            TopicRepository topicRepository) {
        this.exerciseRepository = exerciseRepository;
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true)
    public ExerciseResponse execute(UUID exerciseKey) {
        ExerciseEntity exercise = exerciseRepository.findByExerciseKeyAndStatus(exerciseKey, ExerciseStatus.PUBLISHED)
                .orElseThrow(() -> new DomainException("EXERCISE_NOT_FOUND", "Published exercise not found: " + exerciseKey, HttpStatus.NOT_FOUND));

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
