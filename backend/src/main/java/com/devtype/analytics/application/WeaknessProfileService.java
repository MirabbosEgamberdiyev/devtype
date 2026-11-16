package com.devtype.analytics.application;

import com.devtype.analytics.api.WeaknessProfileProvider;
import com.devtype.analytics.infrastructure.UserBigramStatsEntity;
import com.devtype.analytics.infrastructure.UserBigramStatsRepository;
import com.devtype.analytics.infrastructure.UserCharStatsEntity;
import com.devtype.analytics.infrastructure.UserCharStatsRepository;
import com.devtype.content.api.LanguageCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WeaknessProfileService implements WeaknessProfileProvider {

    public static final int MIN_EXPECTED_CNT = 30;
    public static final int MIN_OBSERVED_SESSIONS = 3;
    public static final int MIN_ROWS_FOR_PROFILE = 8;
    public static final double ALNUM_ERROR_THRESHOLD = 0.08;
    public static final double WEAKNESS_SCORE_THRESHOLD = 1.0;

    private final UserCharStatsRepository charStatsRepository;
    private final UserBigramStatsRepository bigramStatsRepository;
    private final LanguageCatalog languageCatalog;

    public WeaknessProfileService(
            UserCharStatsRepository charStatsRepository,
            UserBigramStatsRepository bigramStatsRepository,
            LanguageCatalog languageCatalog
    ) {
        this.charStatsRepository = charStatsRepository;
        this.bigramStatsRepository = bigramStatsRepository;
        this.languageCatalog = languageCatalog;
    }

    public record ScoredItem(
            String token,
            boolean isChar,
            double ewmaError,
            double ewmaMs,
            double weaknessScore
    ) {}

    @Override
    @Transactional(readOnly = true)
    public WeaknessProfileView getProfile(Long userId, String languageSlug) {
        if (userId == null) {
            return new WeaknessProfileView(List.of(), List.of(), false);
        }

        short languageId = languageCatalog.findLanguageIdBySlug(languageSlug).orElse((short) 1);
        List<UserCharStatsEntity> chars = charStatsRepository.findByUserIdAndLanguageId(userId, languageId);
        List<UserBigramStatsEntity> bigrams = bigramStatsRepository.findByUserIdAndLanguageId(userId, languageId);

        return computeProfile(chars, bigrams);
    }

    public WeaknessProfileView computeProfile(
            List<UserCharStatsEntity> chars,
            List<UserBigramStatsEntity> bigrams
    ) {
        List<CandidateRow> rows = new ArrayList<>();

        if (chars != null) {
            for (UserCharStatsEntity c : chars) {
                if (c.getExpectedCnt() >= MIN_EXPECTED_CNT && c.getObservedSessions() >= MIN_OBSERVED_SESSIONS) {
                    double err = c.getEwmaError() != null ? c.getEwmaError().doubleValue() : 0.0;
                    double ms = c.getEwmaMs() != null ? c.getEwmaMs().doubleValue() : 0.0;

                    // Alnum filter: ignore unless error >= 0.08
                    if (isAlnum(c.getToken()) && err < ALNUM_ERROR_THRESHOLD) {
                        continue;
                    }
                    rows.add(new CandidateRow(c.getToken(), true, err, ms));
                }
            }
        }

        if (bigrams != null) {
            for (UserBigramStatsEntity b : bigrams) {
                if (b.getExpectedCnt() >= MIN_EXPECTED_CNT && b.getObservedSessions() >= MIN_OBSERVED_SESSIONS) {
                    double err = b.getEwmaError() != null ? b.getEwmaError().doubleValue() : 0.0;
                    double ms = b.getEwmaMs() != null ? b.getEwmaMs().doubleValue() : 0.0;
                    rows.add(new CandidateRow(b.getToken(), false, err, ms));
                }
            }
        }

        if (rows.size() < MIN_ROWS_FOR_PROFILE) {
            return new WeaknessProfileView(List.of(), List.of(), false);
        }

        double meanErr = rows.stream().mapToDouble(r -> r.ewmaError).average().orElse(0.0);
        double stdErr = calculateStdDev(rows.stream().map(r -> r.ewmaError).toList(), meanErr);

        double meanMs = rows.stream().mapToDouble(r -> r.ewmaMs).average().orElse(0.0);
        double stdMs = calculateStdDev(rows.stream().map(r -> r.ewmaMs).toList(), meanMs);

        List<ScoredItem> scored = new ArrayList<>();
        for (CandidateRow r : rows) {
            double ze = (stdErr > 0.0) ? (r.ewmaError - meanErr) / stdErr : 0.0;
            double zm = (stdMs > 0.0) ? (r.ewmaMs - meanMs) / stdMs : 0.0;
            double score = 0.6 * ze + 0.4 * zm;

            if (score > WEAKNESS_SCORE_THRESHOLD) {
                scored.add(new ScoredItem(r.token, r.isChar, r.ewmaError, r.ewmaMs, score));
            }
        }

        scored.sort((a, b) -> Double.compare(b.weaknessScore, a.weaknessScore));

        List<Character> weakChars = scored.stream()
                .filter(ScoredItem::isChar)
                .map(s -> s.token.charAt(0))
                .distinct()
                .limit(5)
                .toList();

        List<String> weakBigrams = scored.stream()
                .filter(s -> !s.isChar())
                .map(ScoredItem::token)
                .distinct()
                .limit(5)
                .toList();

        return new WeaknessProfileView(weakChars, weakBigrams, true);
    }

    private static boolean isAlnum(String token) {
        if (token == null || token.length() != 1) return false;
        char ch = token.charAt(0);
        return Character.isLetterOrDigit(ch);
    }

    private static double calculateStdDev(List<Double> values, double mean) {
        if (values.size() <= 1) return 0.0;
        double sumSq = 0.0;
        for (double v : values) {
            sumSq += (v - mean) * (v - mean);
        }
        return Math.sqrt(sumSq / values.size());
    }

    private record CandidateRow(
            String token,
            boolean isChar,
            double ewmaError,
            double ewmaMs
    ) {}
}
