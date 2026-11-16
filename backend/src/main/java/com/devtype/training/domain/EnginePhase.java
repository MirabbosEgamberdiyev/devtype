package com.devtype.training.domain;

/**
 * Lifecycle phase of the typing engine — docs/07 §2.
 */
public enum EnginePhase {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}
