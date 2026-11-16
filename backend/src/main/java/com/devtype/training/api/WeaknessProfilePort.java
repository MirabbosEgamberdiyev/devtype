package com.devtype.training.api;

import java.util.List;

/**
 * SPI: Training moduli analytics modulidagi zaiflik profilini
 * siklik bog'liqliksiz olishi uchun port.
 */
public interface WeaknessProfilePort {
    List<Character> getWeakCharacters(Long userId, String languageSlug);
    List<String> getWeakBigrams(Long userId, String languageSlug);
}
