package com.devtype.analytics.api;

import java.util.List;

public interface WeaknessProfileProvider {
    WeaknessProfileView getProfile(Long userId, String languageSlug);

    record WeaknessProfileView(
        List<Character> weakCharacters,
        List<String> weakBigrams,
        boolean enoughData
    ) {}
}
