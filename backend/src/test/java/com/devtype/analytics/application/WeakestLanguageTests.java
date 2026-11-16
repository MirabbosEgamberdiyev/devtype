package com.devtype.analytics.application;

import com.devtype.analytics.infrastructure.UserLanguageStatsEntity;
import com.devtype.analytics.infrastructure.UserLanguageStatsRepository;
import com.devtype.content.api.LanguageCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

public class WeakestLanguageTests {

    private final UserLanguageStatsRepository repository = Mockito.mock(UserLanguageStatsRepository.class);
    private final LanguageCatalog languageCatalog = Mockito.mock(LanguageCatalog.class);
    private final WeakestLanguageService service = new WeakestLanguageService(repository, languageCatalog);

    @Test
    @Tag("AC-AN-6")
    @DisplayName("AC-AN-6: Weakest language identified when recent_wpm ratio < 0.75 vs median")
    void testWeakestLanguageIdentified() {
        Long userId = 10L;

        given(languageCatalog.getPublishedLanguageSlugs()).willReturn(List.of("java", "python", "javascript"));
        given(languageCatalog.findLanguageIdBySlug("java")).willReturn(Optional.of((short) 1));
        given(languageCatalog.findLanguageIdBySlug("python")).willReturn(Optional.of((short) 2));
        given(languageCatalog.findLanguageIdBySlug("javascript")).willReturn(Optional.of((short) 3));

        // Java: 60 WPM, 15 sessions
        UserLanguageStatsEntity javaStats = new UserLanguageStatsEntity(userId, (short) 1, "CORE");
        javaStats.setSessionsCnt(15);
        javaStats.setRecentWpm(BigDecimal.valueOf(60.0));

        // Python: 65 WPM, 20 sessions
        UserLanguageStatsEntity pyStats = new UserLanguageStatsEntity(userId, (short) 2, "CORE");
        pyStats.setSessionsCnt(20);
        pyStats.setRecentWpm(BigDecimal.valueOf(65.0));

        // JS: 40 WPM, 12 sessions. (Median of 40, 60, 65 is 60. Ratio for JS = 40 / 60 = 0.667 < 0.75)
        UserLanguageStatsEntity jsStats = new UserLanguageStatsEntity(userId, (short) 3, "CORE");
        jsStats.setSessionsCnt(12);
        jsStats.setRecentWpm(BigDecimal.valueOf(40.0));

        given(repository.findByUserIdAndModeGroup(userId, "CORE"))
                .willReturn(List.of(javaStats, pyStats, jsStats));

        var res = service.findWeakestLanguage(userId);
        assertThat(res).isPresent();
        assertThat(res.get().languageSlug()).isEqualTo("javascript");
        assertThat(res.get().ratio()).isLessThan(0.75);
    }

    @Test
    @Tag("AC-AN-6")
    @DisplayName("AC-AN-6: No weakest language if all languages have ratio >= 0.75")
    void testNoWeakestLanguageWhenAboveThreshold() {
        Long userId = 10L;

        given(languageCatalog.getPublishedLanguageSlugs()).willReturn(List.of("java", "python"));
        given(languageCatalog.findLanguageIdBySlug("java")).willReturn(Optional.of((short) 1));
        given(languageCatalog.findLanguageIdBySlug("python")).willReturn(Optional.of((short) 2));

        UserLanguageStatsEntity javaStats = new UserLanguageStatsEntity(userId, (short) 1, "CORE");
        javaStats.setSessionsCnt(15);
        javaStats.setRecentWpm(BigDecimal.valueOf(55.0));

        UserLanguageStatsEntity pyStats = new UserLanguageStatsEntity(userId, (short) 2, "CORE");
        pyStats.setSessionsCnt(20);
        pyStats.setRecentWpm(BigDecimal.valueOf(60.0));

        given(repository.findByUserIdAndModeGroup(userId, "CORE"))
                .willReturn(List.of(javaStats, pyStats));

        var res = service.findWeakestLanguage(userId);
        assertThat(res).isEmpty();
    }
}
