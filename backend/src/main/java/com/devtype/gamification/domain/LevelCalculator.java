package com.devtype.gamification.domain;

public class LevelCalculator {

    public static final int MAX_LEVEL = 200;

    private static final long[] CUMULATIVE_XP = new long[MAX_LEVEL + 1];
    private static final int[] XP_TO_NEXT = new int[MAX_LEVEL + 1];

    static {
        CUMULATIVE_XP[1] = 0;
        for (int i = 1; i <= MAX_LEVEL; i++) {
            XP_TO_NEXT[i] = (int) Math.round(100.0 * Math.pow(i, 1.5));
            if (i < MAX_LEVEL) {
                CUMULATIVE_XP[i + 1] = CUMULATIVE_XP[i] + XP_TO_NEXT[i];
            }
        }
    }

    public record LevelInfo(
            int level,
            long xpIntoLevel,
            int xpToNext,
            long cumulativeXpForCurrentLevel
    ) {}

    public static LevelInfo getLevelInfo(long totalXp) {
        if (totalXp <= 0) {
            return new LevelInfo(1, 0, XP_TO_NEXT[1], 0);
        }

        int level = 1;
        for (int i = MAX_LEVEL; i >= 1; i--) {
            if (totalXp >= CUMULATIVE_XP[i]) {
                level = i;
                break;
            }
        }

        long xpInto = totalXp - CUMULATIVE_XP[level];
        int toNext = (level < MAX_LEVEL) ? XP_TO_NEXT[level] : 0;

        return new LevelInfo(level, xpInto, toNext, CUMULATIVE_XP[level]);
    }

    public static int calculateLevel(long totalXp) {
        return getLevelInfo(totalXp).level();
    }

    public static int xpToNext(int level) {
        if (level < 1) return XP_TO_NEXT[1];
        if (level >= MAX_LEVEL) return 0;
        return XP_TO_NEXT[level];
    }

    public static long cumulativeXp(int level) {
        if (level <= 1) return 0;
        if (level > MAX_LEVEL) return CUMULATIVE_XP[MAX_LEVEL];
        return CUMULATIVE_XP[level];
    }
}
