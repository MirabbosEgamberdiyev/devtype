package com.devtype.analytics.application;

import com.devtype.analytics.infrastructure.UserBigramStatsEntity;
import com.devtype.analytics.infrastructure.UserCharStatsEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WeaknessProfileTests {

    private final WeaknessProfileService service = new WeaknessProfileService(null, null, null);

    @Test
    @Tag("AC-AN-5")
    @DisplayName("AC-AN-5: If eligible rows < 8, returns empty profile (enoughData = false)")
    void testInsufficientRowsReturnsEmpty() {
        List<UserCharStatsEntity> chars = new ArrayList<>();
        // Only 5 rows
        for (int i = 0; i < 5; i++) {
            UserCharStatsEntity c = new UserCharStatsEntity(1L, (short) 1, String.valueOf((char) ('!' + i)));
            c.setExpectedCnt(50);
            c.setObservedSessions(5);
            c.setEwmaError(BigDecimal.valueOf(0.1));
            c.setEwmaMs(BigDecimal.valueOf(250.0));
            chars.add(c);
        }

        var profile = service.computeProfile(chars, List.of());
        assertThat(profile.enoughData()).isFalse();
        assertThat(profile.weakCharacters()).isEmpty();
    }

    @Test
    @Tag("AC-AN-5")
    @DisplayName("AC-AN-5: Alphanumeric characters with error < 0.08 are filtered out")
    void testAlnumFilteredOutWhenLowError() {
        List<UserCharStatsEntity> chars = new ArrayList<>();
        // 8 lowercase letters with low error (< 0.08)
        for (int i = 0; i < 8; i++) {
            UserCharStatsEntity c = new UserCharStatsEntity(1L, (short) 1, String.valueOf((char) ('a' + i)));
            c.setExpectedCnt(100);
            c.setObservedSessions(10);
            c.setEwmaError(BigDecimal.valueOf(0.02)); // < 0.08
            c.setEwmaMs(BigDecimal.valueOf(200.0));
            chars.add(c);
        }

        var profile = service.computeProfile(chars, List.of());
        assertThat(profile.enoughData()).isFalse(); // Because all were filtered out
    }

    @Test
    @Tag("AC-AN-5")
    @DisplayName("AC-AN-5: Weakness score ranks high-error/slow tokens > 1.0 at the top")
    void testWeaknessRanking() {
        List<UserCharStatsEntity> chars = new ArrayList<>();

        // 7 normal symbols (baseline: error = 0.02, ms = 200)
        for (int i = 0; i < 7; i++) {
            UserCharStatsEntity c = new UserCharStatsEntity(1L, (short) 1, String.valueOf((char) ('!' + i)));
            c.setExpectedCnt(50);
            c.setObservedSessions(5);
            c.setEwmaError(BigDecimal.valueOf(0.02));
            c.setEwmaMs(BigDecimal.valueOf(200.0));
            chars.add(c);
        }

        // 1 troubled symbol (e.g. '%' with 25% error and 800ms latency)
        UserCharStatsEntity troubled = new UserCharStatsEntity(1L, (short) 1, "%");
        troubled.setExpectedCnt(60);
        troubled.setObservedSessions(6);
        troubled.setEwmaError(BigDecimal.valueOf(0.25));
        troubled.setEwmaMs(BigDecimal.valueOf(800.0));
        chars.add(troubled);

        var profile = service.computeProfile(chars, List.of());
        assertThat(profile.enoughData()).isTrue();
        assertThat(profile.weakCharacters()).contains('%');
        assertThat(profile.weakCharacters().get(0)).isEqualTo('%');
    }
}
