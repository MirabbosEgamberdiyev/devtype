package com.devtype.analytics.application;

import com.devtype.analytics.api.LanguageStatsQuery;
import com.devtype.analytics.infrastructure.UserLanguageStatsEntity;
import com.devtype.analytics.infrastructure.UserLanguageStatsRepository;
import com.devtype.content.api.LanguageCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LanguageStatsQueryService implements LanguageStatsQuery {

    private final UserLanguageStatsRepository languageStatsRepository;
    private final LanguageCatalog languageCatalog;

    public LanguageStatsQueryService(
            UserLanguageStatsRepository languageStatsRepository,
            LanguageCatalog languageCatalog
    ) {
        this.languageStatsRepository = languageStatsRepository;
        this.languageCatalog = languageCatalog;
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageStatsView getStats(Long userId, String languageSlug) {
        if (userId == null) {
            return new LanguageStatsView(0, 0.0, 0.0, 0.0, 0);
        }

        short languageId = languageCatalog.findLanguageIdBySlug(languageSlug).orElse((short) 1);
        Optional<UserLanguageStatsEntity> statsOpt = languageStatsRepository
                .findByUserIdAndLanguageIdAndModeGroup(userId, languageId, "CORE");

        if (statsOpt.isEmpty()) {
            return new LanguageStatsView(0, 0.0, 0.0, 0.0, 0);
        }

        UserLanguageStatsEntity s = statsOpt.get();
        double avgAcc = (s.getTotalKeys() > 0)
                ? (s.getCorrectKeys() * 100.0 / s.getTotalKeys())
                : 0.0;

        return new LanguageStatsView(
                s.getSessionsCnt(),
                s.getRecentWpm() != null ? s.getRecentWpm().doubleValue() : 0.0,
                s.getMaxWpm() != null ? s.getMaxWpm().doubleValue() : 0.0,
                Math.round(avgAcc * 100.0) / 100.0,
                s.getTotalTimeMs() / 1000L
        );
    }
}
