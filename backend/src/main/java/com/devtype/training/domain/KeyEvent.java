package com.devtype.training.domain;

/**
 * Single typing keystroke or lifecycle event — docs/07 §3.
 */
public record KeyEvent(
        int t,
        KeyKind kind,
        Character ch
) {
    public static KeyEvent ofChar(int t, char ch) {
        return new KeyEvent(t, KeyKind.CHAR, ch);
    }

    public static KeyEvent ofEnter(int t) {
        return new KeyEvent(t, KeyKind.ENTER, null);
    }

    public static KeyEvent ofTab(int t) {
        return new KeyEvent(t, KeyKind.TAB, null);
    }

    public static KeyEvent ofBackspace(int t) {
        return new KeyEvent(t, KeyKind.BACKSPACE, null);
    }

    public static KeyEvent ofWordDelete(int t) {
        return new KeyEvent(t, KeyKind.WORD_DELETE, null);
    }

    public static KeyEvent ofPause(int t) {
        return new KeyEvent(t, KeyKind.PAUSE, null);
    }

    public static KeyEvent ofResume(int t) {
        return new KeyEvent(t, KeyKind.RESUME, null);
    }

    public static KeyEvent ofNext(int t) {
        return new KeyEvent(t, KeyKind.NEXT, null);
    }

    public static KeyEvent ofFinishEarly(int t) {
        return new KeyEvent(t, KeyKind.FINISH_EARLY, null);
    }

    public static KeyEvent ofTimeout(int t) {
        return new KeyEvent(t, KeyKind.TIMEOUT, null);
    }
}
