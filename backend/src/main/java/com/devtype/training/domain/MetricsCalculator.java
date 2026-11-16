package com.devtype.training.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pure Metrics Calculator — docs/00 §4.5, docs/07 §6.
 * Matches frontend/src/engine/metrics.ts 1-to-1.
 */
public final class MetricsCalculator {

    private MetricsCalculator() {
    }

    public static double populationStdDev(List<Double> values, double mean) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double v : values) {
            sum += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sum / values.size());
    }

    public static BigDecimal calculateConsistency(List<Integer> perSecond) {
        if (perSecond.size() < 3) {
            return EngineMetrics.roundHalfUp(0.0, 2);
        }

        List<Double> r = perSecond.stream()
                .map(k -> (k / 5.0) * 60.0)
                .toList();

        double sum = 0.0;
        for (double val : r) {
            sum += val;
        }
        double mu = sum / r.size();

        if (mu <= 0.0) {
            return EngineMetrics.roundHalfUp(0.0, 2);
        }

        double sigma = populationStdDev(r, mu);
        double consistency = 100.0 * (1.0 - (sigma / mu));
        double clamped = Math.max(0.0, Math.min(100.0, consistency));

        return EngineMetrics.roundHalfUp(clamped, 2);
    }

    public static EngineMetrics calculate(EngineState state) {
        List<TargetChar> currentTargets = state.targets().isEmpty() ? List.of() : state.targets().get(state.seq());
        List<CharState> currentStates = state.states();

        int totalTypableAllSnippets = 0;
        for (List<TargetChar> snippetTargets : state.targets()) {
            for (TargetChar tc : snippetTargets) {
                if (!tc.auto()) {
                    totalTypableAllSnippets++;
                }
            }
        }

        int currentCorrectCount = 0;
        int uncorrectedErrors = 0;
        int typedPositions = 0;

        boolean isTimeout = !state.log().isEmpty() && state.log().get(state.log().size() - 1).kind() == KeyKind.TIMEOUT;
        Integer incompleteWordId = null;

        if (isTimeout && state.cursor() < currentTargets.size()) {
            int cursorWordId = currentTargets.get(state.cursor()).wordId();
            boolean isCompleted = true;
            for (int i = 0; i < currentTargets.size(); i++) {
                TargetChar tc = currentTargets.get(i);
                if (tc.wordId() == cursorWordId && !tc.auto()) {
                    CharState s = currentStates.get(i);
                    if (s != CharState.CORRECT && s != CharState.CORRECTED) {
                        isCompleted = false;
                        break;
                    }
                }
            }
            if (!isCompleted) {
                incompleteWordId = cursorWordId;
            }
        }

        for (int i = 0; i < currentTargets.size(); i++) {
            TargetChar tc = currentTargets.get(i);
            CharState s = currentStates.get(i);

            if (!tc.auto()) {
                if (s != CharState.PENDING) {
                    typedPositions++;
                }

                if (s == CharState.CORRECT || s == CharState.CORRECTED) {
                    if (incompleteWordId != null && tc.wordId() == incompleteWordId) {
                        // Excluded
                    } else {
                        currentCorrectCount++;
                    }
                } else if (s == CharState.INCORRECT) {
                    uncorrectedErrors++;
                }
            }
        }

        int c = state.finishedCorrect() + currentCorrectCount;
        int t = state.totalTyped();
        int kCorrect = state.correctKeys();
        int kIncorrect = state.incorrectKeys();

        double activeMinutes;
        if (state.mode() == EngineMode.SPEED_TEST && isTimeout && state.limitMs() != null) {
            activeMinutes = state.limitMs() / 60000.0;
        } else {
            activeMinutes = state.activeMs() / 60000.0;
        }

        double wpmRaw = activeMinutes > 0.0 ? (c / 5.0) / activeMinutes : 0.0;
        BigDecimal wpm = EngineMetrics.roundHalfUp(wpmRaw, 2);

        double rawWpmVal = activeMinutes > 0.0 ? (t / 5.0) / activeMinutes : 0.0;
        BigDecimal rawWpm = EngineMetrics.roundHalfUp(rawWpmVal, 2);

        double cpmVal = activeMinutes > 0.0 ? c / activeMinutes : 0.0;
        BigDecimal cpm = EngineMetrics.roundHalfUp(cpmVal, 2);

        int totalKeys = kCorrect + kIncorrect;
        double accuracyVal = totalKeys > 0 ? ((double) kCorrect / totalKeys) * 100.0 : 0.0;
        BigDecimal accuracy = EngineMetrics.roundHalfUp(accuracyVal, 2);

        BigDecimal consistency = calculateConsistency(state.perSecond());

        BigDecimal completionPct = BigDecimal.ZERO;
        BigDecimal progressPct = BigDecimal.ZERO;

        if (totalTypableAllSnippets > 0) {
            progressPct = EngineMetrics.roundHalfUp(((double) typedPositions / totalTypableAllSnippets) * 100.0, 2);
            if (state.phase() == EnginePhase.FINISHED && state.seq() >= state.targets().size() - 1 && uncorrectedErrors == 0) {
                completionPct = EngineMetrics.roundHalfUp(100.0, 2);
            } else {
                completionPct = EngineMetrics.roundHalfUp(((double) c / totalTypableAllSnippets) * 100.0, 2);
            }
        }

        return new EngineMetrics(
                wpm,
                rawWpm,
                cpm,
                accuracy,
                consistency,
                state.activeMs(),
                t,
                c,
                uncorrectedErrors,
                kCorrect,
                kIncorrect,
                state.backspaces(),
                completionPct,
                progressPct
        );
    }
}
