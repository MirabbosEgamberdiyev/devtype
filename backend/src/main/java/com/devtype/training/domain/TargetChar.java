package com.devtype.training.domain;

/**
 * Target character with formatting and analytical metadata — docs/07 §1.
 */
public record TargetChar(
        char ch,
        int line,
        int col,
        boolean auto,
        int wordId,
        int patternId,
        int exerciseSeq
) {
}
