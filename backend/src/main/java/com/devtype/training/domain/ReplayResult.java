package com.devtype.training.domain;

import java.util.Map;

/**
 * Result of server keylog replay containing official metrics and analysis — docs/07 §8.
 */
public record ReplayResult(
        EngineMetrics metrics,
        Map<String, CharStat> charStats,
        Map<String, Map<String, Integer>> confusion,
        SuspicionRules.SuspicionEvaluation suspicion
) {
    public record CharStat(
            int expected,
            int errors,
            long totalMs
    ) {}
}
