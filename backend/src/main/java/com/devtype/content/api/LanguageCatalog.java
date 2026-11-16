package com.devtype.content.api;

import java.util.List;
import java.util.Optional;

public interface LanguageCatalog {
    List<String> getPublishedLanguageSlugs();
    boolean isLanguagePublished(String slug);
    Optional<LanguageView> getLanguage(String slug);
    Optional<Short> findLanguageIdBySlug(String slug);

    record LanguageView(
        String slug,
        String name,
        String status,
        int indentSize,
        String indentStyle,
        List<String> tokenPatterns
    ) {}
}
