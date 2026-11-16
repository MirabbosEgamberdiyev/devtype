package com.devtype.training.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure Deterministic Engine Reducer — docs/07 §2, §3.
 * Matches frontend/src/engine/reducer.ts exactly.
 */
public final class EngineReducer {

    public static final int FREE_MODE_CONSECUTIVE_ERRORS_LIMIT = 8;

    private EngineReducer() {
    }

    public static int getFirstTypablePosition(List<TargetChar> targets) {
        int pos = 0;
        while (pos < targets.size() && targets.get(pos).auto()) {
            pos++;
        }
        return pos;
    }

    private static int advanceCursor(int cursor, List<TargetChar> targets) {
        int next = cursor + 1;
        while (next < targets.size() && targets.get(next).auto()) {
            next++;
        }
        return next;
    }

    private static List<Integer> recordPerSecondKeystroke(List<Integer> perSecond, int eventTimeMs) {
        int sec = eventTimeMs / 1000;
        List<Integer> updated = new ArrayList<>(perSecond);
        while (updated.size() <= sec) {
            updated.add(0);
        }
        updated.set(sec, updated.get(sec) + 1);
        return updated;
    }

    public static EngineState reduce(EngineState state, KeyEvent event) {
        if (state.phase() == EnginePhase.FINISHED) {
            return state;
        }

        // FSM Transitions (docs/07 §3.1)
        if (state.phase() == EnginePhase.IDLE) {
            if (event.kind() != KeyKind.CHAR && event.kind() != KeyKind.ENTER && event.kind() != KeyKind.TAB) {
                return state;
            }
            EngineState startedState = new EngineState(
                    EnginePhase.RUNNING,
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    state.states(),
                    state.typed(),
                    state.everWrong(),
                    state.cursor(),
                    state.consecutiveErrors(),
                    0,
                    event.t(),
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces(),
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    state.log()
            );
            return reduce(startedState, event);
        }

        if (state.phase() == EnginePhase.PAUSED) {
            if (event.kind() == KeyKind.RESUME) {
                List<KeyEvent> newLog = new ArrayList<>(state.log());
                newLog.add(event);
                return new EngineState(
                        EnginePhase.RUNNING,
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        state.everWrong(),
                        state.cursor(),
                        state.consecutiveErrors(),
                        state.activeMs(),
                        event.t(),
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces(),
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
            }
            if (event.kind() == KeyKind.CHAR || event.kind() == KeyKind.ENTER || event.kind() == KeyKind.TAB ||
                event.kind() == KeyKind.BACKSPACE || event.kind() == KeyKind.WORD_DELETE) {
                List<KeyEvent> newLog = new ArrayList<>(state.log());
                newLog.add(KeyEvent.ofResume(event.t()));
                EngineState resumed = new EngineState(
                        EnginePhase.RUNNING,
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        state.everWrong(),
                        state.cursor(),
                        state.consecutiveErrors(),
                        state.activeMs(),
                        event.t(),
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces(),
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
                return reduce(resumed, event);
            }
            return state;
        }

        // Running
        List<TargetChar> currentTargets = state.targets().isEmpty() ? List.of() : state.targets().get(state.seq());
        int currentLen = currentTargets.size();

        List<KeyEvent> newLog = new ArrayList<>(state.log());
        newLog.add(event);

        int newActiveMs = state.activeMs();
        if (event.t() >= state.lastEventAt()) {
            newActiveMs += (event.t() - state.lastEventAt());
        }
        int newLastEventAt = event.t();

        // Pause
        if (event.kind() == KeyKind.PAUSE) {
            if (state.errorMode() == ErrorMode.STOP) {
                return new EngineState(
                        EnginePhase.PAUSED,
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        state.everWrong(),
                        state.cursor(),
                        state.consecutiveErrors(),
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces(),
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
            }
            return state;
        }

        // Timeout
        if (event.kind() == KeyKind.TIMEOUT) {
            int finalActiveMs = state.limitMs() != null ? state.limitMs() : newActiveMs;
            return new EngineState(
                    EnginePhase.FINISHED,
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    state.states(),
                    state.typed(),
                    state.everWrong(),
                    state.cursor(),
                    state.consecutiveErrors(),
                    finalActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces(),
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    newLog
            );
        }

        // FinishEarly
        if (event.kind() == KeyKind.FINISH_EARLY) {
            return new EngineState(
                    EnginePhase.FINISHED,
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    state.states(),
                    state.typed(),
                    state.everWrong(),
                    state.cursor(),
                    state.consecutiveErrors(),
                    newActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces(),
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    newLog
            );
        }

        // Next
        if (event.kind() == KeyKind.NEXT) {
            if (state.cursor() < currentLen) {
                return state;
            }
            int nextSeq = state.seq() + 1;
            if (nextSeq >= state.targets().size()) {
                return new EngineState(
                        EnginePhase.FINISHED,
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        state.everWrong(),
                        state.cursor(),
                        state.consecutiveErrors(),
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces(),
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
            }

            int snippetCorrect = 0;
            for (int i = 0; i < currentLen; i++) {
                TargetChar tc = currentTargets.get(i);
                CharState cs = state.states().get(i);
                if (!tc.auto() && (cs == CharState.CORRECT || cs == CharState.CORRECTED)) {
                    snippetCorrect++;
                }
            }

            List<TargetChar> nextTargets = state.targets().get(nextSeq);
            int nextLen = nextTargets.size();
            List<CharState> nextStates = new ArrayList<>(nextLen);
            for (TargetChar tc : nextTargets) {
                nextStates.add(tc.auto() ? CharState.CORRECT : CharState.PENDING);
            }
            List<Character> nextTyped = new ArrayList<>(Collections.nCopies(nextLen, null));
            List<Boolean> nextEverWrong = new ArrayList<>(Collections.nCopies(nextLen, false));
            int nextCursor = getFirstTypablePosition(nextTargets);

            return new EngineState(
                    state.phase(),
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    nextSeq,
                    nextStates,
                    nextTyped,
                    nextEverWrong,
                    nextCursor,
                    0,
                    newActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces(),
                    state.totalTyped(),
                    state.finishedCorrect() + snippetCorrect,
                    state.perSecond(),
                    newLog
            );
        }

        // Backspace
        if (event.kind() == KeyKind.BACKSPACE) {
            int firstPos = getFirstTypablePosition(currentTargets);
            if (state.errorMode() == ErrorMode.STOP) {
                return new EngineState(
                        state.phase(),
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        state.everWrong(),
                        state.cursor(),
                        state.consecutiveErrors(),
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces() + 1,
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
            }

            if (state.cursor() <= firstPos) {
                return new EngineState(
                        state.phase(),
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        state.everWrong(),
                        state.cursor(),
                        state.consecutiveErrors(),
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces(),
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
            }

            int prevPos = state.cursor() - 1;
            while (prevPos >= 0 && currentTargets.get(prevPos).auto()) {
                prevPos--;
            }

            if (prevPos < 0) {
                return state;
            }

            List<CharState> newStates = new ArrayList<>(state.states());
            List<Character> newTyped = new ArrayList<>(state.typed());
            int newConsecutiveErrors = state.consecutiveErrors();

            if (newStates.get(prevPos) == CharState.INCORRECT) {
                newConsecutiveErrors = Math.max(0, newConsecutiveErrors - 1);
            }
            newStates.set(prevPos, CharState.PENDING);
            newTyped.set(prevPos, null);

            return new EngineState(
                    state.phase(),
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    newStates,
                    newTyped,
                    state.everWrong(),
                    prevPos,
                    newConsecutiveErrors,
                    newActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces() + 1,
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    newLog
            );
        }

        // WordDelete
        if (event.kind() == KeyKind.WORD_DELETE) {
            int firstPos = getFirstTypablePosition(currentTargets);
            if (state.errorMode() == ErrorMode.STOP) {
                return new EngineState(
                        state.phase(),
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        state.everWrong(),
                        state.cursor(),
                        state.consecutiveErrors(),
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces() + 1,
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
            }

            if (state.cursor() <= firstPos) {
                return state;
            }

            List<CharState> newStates = new ArrayList<>(state.states());
            List<Character> newTyped = new ArrayList<>(state.typed());

            int pos = state.cursor() - 1;
            while (pos >= 0 && currentTargets.get(pos).auto()) {
                pos--;
            }

            if (pos < 0) {
                return state;
            }

            TargetChar currentPosChar = currentTargets.get(pos);
            if (currentPosChar.ch() == '\n') {
                newStates.set(pos, CharState.PENDING);
                newTyped.set(pos, null);
                return new EngineState(
                        state.phase(),
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        newStates,
                        newTyped,
                        state.everWrong(),
                        pos,
                        state.consecutiveErrors(),
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys(),
                        state.backspaces() + 1,
                        state.totalTyped(),
                        state.finishedCorrect(),
                        state.perSecond(),
                        newLog
                );
            }

            int targetWordId = currentPosChar.wordId();
            while (pos >= firstPos && currentTargets.get(pos).wordId() == targetWordId && currentTargets.get(pos).ch() != '\n') {
                TargetChar tc = currentTargets.get(pos);
                if (!tc.auto()) {
                    newStates.set(pos, CharState.PENDING);
                    newTyped.set(pos, null);
                }
                pos--;
            }

            int nextCursor = Math.max(firstPos, pos + 1);

            int leftErrors = 0;
            int scan = nextCursor - 1;
            while (scan >= 0 && newStates.get(scan) == CharState.INCORRECT) {
                leftErrors++;
                scan--;
            }

            return new EngineState(
                    state.phase(),
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    newStates,
                    newTyped,
                    state.everWrong(),
                    nextCursor,
                    leftErrors,
                    newActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces() + 1,
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    newLog
            );
        }

        // Tab
        if (event.kind() == KeyKind.TAB) {
            if (state.cursor() >= currentLen) {
                return state;
            }
            TargetChar exp = currentTargets.get(state.cursor());
            if (exp.ch() == '\t') {
                return reduce(state, KeyEvent.ofChar(event.t(), '\t'));
            }
            if (exp.ch() == ' ') {
                int spaceCount = 0;
                int checkPos = state.cursor();
                while (checkPos < currentLen &&
                       currentTargets.get(checkPos).line() == exp.line() &&
                       currentTargets.get(checkPos).ch() == ' ') {
                    spaceCount++;
                    checkPos++;
                }

                if (spaceCount > 0) {
                    int fillCount = Math.min(spaceCount, 2);
                    List<CharState> newStates = new ArrayList<>(state.states());
                    for (int i = 0; i < fillCount; i++) {
                        newStates.set(state.cursor() + i, CharState.CORRECT);
                    }

                    int newCursor = state.cursor() + fillCount;
                    while (newCursor < currentLen && currentTargets.get(newCursor).auto()) {
                        newCursor++;
                    }

                    List<Integer> newPerSec = recordPerSecondKeystroke(state.perSecond(), event.t());
                    boolean isFinished = newCursor >= currentLen && state.seq() == state.targets().size() - 1;

                    return new EngineState(
                            isFinished ? EnginePhase.FINISHED : state.phase(),
                            state.mode(),
                            state.errorMode(),
                            state.targets(),
                            state.seq(),
                            newStates,
                            state.typed(),
                            state.everWrong(),
                            newCursor,
                            state.consecutiveErrors(),
                            newActiveMs,
                            newLastEventAt,
                            state.limitMs(),
                            state.correctKeys() + 1,
                            state.incorrectKeys(),
                            state.backspaces(),
                            state.totalTyped() + 1,
                            state.finishedCorrect(),
                            newPerSec,
                            newLog
                    );
                }
            }
            return reduce(state, KeyEvent.ofChar(event.t(), '\t'));
        }

        // Char and Enter
        char inputChar = event.kind() == KeyKind.ENTER ? '\n' : (event.ch() != null ? event.ch() : '\0');

        if (event.kind() == KeyKind.ENTER && state.cursor() >= currentLen && state.mode() == EngineMode.CHALLENGE) {
            return new EngineState(
                    EnginePhase.FINISHED,
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    state.states(),
                    state.typed(),
                    state.everWrong(),
                    state.cursor(),
                    state.consecutiveErrors(),
                    newActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces(),
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    newLog
            );
        }

        if (state.cursor() >= currentLen) {
            return state;
        }

        if (state.errorMode() == ErrorMode.FREE && state.consecutiveErrors() >= FREE_MODE_CONSECUTIVE_ERRORS_LIMIT) {
            return new EngineState(
                    state.phase(),
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    state.states(),
                    state.typed(),
                    state.everWrong(),
                    state.cursor(),
                    state.consecutiveErrors(),
                    newActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces(),
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    newLog
            );
        }

        TargetChar expChar = currentTargets.get(state.cursor());
        boolean isMatch = inputChar == expChar.ch();

        int newTotalTyped = state.totalTyped() + 1;
        List<Integer> newPerSec = recordPerSecondKeystroke(state.perSecond(), event.t());

        if (isMatch) {
            List<CharState> newStates = new ArrayList<>(state.states());
            boolean wasWrong = state.everWrong().get(state.cursor());
            newStates.set(state.cursor(), wasWrong ? CharState.CORRECTED : CharState.CORRECT);

            int nextCursor = advanceCursor(state.cursor(), currentTargets);
            boolean isFinished = nextCursor >= currentLen && state.seq() == state.targets().size() - 1;

            return new EngineState(
                    isFinished ? EnginePhase.FINISHED : state.phase(),
                    state.mode(),
                    state.errorMode(),
                    state.targets(),
                    state.seq(),
                    newStates,
                    state.typed(),
                    state.everWrong(),
                    nextCursor,
                    0,
                    newActiveMs,
                    newLastEventAt,
                    state.limitMs(),
                    state.correctKeys() + 1,
                    state.incorrectKeys(),
                    state.backspaces(),
                    newTotalTyped,
                    state.finishedCorrect(),
                    newPerSec,
                    newLog
            );
        } else {
            List<Boolean> newEverWrong = new ArrayList<>(state.everWrong());
            newEverWrong.set(state.cursor(), true);

            if (state.errorMode() == ErrorMode.STOP) {
                return new EngineState(
                        state.phase(),
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        state.states(),
                        state.typed(),
                        newEverWrong,
                        state.cursor(),
                        state.consecutiveErrors(),
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys() + 1,
                        state.backspaces(),
                        newTotalTyped,
                        state.finishedCorrect(),
                        newPerSec,
                        newLog
                );
            } else {
                List<CharState> newStates = new ArrayList<>(state.states());
                List<Character> newTyped = new ArrayList<>(state.typed());
                newStates.set(state.cursor(), CharState.INCORRECT);
                newTyped.set(state.cursor(), inputChar);

                int nextCursor = advanceCursor(state.cursor(), currentTargets);

                return new EngineState(
                        state.phase(),
                        state.mode(),
                        state.errorMode(),
                        state.targets(),
                        state.seq(),
                        newStates,
                        newTyped,
                        newEverWrong,
                        nextCursor,
                        state.consecutiveErrors() + 1,
                        newActiveMs,
                        newLastEventAt,
                        state.limitMs(),
                        state.correctKeys(),
                        state.incorrectKeys() + 1,
                        state.backspaces(),
                        newTotalTyped,
                        state.finishedCorrect(),
                        newPerSec,
                        newLog
                );
            }
        }
    }
}
