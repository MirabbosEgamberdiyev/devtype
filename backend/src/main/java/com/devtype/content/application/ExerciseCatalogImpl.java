package com.devtype.content.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.content.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExerciseCatalogImpl implements ExerciseCatalog {

    private final ExerciseRepository exerciseRepository;
    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;

    public ExerciseCatalogImpl(
            ExerciseRepository exerciseRepository,
            LanguageRepository languageRepository,
            TopicRepository topicRepository) {
        this.exerciseRepository = exerciseRepository;
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExerciseView> findByKey(String key) {
        try {
            UUID uuid = UUID.fromString(key);
            return exerciseRepository.findByExerciseKeyAndStatus(uuid, ExerciseStatus.PUBLISHED)
                    .map(this::toView);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseView> findByTopicAndLevel(String topicKey, String level) {
        // topicKey format: languageSlug:topicSlug or numeric id
        String[] parts = topicKey.split(":");
        if (parts.length != 2) {
            return Collections.emptyList();
        }
        String langSlug = parts[0];
        String topicSlug = parts[1];

        Optional<LanguageEntity> lang = languageRepository.findBySlug(langSlug);
        if (lang.isEmpty()) return Collections.emptyList();

        Optional<TopicEntity> topic = topicRepository.findByLanguageIdAndSlug(lang.get().getId(), topicSlug);
        if (topic.isEmpty()) return Collections.emptyList();

        try {
            DifficultyLevel difficulty = DifficultyLevel.valueOf(level.toUpperCase());
            return exerciseRepository.findByLanguageIdAndTopicIdAndDifficultyAndStatus(
                    lang.get().getId(),
                    topic.get().getId(),
                    difficulty,
                    ExerciseStatus.PUBLISHED
            ).stream().map(this::toView).toList();
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseView> findRankedPool(String languageSlug) {
        Optional<LanguageEntity> lang = languageRepository.findBySlug(languageSlug.toLowerCase().trim());
        if (lang.isEmpty()) return Collections.emptyList();

        return exerciseRepository.findByLanguageIdAndRankedPoolTrueAndStatus(lang.get().getId(), ExerciseStatus.PUBLISHED)
                .stream()
                .map(this::toView)
                .toList();
    }

    private ExerciseView toView(ExerciseEntity e) {
        String langSlug = languageRepository.findById(e.getLanguageId()).map(LanguageEntity::getSlug).orElse("unknown");
        String topicSlug = e.getTopicId() != null
                ? topicRepository.findById(e.getTopicId()).map(TopicEntity::getSlug).orElse(null)
                : null;

        return new ExerciseView(
                e.getId(),
                e.getExerciseKey().toString(),
                langSlug,
                topicSlug,
                e.getDifficulty().name(),
                e.getCode(),
                e.getTitle(),
                e.getCharCount(),
                e.isRankedPool()
        );
    }
}
