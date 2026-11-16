package com.devtype.content.application;

import com.devtype.content.api.LanguageCatalog;
import com.devtype.content.domain.LanguageEntity;
import com.devtype.content.domain.LanguageRepository;
import com.devtype.content.domain.LanguageStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LanguageCatalogImpl implements LanguageCatalog {

    private final LanguageRepository languageRepository;

    public LanguageCatalogImpl(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPublishedLanguageSlugs() {
        return languageRepository.findByStatusOrderBySortOrderAsc(LanguageStatus.PUBLISHED).stream()
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
                .map(l -> new LanguageView(
                        l.getSlug(),
                        l.getName(),
                        l.getStatus().name(),
                        l.getIndentSize(),
                        l.getIndentStyle().name(),
                        l.getTokenPatterns() != null ? List.of(l.getTokenPatterns()) : List.of()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Short> findLanguageIdBySlug(String slug) {
        return languageRepository.findBySlug(slug.toLowerCase().trim())
                .map(LanguageEntity::getId);
    }
}
