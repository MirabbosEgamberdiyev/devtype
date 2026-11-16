package com.devtype.training.domain;

import java.util.List;

/**
 * Language formatting and pattern configuration.
 */
public record LanguageConfig(
        String indentStyle,
        int indentSize,
        List<String> tokenPatterns
) {
    public LanguageConfig {
        if (indentStyle == null) indentStyle = "SPACE";
        if (indentSize <= 0) indentSize = 2;
        if (tokenPatterns == null) tokenPatterns = List.of();
    }

    public static LanguageConfig defaultConfig() {
        return new LanguageConfig("SPACE", 2, List.of());
    }
}
