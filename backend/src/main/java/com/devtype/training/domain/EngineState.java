package com.devtype.training.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic engine state — docs/07 §2.
 */
public record EngineState(
        EnginePhase phase,
        EngineMode mode,
        ErrorMode errorMode,
        List<List<TargetChar>> targets,
        int seq,
        List<CharState> states,
        List<Character> typed,
        List<Boolean> everWrong,
        int cursor,
        int consecutiveErrors,
        int activeMs,
        int lastEventAt,
        Integer limitMs,
        int correctKeys,
        int incorrectKeys,
        int backspaces,
        int totalTyped,
        int finishedCorrect,
        List<Integer> perSecond,
        List<KeyEvent> log
) {
    public static EngineState createInitial(
            List<List<TargetChar>> targets,
            EngineMode mode,
            ErrorMode errorMode,
            Integer limitMs
    ) {
        List<TargetChar> currentTargets = targets.isEmpty() ? List.of() : targets.get(0);
        int len = currentTargets.size();

        List<CharState> states = new ArrayList<>(len);
        List<Character> typed = new ArrayList<>(Collections.nCopies(len, null));
        List<Boolean> everWrong = new ArrayList<>(Collections.nCopies(len, false));

        for (TargetChar tc : currentTargets) {
            states.add(tc.auto() ? CharState.CORRECT : CharState.PENDING);
        }

        int cursor = 0;
        while (cursor < len && currentTargets.get(cursor).auto()) {
            cursor++;
        }

        return new EngineState(
                EnginePhase.IDLE,
                mode,
                errorMode,
                targets,
                0,
                states,
                typed,
                everWrong,
                cursor,
                0,
                0,
                0,
                limitMs,
                0,
                0,
                0,
                0,
                0,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }
}
