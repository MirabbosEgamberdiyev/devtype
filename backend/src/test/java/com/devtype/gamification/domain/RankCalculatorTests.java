package com.devtype.gamification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RankCalculatorTests {

    @Test
    @Tag("AC-GM-5")
    @DisplayName("AC-GM-5: Less than 10 CORE sessions results in UNRANKED")
    void testUnrankedWhenLessThan10Sessions() {
        var res = RankCalculator.evaluateRank(9, 75.0, LanguageRank.UNRANKED);
        assertThat(res.rank()).isEqualTo(LanguageRank.UNRANKED);
    }

    @Test
    @Tag("AC-GM-5")
    @DisplayName("AC-GM-5: Rank thresholds evaluated correctly on first qualification")
    void testInitialRankEvaluation() {
        assertThat(RankCalculator.evaluateRank(10, 25.0, LanguageRank.UNRANKED).rank()).isEqualTo(LanguageRank.BRONZE);
        assertThat(RankCalculator.evaluateRank(10, 30.0, LanguageRank.UNRANKED).rank()).isEqualTo(LanguageRank.SILVER);
        assertThat(RankCalculator.evaluateRank(10, 45.0, LanguageRank.UNRANKED).rank()).isEqualTo(LanguageRank.GOLD);
        assertThat(RankCalculator.evaluateRank(10, 60.0, LanguageRank.UNRANKED).rank()).isEqualTo(LanguageRank.PLATINUM);
        assertThat(RankCalculator.evaluateRank(10, 80.0, LanguageRank.UNRANKED).rank()).isEqualTo(LanguageRank.DIAMOND);
    }

    @Test
    @Tag("AC-GM-5")
    @DisplayName("AC-GM-5: Immediate promotion on meeting higher rank threshold")
    void testImmediatePromotion() {
        var res = RankCalculator.evaluateRank(15, 60.5, LanguageRank.GOLD);
        assertThat(res.rank()).isEqualTo(LanguageRank.PLATINUM);
        assertThat(res.changed()).isTrue();
    }

    @Test
    @Tag("AC-GM-5")
    @DisplayName("AC-GM-5: Hysteresis prevents demotion within 2 WPM buffer")
    void testHysteresisBuffer() {
        // Current rank GOLD (entry 45.0).
        // If recent WPM drops to 44.0 (within 2 WPM boundary: 45 - 2 = 43.0), should stay GOLD.
        var resStay = RankCalculator.evaluateRank(15, 44.0, LanguageRank.GOLD);
        assertThat(resStay.rank()).isEqualTo(LanguageRank.GOLD);
        assertThat(resStay.changed()).isFalse();

        // If recent WPM drops to 42.5 (< 43.0), should demote to SILVER
        var resDemote = RankCalculator.evaluateRank(15, 42.5, LanguageRank.GOLD);
        assertThat(resDemote.rank()).isEqualTo(LanguageRank.SILVER);
        assertThat(resDemote.changed()).isTrue();
    }
}
