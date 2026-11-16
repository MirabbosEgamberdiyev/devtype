package com.devtype.content.application;

import com.devtype.content.domain.LanguageEntity;
import com.devtype.content.domain.LanguageRepository;
import com.devtype.content.domain.TopicRepository;
import com.devtype.content.presentation.ContentDtos.TopicResponse;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetLanguageTopicsUseCase {

    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;

    public GetLanguageTopicsUseCase(LanguageRepository languageRepository, TopicRepository topicRepository) {
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> execute(String languageSlug) {
        LanguageEntity language = languageRepository.findBySlug(languageSlug.toLowerCase().trim())
                .orElseThrow(() -> new DomainException("LANGUAGE_NOT_FOUND", "Language not found: " + languageSlug, HttpStatus.NOT_FOUND));

        return topicRepository.findByLanguageIdOrderBySortOrderAsc(language.getId())
                .stream()
                .map(t -> new TopicResponse(
                        t.getSlug(),
                        t.getName(),
                        t.getDescription(),
                        t.getSortOrder(),
                        t.getStatus()
                ))
                .toList();
    }
}
