package com.devtype.training.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Engine Vectors — AC-TE-1 (Backend Execution)")
class VectorTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public record VectorModel(
            String name,
            LanguageModel language,
            String mode,
            Integer limitMs,
            boolean strictIndent,
            List<String> exercises,
            List<EventModel> events,
            ExpectModel expect
    ) {}

    public record LanguageModel(
            String indentStyle,
            int indentSize,
            List<String> tokenPatterns
    ) {}

    public record EventModel(
            int t,
            String k,
            Character ch
    ) {}

    public record ExpectModel(
            String phase,
            int cursor,
            int seq,
            String states,
            MetricsModel metrics
    ) {}

    public record MetricsModel(
            BigDecimal wpm,
            BigDecimal rawWpm,
            BigDecimal accuracy,
            int correctChars,
            int errors,
            int backspaces
    ) {}

    static Stream<File> vectorFiles() {
        File dir = new File("../engine-vectors");
        if (!dir.exists() || !dir.isDirectory()) {
            dir = new File("engine-vectors");
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        assertThat(files).isNotNull();
        assertThat(files.length).isGreaterThanOrEqualTo(40);
        Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));
        return Arrays.stream(files);
    }

    private static String statesToString(EngineState state) {
        List<TargetChar> currentTargets = state.targets().isEmpty() ? List.of() : state.targets().get(state.seq());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentTargets.size(); i++) {
            TargetChar tc = currentTargets.get(i);
            if (tc.auto()) {
                sb.append('a');
            } else {
                CharState s = state.states().get(i);
                switch (s) {
                    case CORRECT -> sb.append('c');
                    case INCORRECT -> sb.append('i');
                    case CORRECTED -> sb.append('r');
                    default -> sb.append('p');
                }
            }
        }
        return sb.toString();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("vectorFiles")
    void testVector(File vectorFile) throws IOException {
        VectorModel v = MAPPER.readValue(vectorFile, VectorModel.class);

        LanguageConfig langConfig = new LanguageConfig(
                v.language().indentStyle(),
                v.language().indentSize(),
                v.language().tokenPatterns() != null ? v.language().tokenPatterns() : List.of()
        );

        List<List<TargetChar>> targets = TargetPreparer.prepareTargets(
                v.exercises(),
                langConfig,
                v.strictIndent()
        );

        EngineMode mode = EngineMode.valueOf(v.mode());
        ErrorMode errorMode = mode.defaultErrorMode();

        EngineState state = EngineState.createInitial(
                targets,
                mode,
                errorMode,
                v.strictIndent(),
                v.limitMs()
        );

        for (EventModel ev : v.events()) {
            KeyKind kind = switch (ev.k()) {
                case "char" -> KeyKind.CHAR;
                case "enter" -> KeyKind.ENTER;
                case "tab" -> KeyKind.TAB;
                case "backspace" -> KeyKind.BACKSPACE;
                case "wordDelete" -> KeyKind.WORD_DELETE;
                case "pause" -> KeyKind.PAUSE;
                case "resume" -> KeyKind.RESUME;
                case "next" -> KeyKind.NEXT;
                case "finishEarly" -> KeyKind.FINISH_EARLY;
                case "timeout" -> KeyKind.TIMEOUT;
                default -> throw new IllegalArgumentException("Unknown kind " + ev.k());
            };
            KeyEvent keyEvent = new KeyEvent(ev.t(), kind, ev.ch());
            state = EngineReducer.reduce(state, keyEvent);
        }

        // Check phase, cursor, seq
        assertThat(state.phase().name().toLowerCase()).isEqualTo(v.expect().phase().toLowerCase());
        assertThat(state.cursor()).isEqualTo(v.expect().cursor());
        assertThat(state.seq()).isEqualTo(v.expect().seq());

        // Check states string
        String actualStates = statesToString(state);
        assertThat(actualStates).isEqualTo(v.expect().states());

        // Check metrics
        EngineMetrics m = MetricsCalculator.calculate(state);
        assertThat(m.wpm()).isEqualByComparingTo(v.expect().metrics().wpm());
        assertThat(m.accuracy()).isEqualByComparingTo(v.expect().metrics().accuracy());
        assertThat(m.correctChars()).isEqualTo(v.expect().metrics().correctChars());
        assertThat(m.incorrectKeys()).isEqualTo(v.expect().metrics().errors());
        assertThat(m.backspaceCount()).isEqualTo(v.expect().metrics().backspaces());
    }
}
