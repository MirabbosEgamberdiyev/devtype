package com.devtype.content.application;

import com.devtype.content.api.LanguageCatalog;
import com.devtype.content.domain.LanguageEntity;
import com.devtype.content.domain.LanguageRepository;
import com.devtype.content.domain.LanguageStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class LanguageCatalogImpl implements LanguageCatalog {

    private final LanguageRepository languageRepository;
    private final ObjectMapper objectMapper;

    public LanguageCatalogImpl(LanguageRepository languageRepository, ObjectMapper objectMapper) {
        this.languageRepository = languageRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPublishedLanguageSlugs() {
        return languageRepository.findByStatusOrderBySortOrderAsc(LanguageStatus.PUBLISHED)
                .stream()
                .map(LanguageEntity::getSlug)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLanguagePublished(String slug) {
        return languageRepository.findBySlug(slug.toLowerCase().trim())
                .map(l -> l.getStatus() == LanguageStatus.PUBLISHED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LanguageView> getLanguage(String slug) {
        return languageRepository.findBySlug(slug.toLowerCase().trim())
                .map(l -> {
                    List<String> patterns = Collections.emptyList();
                    try {
                        patterns = objectMapper.readValue(l.getTokenPatterns(), new TypeReference<List<String>>() {});
                    } catch (Exception ignored) {
                    }
                    return new LanguageView(
                            l.getSlug(),
                            l.getName(),
                            l.getStatus().name(),
                            l.getIndentSize(),
                            l.getIndentStyle().name(),
                            patterns
                    );
                });
    }
}
