package com.devtype.gamification.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XpCalculator {

    public static final int DAILY_XP_CAP = 3000;
    public static final int BONUS_RECORD = 25;
    public static final int BONUS_FIRST_OF_DAY = 10;
    public static final int BONUS_SPRINT = 30;

    public record SnippetWeight(String difficulty, int correctChars) {}

    public record XpCalculationResult(
            int earnedXp,
            XpBreakdown breakdown
    ) {}

    public static XpCalculationResult calculate(
            int correctChars,
            String mode,
            List<SnippetWeight> snippetWeights,
            double accuracy,
            double completionPct,
            int repeatCountIn24h,
            boolean isRecordBroken,
            boolean isFirstOfDay,
            boolean isSprintComplete,
            int todaySessionXp
    ) {
        if (correctChars <= 0) {
            Map<String, Integer> bonuses = new LinkedHashMap<>();
            if (isFirstOfDay) {
                bonuses.put("firstOfDay", BONUS_FIRST_OF_DAY);
            }
            int totalBonus = bonuses.values().stream().mapToInt(Integer::intValue).sum();
            int awarded = Math.min(totalBonus, Math.max(0, DAILY_XP_CAP - todaySessionXp));
            boolean capped = totalBonus > awarded;
            return new XpCalculationResult(
                    awarded,
                    new XpBreakdown(0.0, getModeCoef(mode), 1.0, getAccFactor(accuracy), 1.0, bonuses, capped)
            );
        }

        double modeCoef = getModeCoef(mode);
        double diffCoef = getDiffCoef(mode, snippetWeights);
        double accFactor = getAccFactor(accuracy);
        double repeatFactor = getRepeatFactor(mode, repeatCountIn24h);

        double baseProduct = (correctChars / 10.0) * modeCoef * diffCoef * accFactor * repeatFactor;
        int baseXp = (int) Math.round(baseProduct);

        Map<String, Integer> bonuses = new LinkedHashMap<>();
        if (isFirstOfDay) {
            bonuses.put("firstOfDay", BONUS_FIRST_OF_DAY);
        }
        if (completionPct >= 100.0) {
            if (isRecordBroken) {
                bonuses.put("record", BONUS_RECORD);
            }
            if (isSprintComplete) {
                bonuses.put("sprint", BONUS_SPRINT);
            }
        }

        int totalBonuses = bonuses.values().stream().mapToInt(Integer::intValue).sum();
        int uncappedTotal = baseXp + totalBonuses;

        int availableCap = Math.max(0, DAILY_XP_CAP - todaySessionXp);
        int finalXp = Math.min(uncappedTotal, availableCap);
        boolean capped = uncappedTotal > availableCap;

        XpBreakdown breakdown = new XpBreakdown(
                Math.round(baseProduct * 10.0) / 10.0,
                modeCoef,
                Math.round(diffCoef * 100.0) / 100.0,
                Math.round(accFactor * 1000.0) / 1000.0,
                repeatFactor,
                bonuses,
                capped
        );

        return new XpCalculationResult(finalXp, breakdown);
    }

    public static double getModeCoef(String mode) {
        if (mode == null) return 1.0;
        return switch (mode.toUpperCase()) {
            case "SPEED_TEST" -> 1.2;
            case "ACCURACY" -> 1.3;
            case "CHALLENGE" -> 1.5;
            case "SPECIAL_CHARS" -> 1.0;
            case "WEAKNESS" -> 1.4;
            case "CODE_SPRINT" -> 1.5;
            default -> 1.0; // PRACTICE
        };
    }

    public static double getDiffCoef(String mode, List<SnippetWeight> snippetWeights) {
        if ("SPECIAL_CHARS".equalsIgnoreCase(mode)) {
            return 1.0;
        }
        if ("SPEED_TEST".equalsIgnoreCase(mode)) {
            return 1.25;
        }
        if (snippetWeights == null || snippetWeights.isEmpty()) {
            return 1.0;
        }

        int totalChars = 0;
        double weightedSum = 0.0;
        for (SnippetWeight sw : snippetWeights) {
            double c = diffToCoef(sw.difficulty());
            int chars = Math.max(0, sw.correctChars());
            totalChars += chars;
            weightedSum += c * chars;
        }

        if (totalChars == 0) {
            return diffToCoef(snippetWeights.get(0).difficulty());
        }
        return weightedSum / totalChars;
    }

    public static double diffToCoef(String difficulty) {
        if (difficulty == null) return 1.0;
        return switch (difficulty.toUpperCase()) {
            case "INTERMEDIATE" -> 1.25;
            case "ADVANCED" -> 1.5;
            case "EXPERT" -> 2.0;
            default -> 1.0; // BEGINNER
        };
    }

    public static double getAccFactor(double accuracy) {
        double acc = Math.round(accuracy * 100.0) / 100.0;
        if (acc < 85.0) {
            return 0.25;
        }
        if (acc < 95.0) {
            return 0.5 + (acc - 85.0) * 0.05;
        }
        if (acc < 98.0) {
            return 1.0;
        }
        if (acc < 100.0) {
            return 1.1;
        }
        return 1.25; // 100.0%
    }

    public static double getRepeatFactor(String mode, int repeatCountIn24h) {
        if (mode == null) return 1.0;
        String m = mode.toUpperCase();
        if (m.equals("PRACTICE") || m.equals("ACCURACY") || m.equals("CHALLENGE")) {
            if (repeatCountIn24h <= 0) return 1.0;
            if (repeatCountIn24h == 1) return 0.5;
            return 0.1;
        }
        return 1.0;
    }
}
