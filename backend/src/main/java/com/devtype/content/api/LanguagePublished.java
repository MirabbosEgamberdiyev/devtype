package com.devtype.content.api;

import java.time.Instant;

public record LanguagePublished(
    String languageSlug,
    Instant timestamp
) {}
