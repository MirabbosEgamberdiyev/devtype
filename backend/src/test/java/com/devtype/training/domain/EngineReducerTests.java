package com.devtype.training.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EngineReducer Tests — docs/07 §2, §3 & AC-TE-4, 5, 13")
class EngineReducerTests {

    @Test
    @DisplayName("transitions from idle to running on first typing keystroke")
    void testIdleToRunning() {
        List<List<TargetChar>> targets = TargetPreparer.prepareTargets(
                List.of("hello"),
                LanguageConfig.defaultConfig(),
                false
        );
        EngineState state = EngineState.createInitial(targets, EngineMode.PRACTICE, ErrorMode.STOP, false, null);
        assertThat(state.phase()).isEqualTo(EnginePhase.IDLE);

        EngineState running = EngineReducer.reduce(state, KeyEvent.ofChar(0, 'h'));
        assertThat(running.phase()).isEqualTo(EnginePhase.RUNNING);
        assertThat(running.cursor()).isEqualTo(1);
        assertThat(running.correctKeys()).isEqualTo(1);
        assertThat(running.states().get(0)).isEqualTo(CharState.CORRECT);
    }

    @Test
    @DisplayName("stop mode: cursor does not advance on incorrect char, marked corrected on success")
    void testStopModeBehavior() {
        List<List<TargetChar>> targets = TargetPreparer.prepareTargets(
                List.of("hello"),
                LanguageConfig.defaultConfig(),
                false
        );
        EngineState state = EngineState.createInitial(targets, EngineMode.PRACTICE, ErrorMode.STOP, false, null);
        EngineState s1 = EngineReducer.reduce(state, KeyEvent.ofChar(0, 'h'));

        EngineState wrong = EngineReducer.reduce(s1, KeyEvent.ofChar(100, 'x'));
        assertThat(wrong.cursor()).isEqualTo(1);
        assertThat(wrong.incorrectKeys()).isEqualTo(1);
        assertThat(wrong.everWrong().get(1)).isTrue();

        EngineState correct = EngineReducer.reduce(wrong, KeyEvent.ofChar(200, 'e'));
        assertThat(correct.cursor()).isEqualTo(2);
        assertThat(correct.states().get(1)).isEqualTo(CharState.CORRECTED);
        assertThat(correct.correctKeys()).isEqualTo(2);
    }

    @Test
    @DisplayName("AC-TE-13: free mode blocks input after 8 consecutive errors, unblocks after Backspace")
    void testFreeModeConsecutiveErrorsLimit() {
        List<List<TargetChar>> targets = TargetPreparer.prepareTargets(
                List.of("0123456789"),
                LanguageConfig.defaultConfig(),
                false
        );
        EngineState state = EngineState.createInitial(targets, EngineMode.SPEED_TEST, ErrorMode.FREE, false, null);
        EngineState s = state;

        for (int i = 0; i < 8; i++) {
            s = EngineReducer.reduce(s, KeyEvent.ofChar(i * 100, 'x'));
        }
        assertThat(s.cursor()).isEqualTo(8);
        assertThat(s.consecutiveErrors()).isEqualTo(8);

        // 9th keystroke blocked
        EngineState blocked = EngineReducer.reduce(s, KeyEvent.ofChar(800, 'x'));
        assertThat(blocked.cursor()).isEqualTo(8);
        assertThat(blocked.incorrectKeys()).isEqualTo(8);
        assertThat(blocked.totalTyped()).isEqualTo(8);

        // Backspace unblocks
        EngineState afterBs = EngineReducer.reduce(blocked, KeyEvent.ofBackspace(900));
        assertThat(afterBs.cursor()).isEqualTo(7);
        assertThat(afterBs.consecutiveErrors()).isEqualTo(7);

        EngineState resumed = EngineReducer.reduce(afterBs, KeyEvent.ofChar(1000, '7'));
        assertThat(resumed.cursor()).isEqualTo(8);
        assertThat(resumed.states().get(7)).isEqualTo(CharState.CORRECTED);
    }

    @Test
    @DisplayName("AC-TE-5: automatically skips leading indentation after enter")
    void testAutoIndentSkip() {
        String code = "fn() {\n  return 1;\n}";
        List<List<TargetChar>> targets = TargetPreparer.prepareTargets(
                List.of(code),
                LanguageConfig.defaultConfig(),
                false
        );
        EngineState s = EngineState.createInitial(targets, EngineMode.PRACTICE, ErrorMode.STOP, false, null);

        String line1 = "fn() {\n";
        for (int i = 0; i < line1.length(); i++) {
            char ch = line1.charAt(i);
            s = EngineReducer.reduce(s, ch == '\n' ? KeyEvent.ofEnter(i * 50) : KeyEvent.ofChar(i * 50, ch));
        }

        // Cursor should have jumped to position 9 ('r')
        assertThat(s.cursor()).isEqualTo(9);
        assertThat(s.targets().get(0).get(s.cursor()).ch()).isEqualTo('r');
    }

    @Test
    @DisplayName("AC-TE-4: pauses in stop mode and preserves active time")
    void testPausePreservesActiveTime() {
        List<List<TargetChar>> targets = TargetPreparer.prepareTargets(
                List.of("abc"),
                LanguageConfig.defaultConfig(),
                false
        );
        EngineState s = EngineState.createInitial(targets, EngineMode.PRACTICE, ErrorMode.STOP, false, null);

        s = EngineReducer.reduce(s, KeyEvent.ofChar(0, 'a'));
        assertThat(s.activeMs()).isEqualTo(0);

        s = EngineReducer.reduce(s, KeyEvent.ofPause(1000));
        assertThat(s.phase()).isEqualTo(EnginePhase.PAUSED);
        assertThat(s.activeMs()).isEqualTo(1000);

        s = EngineReducer.reduce(s, KeyEvent.ofResume(21000));
        assertThat(s.phase()).isEqualTo(EnginePhase.RUNNING);
        assertThat(s.activeMs()).isEqualTo(1000); // 20s pause was not added

        s = EngineReducer.reduce(s, KeyEvent.ofChar(21500, 'b'));
        assertThat(s.activeMs()).isEqualTo(1500);
    }
}
