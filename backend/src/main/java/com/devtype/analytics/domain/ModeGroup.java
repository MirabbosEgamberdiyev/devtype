package com.devtype.analytics.domain;

public enum ModeGroup {
    CORE,
    DRILL;

    public static ModeGroup fromMode(String mode) {
        if (mode == null) return CORE;
        return switch (mode.toUpperCase()) {
            case "SPECIAL_CHARS", "WEAKNESS" -> DRILL;
            default -> CORE; // PRACTICE, SPEED_TEST, ACCURACY, CHALLENGE, CODE_SPRINT
        };
    }
}
