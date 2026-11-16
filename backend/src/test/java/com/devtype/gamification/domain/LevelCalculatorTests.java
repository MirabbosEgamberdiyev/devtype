package com.devtype.gamification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LevelCalculatorTests {

    @Test
    @Tag("AC-GM-4")
    @DisplayName("AC-GM-4: Level thresholds match docs/11 §2 table exactly")
    void testLevelThresholdsFromSpec() {
        // Level 1: Kumulyativ 0, xpToNext 100
        assertThat(LevelCalculator.cumulativeXp(1)).isEqualTo(0);
        assertThat(LevelCalculator.xpToNext(1)).isEqualTo(100);

        // Level 2: Kumulyativ 100, xpToNext 283
        assertThat(LevelCalculator.cumulativeXp(2)).isEqualTo(100);
        assertThat(LevelCalculator.xpToNext(2)).isEqualTo(283);

        // Level 3: Kumulyativ 383, xpToNext 520
        assertThat(LevelCalculator.cumulativeXp(3)).isEqualTo(383);
        assertThat(LevelCalculator.xpToNext(3)).isEqualTo(520);

        // Level 5: Kumulyativ 1703, xpToNext 1118
        assertThat(LevelCalculator.cumulativeXp(5)).isEqualTo(1703);
        assertThat(LevelCalculator.xpToNext(5)).isEqualTo(1118);

        // Level 10: Kumulyativ 11106, xpToNext 3162
        assertThat(LevelCalculator.cumulativeXp(10)).isEqualTo(11106);
        assertThat(LevelCalculator.xpToNext(10)).isEqualTo(3162);

        // Level 20: xpToNext 8944, Kumulyativ sum_{i=1}^{19} xpToNext(i)
        assertThat(LevelCalculator.cumulativeXp(20)).isEqualTo(67135L);
        assertThat(LevelCalculator.xpToNext(20)).isEqualTo(8944);

        // Level 50: xpToNext 35355
        assertThat(LevelCalculator.xpToNext(50)).isEqualTo(35355);
    }

    @Test
    @Tag("AC-GM-4")
    @DisplayName("AC-GM-4: calculateLevel and getLevelInfo progression")
    void testLevelInfoProgression() {
        // 0 XP -> Level 1, 0 into level, 100 to next
        var info0 = LevelCalculator.getLevelInfo(0);
        assertThat(info0.level()).isEqualTo(1);
        assertThat(info0.xpIntoLevel()).isEqualTo(0);
        assertThat(info0.xpToNext()).isEqualTo(100);

        // 99 XP -> Level 1, 99 into level, 100 to next
        var info99 = LevelCalculator.getLevelInfo(99);
        assertThat(info99.level()).isEqualTo(1);
        assertThat(info99.xpIntoLevel()).isEqualTo(99);
        assertThat(info99.xpToNext()).isEqualTo(100);

        // 100 XP -> Level 2, 0 into level, 283 to next
        var info100 = LevelCalculator.getLevelInfo(100);
        assertThat(info100.level()).isEqualTo(2);
        assertThat(info100.xpIntoLevel()).isEqualTo(0);
        assertThat(info100.xpToNext()).isEqualTo(283);

        // 12480 XP -> Level 10
        // Kumulyativ for 10 is 11106. 12480 - 11106 = 1374 into level. xpToNext = 3162.
        // Matches the exact example in docs/14 §3.1:
        // "totalXp": 12480, "level": 10, "xpIntoLevel": 1374, "xpToNext": 3162
        var info12480 = LevelCalculator.getLevelInfo(12480);
        assertThat(info12480.level()).isEqualTo(10);
        assertThat(info12480.xpIntoLevel()).isEqualTo(1374);
        assertThat(info12480.xpToNext()).isEqualTo(3162);
    }
}
