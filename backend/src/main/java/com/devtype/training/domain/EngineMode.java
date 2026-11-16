package com.devtype.training.domain;

/**
 * Supported training modes — docs/00 §4.4, docs/07.
 */
public enum EngineMode {
    PRACTICE,
    SPEED_TEST,
    ACCURACY,
    CHALLENGE,
    SPECIAL_CHARS,
    WEAKNESS,
    CODE_SPRINT;

    public ErrorMode defaultErrorMode() {
        return switch (this) {
            case PRACTICE, ACCURACY, SPECIAL_CHARS, WEAKNESS -> ErrorMode.STOP;
            case SPEED_TEST, CHALLENGE, CODE_SPRINT -> ErrorMode.FREE;
        };
    }
}
