package com.devtype.training.domain;

/**
 * State of a target character in the engine — docs/07 §2.
 */
public enum CharState {
    PENDING,
    CORRECT,
    INCORRECT,
    CORRECTED
}
