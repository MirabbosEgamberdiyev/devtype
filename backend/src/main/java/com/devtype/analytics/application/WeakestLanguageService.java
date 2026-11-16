package com.devtype.analytics.application;

import com.devtype.analytics.infrastructure.UserLanguageStatsEntity;
import com.devtype.analytics.infrastructure.UserLanguageStatsRepository;
import com.devtype.content.api.LanguageCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WeakestLanguageService {

    public static final int MIN_CORE_SESSIONS = 10;
    public static final double WEAKEST_RATIO_THRESHOLD = 0.75;

    private final UserLanguageStatsRepository languageStatsRepository;
    private final LanguageCatalog languageCatalog;

    public WeakestLanguageService(
            UserLanguageStatsRepository languageStatsRepository,
            LanguageCatalog languageCatalog
    ) {
        this.languageStatsRepository = languageStatsRepository;
        this.languageCatalog = languageCatalog;
    }

    public record WeakestLanguageResult(
            String languageSlug,
            double recentWpm,
            double medianWpm,
            double ratio
    ) {}

    @Transactional(readOnly = true)
    public Optional<WeakestLanguageResult> findWeakestLanguage(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        List<UserLanguageStatsEntity> coreStats = languageStatsRepository.findByUserIdAndModeGroup(userId, "CORE");
        List<LanguageData> eligible = new ArrayList<>();

        for (UserLanguageStatsEntity s : coreStats) {
            if (s.getSessionsCnt() >= MIN_CORE_SESSIONS && s.getRecentWpm() != null && s.getRecentWpm().doubleValue() > 0.0) {
                String slug = languageCatalog.getPublishedLanguageSlugs().stream()
                        .filter(l -> languageCatalog.findLanguageIdBySlug(l).orElse((short) -1) == s.getLanguageId())
                        .findFirst()
                        .orElse(null);
                if (slug != null) {
                    eligible.add(new LanguageData(slug, s.getRecentWpm().doubleValue()));
                }
            }
        }

        if (eligible.size() < 2) {
            return Optional.empty();
        }

        List<Double> wpms = eligible.stream().map(l -> l.wpm).sorted().toList();
        double median = computeMedian(wpms);
        if (median <= 0.0) {
            return Optional.empty();
        }

        LanguageData weakest = null;
        double minRatio = 1.0;

        for (LanguageData l : eligible) {
            double ratio = l.wpm / median;
            if (ratio < WEAKEST_RATIO_THRESHOLD && ratio < minRatio) {
                minRatio = ratio;
                weakest = l;
            }
        }

        if (weakest != null) {
            return Optional.of(new WeakestLanguageResult(
                    weakest.slug,
                    weakest.wpm,
                    median,
                    Math.round(minRatio * 100.0) / 100.0
            ));
        }

        return Optional.empty();
    }

    private static double computeMedian(List<Double> sorted) {
        int size = sorted.size();
        if (size == 0) return 0.0;
        int mid = size / 2;
        if (size % 2 == 1) {
            return sorted.get(mid);
        } else {
            return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
        }
    }

    private record LanguageData(String slug, double wpm) {}
}
