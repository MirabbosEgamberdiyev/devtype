package com.devtype.training.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Anti-Cheat Suspicion Scoring Engine — docs/00 §4.6, docs/07 §9.3, AC-SP-20.
 * Evaluates typing rhythm, speed anomalies, and artificial patterns.
 */
public final class SuspicionRules {

    public static final double SUSPICIOUS_WPM_THRESHOLD = 220.0;
    public static final int SUSPICION_FLAG_THRESHOLD = 50;

    public record SuspicionContext(
            Double userLanguageMedianWpm,
            int userSessionCountInLanguage
    ) {
        public static SuspicionContext empty() {
            return new SuspicionContext(null, 0);
        }
    }

    public record SuspicionEvaluation(
            int score,
            boolean flagged,
            List<String> reasons
    ) {}

    private SuspicionRules() {
    }

    public static SuspicionEvaluation evaluate(
            EngineMetrics metrics,
            List<KeyEvent> events,
            SuspicionContext context
    ) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        double wpm = metrics.wpm().doubleValue();

        // Rule 1: SPEED (WPM > 220 -> +50)
        if (wpm > SUSPICIOUS_WPM_THRESHOLD) {
            score += 50;
            reasons.add("SPEED");
        }

        // Collect valid typing intervals between consecutive CHAR/ENTER keystrokes
        // excluding pauses and next transitions
        List<Integer> intervals = new ArrayList<>();
        KeyEvent prevKey = null;

        for (KeyEvent ev : events) {
            if (ev.kind() == KeyKind.PAUSE || ev.kind() == KeyKind.RESUME || ev.kind() == KeyKind.NEXT) {
                prevKey = null;
                continue;
            }
            if (ev.kind() == KeyKind.CHAR || ev.kind() == KeyKind.ENTER) {
                if (prevKey != null) {
                    int dt = ev.t() - prevKey.t();
                    if (dt >= 0) {
                        intervals.add(dt);
                    }
                }
                prevKey = ev;
            } else {
                prevKey = null;
            }
        }

        // Analyze rhythm if >= 30 intervals exist
        if (intervals.size() >= 30) {
            // Rule 2: FAST_INTERVALS (dt < 20ms share > 20% -> +50)
            long fastCount = intervals.stream().filter(dt -> dt < 20).count();
            double fastRatio = (double) fastCount / intervals.size();
            if (fastRatio > 0.20) {
                score += 50;
                reasons.add("FAST_INTERVALS");
            }

            // Rule 3: FLAT_RHYTHM (CV = sigma / mu < 0.15 -> +40)
            double sum = 0.0;
            for (int dt : intervals) {
                sum += dt;
            }
            double mu = sum / intervals.size();
            if (mu > 0.0) {
                double varianceSum = 0.0;
                for (int dt : intervals) {
                    varianceSum += Math.pow(dt - mu, 2);
                }
                double sigma = Math.sqrt(varianceSum / intervals.size());
                double cv = sigma / mu;
                if (cv < 0.15) {
                    score += 40;
                    reasons.add("FLAT_RHYTHM");
                }
            }
        }

        // Rule 4: JUMP (>= 60% above user median in language when >= 10 sessions -> +20)
        if (context.userSessionCountInLanguage() >= 10 && context.userLanguageMedianWpm() != null) {
            double median = context.userLanguageMedianWpm();
            if (median > 0.0 && wpm >= median * 1.60) {
                score += 20;
                reasons.add("JUMP");
            }
        }

        // Rule 5: NO_ERRORS_FAST (K✗ = 0, WPM > 150, C >= 300 -> +20)
        if (metrics.incorrectKeys() == 0 && wpm > 150.0 && metrics.correctChars() >= 300) {
            score += 20;
            reasons.add("NO_ERRORS_FAST");
        }

        boolean flagged = score >= SUSPICION_FLAG_THRESHOLD;
        return new SuspicionEvaluation(score, flagged, reasons);
    }
}
