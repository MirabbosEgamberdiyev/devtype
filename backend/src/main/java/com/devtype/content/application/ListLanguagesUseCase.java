package com.devtype.content.application;

import com.devtype.content.domain.LanguageEntity;
import com.devtype.content.domain.LanguageRepository;
import com.devtype.content.presentation.ContentDtos.LanguageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListLanguagesUseCase {

    private final LanguageRepository languageRepository;

    public ListLanguagesUseCase(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Transactional(readOnly = true)
    public List<LanguageResponse> execute() {
        return languageRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LanguageResponse toResponse(LanguageEntity entity) {
        return new LanguageResponse(
                entity.getSlug(),
                entity.getName(),
                entity.getGrammarId(),
                entity.getIndentStyle().name(),
                entity.getIndentSize(),
                entity.getKeywordCase().name(),
                entity.getRelease(),
                entity.getStatus(),
                entity.getSortOrder()
        );
    }
}
