package com.devtype.training.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TargetPreparer Tests — docs/07 §1")
class TargetPreparerTests {

    @Test
    @DisplayName("strips carriage returns and trailing newlines")
    void testStripsCarriageReturnsAndTrailingNewlines() {
        String raw = "const a = 1;\r\nconst b = 2;\r\n\n";
        List<TargetChar> targets = TargetPreparer.prepareSingleTarget(raw, LanguageConfig.defaultConfig(), false, 0);

        assertThat(targets).allMatch(t -> t.ch() != '\r');
        assertThat(targets.get(targets.size() - 1).ch()).isEqualTo(';');
    }

    @Test
    @DisplayName("marks leading indentation as auto when strictIndent=false")
    void testAutoIndentation() {
        String code = "function foo() {\n  return 42;\n}";
        List<TargetChar> targets = TargetPreparer.prepareSingleTarget(code, new LanguageConfig("SPACE", 2, List.of()), false, 0);

        List<TargetChar> line1 = targets.stream().filter(t -> t.line() == 1).toList();
        assertThat(line1.get(0).ch()).isEqualTo(' ');
        assertThat(line1.get(0).auto()).isTrue();
        assertThat(line1.get(1).ch()).isEqualTo(' ');
        assertThat(line1.get(1).auto()).isTrue();
        assertThat(line1.get(2).ch()).isEqualTo('r');
        assertThat(line1.get(2).auto()).isFalse();
    }

    @Test
    @DisplayName("matches token patterns greedily and non-overlapping")
    void testTokenPatternsGreedy() {
        String code = "a === b && c == d";
        List<TargetChar> targets = TargetPreparer.prepareSingleTarget(
                code,
                new LanguageConfig("SPACE", 2, List.of("==", "===", "&&")),
                false,
                0
        );

        List<TargetChar> eq1 = targets.stream().filter(t -> t.col() >= 2 && t.col() <= 4).toList();
        assertThat(eq1).hasSize(3);
        assertThat(eq1.get(0).patternId()).isGreaterThanOrEqualTo(0);
        assertThat(eq1.get(0).patternId()).isEqualTo(eq1.get(1).patternId());
        assertThat(eq1.get(1).patternId()).isEqualTo(eq1.get(2).patternId());

        List<TargetChar> and = targets.stream().filter(t -> t.ch() == '&').toList();
        assertThat(and).hasSize(2);
        assertThat(and.get(0).patternId()).isGreaterThanOrEqualTo(0);
        assertThat(and.get(0).patternId()).isEqualTo(and.get(1).patternId());
        assertThat(and.get(0).patternId()).isNotEqualTo(eq1.get(0).patternId());
    }

    @Test
    @DisplayName("tracks word boundaries for wordId according to Rule 6")
    void testWordBoundaries() {
        String code = "t_ids.COUNT";
        List<TargetChar> targets = TargetPreparer.prepareSingleTarget(code, LanguageConfig.defaultConfig(), false, 0);

        int wordId1 = targets.get(0).wordId(); // 't'
        int wordIdDot = targets.stream().filter(t -> t.ch() == '.').findFirst().orElseThrow().wordId();
        int wordId2 = targets.stream().filter(t -> t.ch() == 'C').findFirst().orElseThrow().wordId();

        assertThat(wordIdDot).isEqualTo(wordId1 + 1);
        assertThat(wordId2).isEqualTo(wordIdDot + 1);
    }
}
