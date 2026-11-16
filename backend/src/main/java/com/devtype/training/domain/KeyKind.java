package com.devtype.training.domain;

/**
 * Keylog event kind codes — docs/07 §3, §7.
 */
public enum KeyKind {
    CHAR(0),
    ENTER(1),
    TAB(2),
    BACKSPACE(3),
    WORD_DELETE(4),
    PAUSE(5),
    RESUME(6),
    NEXT(7),
    FINISH_EARLY(8),
    TIMEOUT(9);

    private final int code;

    KeyKind(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static KeyKind fromCode(int code) {
        for (KeyKind kind : values()) {
            if (kind.code == code) {
                return kind;
            }
        }
        throw new IllegalArgumentException("KEYLOG_INVALID: unknown kind code " + code);
    }
}
