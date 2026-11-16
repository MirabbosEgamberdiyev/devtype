package com.devtype.training.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server Replayer — docs/07 §8.
 * Executes structural validations and replays Keylog events to produce official metrics.
 */
public final class KeylogReplayer {

    public record ReplaySessionConfig(
            EngineMode mode,
            ErrorMode errorMode,
            Integer limitSeconds,
            boolean strictIndent
    ) {}

    private KeylogReplayer() {
    }

    public static ReplayResult replay(
            ReplaySessionConfig config,
            List<String> exercises,
            LanguageConfig langConfig,
            String base64Keylog,
            SuspicionRules.SuspicionContext suspicionContext
    ) {
        KeylogDecoder.DecodedKeylog decoded = KeylogDecoder.decode(base64Keylog);
        KeylogDecoder.KeylogHeader header = decoded.header();

        // Structural header validations (docs/07 §9.1)
        if (header.strictIndent() != config.strictIndent()) {
            throw new IllegalArgumentException("KEYLOG_INVALID: strictIndent flag mismatch");
        }

        List<List<TargetChar>> targets = TargetPreparer.prepareTargets(
                exercises,
                langConfig,
                config.strictIndent()
        );

        Integer limitMs = config.limitSeconds() != null ? config.limitSeconds() * 1000 : null;
        EngineState state = EngineState.createInitial(targets, config.mode(), config.errorMode(), limitMs);

        // Track stats
        Map<String, int[]> charStatsRaw = new HashMap<>(); // [expected, errors, totalMs]
        Map<String, Map<String, Integer>> confusion = new HashMap<>();
        Map<String, Boolean> firstAttemptErrors = new HashMap<>();

        KeyEvent prevKey = null;
        boolean isFirstChar = true;
        boolean isAfterPauseOrNext = false;
        int currentSeq = 0;
        int pos = 0;

        for (KeyEvent ev : decoded.events()) {
            if (state.phase() == EnginePhase.FINISHED) {
                break;
            }

            // Timeout check for time-limited modes
            if (limitMs != null && ev.t() > limitMs && ev.kind() != KeyKind.TIMEOUT) {
                state = EngineReducer.reduce(state, KeyEvent.ofTimeout(limitMs));
                break;
            }

            // Structural event validations (docs/07 §9.1)
            validateStructuralEvent(ev, state);

            // Execute reduce
            state = EngineReducer.reduce(state, ev);

            // Analytical stats collection
            if (ev.kind() == KeyKind.PAUSE || ev.kind() == KeyKind.NEXT) {
                isAfterPauseOrNext = true;
                if (ev.kind() == KeyKind.NEXT) {
                    currentSeq++;
                    pos = 0;
                }
                prevKey = ev;
                continue;
            }
            if (ev.kind() == KeyKind.RESUME) {
                prevKey = ev;
                continue;
            }
            if (ev.kind() != KeyKind.CHAR && ev.kind() != KeyKind.ENTER && ev.kind() != KeyKind.TAB) {
                prevKey = ev;
                continue;
            }

            List<TargetChar> currentTargets = targets.isEmpty() ? List.of() : targets.get(currentSeq);
            while (pos < currentTargets.size() && currentTargets.get(pos).auto()) {
                pos++;
            }

            if (pos < currentTargets.size()) {
                char expChar = currentTargets.get(pos).ch();
                char typedChar = ev.kind() == KeyKind.ENTER ? '\n' : (ev.kind() == KeyKind.TAB ? '\t' : (ev.ch() != null ? ev.ch() : '\0'));
                String posKey = currentSeq + ":" + pos;
                String expKey = String.valueOf(expChar);
                String typedKey = String.valueOf(typedChar);

                int[] stat = charStatsRaw.computeIfAbsent(expKey, k -> new int[3]);
                boolean isFirstTry = !firstAttemptErrors.containsKey(posKey);

                if (isFirstTry) {
                    stat[0]++; // expected
                }

                if (typedChar != expChar) {
                    confusion.computeIfAbsent(expKey, k -> new HashMap<>())
                            .merge(typedKey, 1, Integer::sum);
                    if (isFirstTry) {
                        firstAttemptErrors.put(posKey, true);
                        stat[1]++; // errors
                    }
                } else {
                    if (isFirstTry) {
                        firstAttemptErrors.put(posKey, false);
                    }

                    if (!isFirstChar && !isAfterPauseOrNext && prevKey != null) {
                        int dt = ev.t() - prevKey.t();
                        if (dt > 0 && dt <= 5000) {
                            stat[2] += dt; // totalMs
                        }
                    }
                    pos++;
                }
            }

            isFirstChar = false;
            isAfterPauseOrNext = false;
            prevKey = ev;
        }

        // Final phase: if client disconnected without explicit finish, mark finished
        if (state.phase() != EnginePhase.FINISHED) {
            state = new EngineState(
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
                    state.activeMs(),
                    state.lastEventAt(),
                    state.limitMs(),
                    state.correctKeys(),
                    state.incorrectKeys(),
                    state.backspaces(),
                    state.totalTyped(),
                    state.finishedCorrect(),
                    state.perSecond(),
                    state.log()
            );
        }

        EngineMetrics metrics = MetricsCalculator.calculate(state);

        Map<String, ReplayResult.CharStat> charStats = new HashMap<>();
        charStatsRaw.forEach((k, v) -> charStats.put(k, new ReplayResult.CharStat(v[0], v[1], v[2])));

        SuspicionRules.SuspicionEvaluation suspicion = SuspicionRules.evaluate(
                metrics,
                decoded.events(),
                suspicionContext != null ? suspicionContext : SuspicionRules.SuspicionContext.empty()
        );

        return new ReplayResult(metrics, charStats, confusion, suspicion);
    }

    private static void validateStructuralEvent(KeyEvent ev, EngineState state) {
        // NEXT validation: only allowed when current snippet is fully completed
        if (ev.kind() == KeyKind.NEXT) {
            List<TargetChar> currentTargets = state.targets().isEmpty() ? List.of() : state.targets().get(state.seq());
            if (state.cursor() < currentTargets.size()) {
                throw new IllegalArgumentException("KEYLOG_INVALID: NEXT dispatched before completing snippet");
            }
            if (state.seq() >= state.targets().size() - 1) {
                throw new IllegalArgumentException("KEYLOG_INVALID: NEXT dispatched after last snippet");
            }
        }

        // PAUSE / RESUME validation: not allowed in FREE errorMode
        if (ev.kind() == KeyKind.PAUSE) {
            if (state.errorMode() == ErrorMode.FREE) {
                throw new IllegalArgumentException("KEYLOG_INVALID: PAUSE not allowed in FREE mode");
            }
            if (state.phase() == EnginePhase.PAUSED) {
                throw new IllegalArgumentException("KEYLOG_INVALID: consecutive PAUSE events without RESUME");
            }
        }

        if (ev.kind() == KeyKind.RESUME) {
            if (state.phase() != EnginePhase.PAUSED) {
                throw new IllegalArgumentException("KEYLOG_INVALID: RESUME without prior PAUSE");
            }
        }

        // FINISH_EARLY validation: only allowed in PRACTICE or CHALLENGE
        if (ev.kind() == KeyKind.FINISH_EARLY) {
            if (state.mode() != EngineMode.PRACTICE && state.mode() != EngineMode.CHALLENGE) {
                throw new IllegalArgumentException("KEYLOG_INVALID: FINISH_EARLY not allowed in mode " + state.mode());
            }
        }
    }
}
