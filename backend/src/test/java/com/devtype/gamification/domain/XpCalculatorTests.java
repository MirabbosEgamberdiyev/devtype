package com.devtype.gamification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class XpCalculatorTests {

    @Test
    @Tag("AC-GM-1")
    @DisplayName("AC-GM-1: Standard practice session calculation with 100% accuracy")
    void testStandardPractice() {
        // C = 500, PRACTICE (1.0), BEGINNER (1.0), 100% acc (1.25), repeat = 1.0
        // base = (500 / 10) * 1.0 * 1.0 * 1.25 * 1.0 = 50 * 1.25 = 62.5 -> round = 63
        var res = XpCalculator.calculate(
                500,
                "PRACTICE",
                List.of(new XpCalculator.SnippetWeight("BEGINNER", 500)),
                100.0,
                100.0,
                0,
                false,
                false,
                false,
                0
        );

        assertThat(res.earnedXp()).isEqualTo(63);
        assertThat(res.breakdown().modeCoef()).isEqualTo(1.0);
        assertThat(res.breakdown().diffCoef()).isEqualTo(1.0);
        assertThat(res.breakdown().accFactor()).isEqualTo(1.25);
        assertThat(res.breakdown().repeatFactor()).isEqualTo(1.0);
        assertThat(res.breakdown().capped()).isFalse();
    }

    @Test
    @Tag("AC-GM-1")
    @DisplayName("AC-GM-1: Accuracy factor piecewise transitions")
    void testAccuracyFactorPiecewise() {
        // Below 85% -> 0.25
        assertThat(XpCalculator.getAccFactor(84.9)).isEqualTo(0.25);
        assertThat(XpCalculator.getAccFactor(70.0)).isEqualTo(0.25);

        // 85.0% -> 0.5 + 0 = 0.5
        assertThat(XpCalculator.getAccFactor(85.0)).isEqualTo(0.5);

        // 90.0% -> 0.5 + (90 - 85) * 0.05 = 0.75
        assertThat(XpCalculator.getAccFactor(90.0)).isEqualTo(0.75);

        // 94.9% -> 0.5 + 9.9 * 0.05 = 0.995
        assertThat(XpCalculator.getAccFactor(94.9)).isCloseTo(0.995, org.assertj.core.data.Offset.offset(0.0001));

        // 95.0% to 97.9% -> 1.0
        assertThat(XpCalculator.getAccFactor(95.0)).isEqualTo(1.0);
        assertThat(XpCalculator.getAccFactor(97.5)).isEqualTo(1.0);

        // 98.0% to 99.9% -> 1.1
        assertThat(XpCalculator.getAccFactor(98.0)).isEqualTo(1.1);
        assertThat(XpCalculator.getAccFactor(99.9)).isEqualTo(1.1);

        // 100% -> 1.25
        assertThat(XpCalculator.getAccFactor(100.0)).isEqualTo(1.25);
    }

    @Test
    @Tag("AC-GM-1")
    @DisplayName("AC-GM-1: Mode coefficients verification")
    void testModeCoefficients() {
        assertThat(XpCalculator.getModeCoef("PRACTICE")).isEqualTo(1.0);
        assertThat(XpCalculator.getModeCoef("SPEED_TEST")).isEqualTo(1.2);
        assertThat(XpCalculator.getModeCoef("ACCURACY")).isEqualTo(1.3);
        assertThat(XpCalculator.getModeCoef("CHALLENGE")).isEqualTo(1.5);
        assertThat(XpCalculator.getModeCoef("SPECIAL_CHARS")).isEqualTo(1.0);
        assertThat(XpCalculator.getModeCoef("WEAKNESS")).isEqualTo(1.4);
        assertThat(XpCalculator.getModeCoef("CODE_SPRINT")).isEqualTo(1.5);
    }

    @Test
    @Tag("AC-GM-1")
    @DisplayName("AC-GM-1: Difficulty coefficients & weighted averages")
    void testDifficultyCoefficients() {
        assertThat(XpCalculator.getDiffCoef("SPECIAL_CHARS", List.of())).isEqualTo(1.0);
        assertThat(XpCalculator.getDiffCoef("SPEED_TEST", List.of())).isEqualTo(1.25);

        // Multi-snippet weighted average
        var weights = List.of(
                new XpCalculator.SnippetWeight("BEGINNER", 200), // 1.0 * 200 = 200
                new XpCalculator.SnippetWeight("EXPERT", 200)    // 2.0 * 200 = 400 -> total 600 / 400 = 1.5
        );
        assertThat(XpCalculator.getDiffCoef("PRACTICE", weights)).isEqualTo(1.5);
    }

    @Test
    @Tag("AC-GM-2")
    @DisplayName("AC-GM-2: Repeat factor: 1st=1.0, 2nd=0.5, 3rd+=0.1 in single snippet modes")
    void testRepeatFactor() {
        assertThat(XpCalculator.getRepeatFactor("PRACTICE", 0)).isEqualTo(1.0);
        assertThat(XpCalculator.getRepeatFactor("PRACTICE", 1)).isEqualTo(0.5);
        assertThat(XpCalculator.getRepeatFactor("PRACTICE", 2)).isEqualTo(0.1);
        assertThat(XpCalculator.getRepeatFactor("PRACTICE", 5)).isEqualTo(0.1);

        // In SPEED_TEST, repeatFactor is always 1.0
        assertThat(XpCalculator.getRepeatFactor("SPEED_TEST", 5)).isEqualTo(1.0);
    }

    @Test
    @Tag("AC-GM-2")
    @DisplayName("AC-GM-2: Daily cap of 3000 XP from sessions is enforced")
    void testDailyCap() {
        // C = 1000, CHALLENGE (1.5), EXPERT (2.0), 100% (1.25), repeat 1.0
        // base = 100 * 1.5 * 2.0 * 1.25 = 375 + bonuses(10 + 25) = 410
        // todaySessionXp = 2900 -> available cap is 100
        var res = XpCalculator.calculate(
                1000,
                "CHALLENGE",
                List.of(new XpCalculator.SnippetWeight("EXPERT", 1000)),
                100.0,
                100.0,
                0,
                true,
                true,
                false,
                2900
        );

        assertThat(res.earnedXp()).isEqualTo(100);
        assertThat(res.breakdown().capped()).isTrue();
    }

    @Test
    @Tag("AC-GM-1")
    @DisplayName("AC-GM-1: Bonuses added correctly: record (+25), firstOfDay (+10), sprint (+30)")
    void testBonuses() {
        var res = XpCalculator.calculate(
                100,
                "PRACTICE",
                List.of(new XpCalculator.SnippetWeight("BEGINNER", 100)),
                96.0,
                100.0,
                0,
                true,
                true,
                false,
                0
        );

        // base = 10 * 1.0 * 1.0 * 1.0 * 1.0 = 10
        // bonuses = 10 (firstOfDay) + 25 (record) = 35
        // total = 45
        assertThat(res.earnedXp()).isEqualTo(45);
        assertThat(res.breakdown().bonuses()).containsEntry("record", 25);
        assertThat(res.breakdown().bonuses()).containsEntry("firstOfDay", 10);
    }

    @Test
    @Tag("AC-GM-1")
    @DisplayName("AC-GM-1: Incomplete session gets base XP without completion bonuses")
    void testIncompleteSessionNoCompletionBonuses() {
        var res = XpCalculator.calculate(
                100,
                "PRACTICE",
                List.of(new XpCalculator.SnippetWeight("BEGINNER", 100)),
                96.0,
                50.0, // completionPct < 100%
                0,
                true, // isRecordBroken flag true but completion < 100
                true, // firstOfDay true
                false,
                0
        );

        // base = 10, firstOfDay = 10, record is ignored
        assertThat(res.earnedXp()).isEqualTo(20);
        assertThat(res.breakdown().bonuses()).doesNotContainKey("record");
        assertThat(res.breakdown().bonuses()).containsEntry("firstOfDay", 10);
    }
}
