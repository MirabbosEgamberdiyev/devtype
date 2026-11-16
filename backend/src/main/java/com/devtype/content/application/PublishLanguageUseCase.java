package com.devtype.content.application;

import com.devtype.content.domain.*;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublishLanguageUseCase {

    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;
    private final ExerciseRepository exerciseRepository;

    public PublishLanguageUseCase(
            LanguageRepository languageRepository,
            TopicRepository topicRepository,
            ExerciseRepository exerciseRepository) {
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional
    public void execute(String languageSlug) {
        LanguageEntity language = languageRepository.findBySlug(languageSlug.toLowerCase().trim())
                .orElseThrow(() -> new DomainException("LANGUAGE_NOT_FOUND", "Language not found: " + languageSlug, HttpStatus.NOT_FOUND));

        if (language.getStatus() == LanguageStatus.PUBLISHED) {
            return;
        }

        // Check readiness condition (AC-PL-2)
        List<TopicEntity> publishedTopics = topicRepository.findByLanguageIdAndStatusOrderBySortOrderAsc(language.getId(), TopicStatus.PUBLISHED);
        if (publishedTopics.isEmpty()) {
            throw new DomainException(
                    "LANGUAGE_NOT_READY",
                    "Language '" + languageSlug + "' must have at least one PUBLISHED topic before being published",
                    HttpStatus.CONFLICT
            );
        }

        language.setStatus(LanguageStatus.PUBLISHED);
        languageRepository.save(language);
    }
}
