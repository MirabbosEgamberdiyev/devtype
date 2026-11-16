package com.devtype.training.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CharCategory Tests — docs/08 §2 & AC-SC-1")
class CharCategoryTests {

    @Test
    @DisplayName("AC-SC-1: covers all 95 printable ASCII characters (0x20..0x7E) exactly into valid categories")
    void testAll95PrintableAsciiCharacters() {
        Map<CharCategory, Integer> counts = new EnumMap<>(CharCategory.class);
        for (CharCategory cat : CharCategory.values()) {
            counts.put(cat, 0);
        }

        for (int code = 0x20; code <= 0x7E; code++) {
            char ch = (char) code;
            CharCategory cat = CharCategory.ofSingleChar(ch);
            assertThat(cat).isNotNull();
            counts.put(cat, counts.get(cat) + 1);
        }

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(total).isEqualTo(95);

        // Specific category counts matching frontend & docs/08 §2 table:
        assertThat(counts.get(CharCategory.BRACKETS)).isEqualTo(8);
        assertThat(counts.get(CharCategory.PUNCT)).isEqualTo(4);
        assertThat(counts.get(CharCategory.ASSIGN_CMP)).isEqualTo(1);
        assertThat(counts.get(CharCategory.LOGICAL)).isEqualTo(4);
        assertThat(counts.get(CharCategory.ARITH)).isEqualTo(7);
        assertThat(counts.get(CharCategory.IDENT)).isEqualTo(4);
        assertThat(counts.get(CharCategory.QUOTES)).isEqualTo(4);
        assertThat(counts.get(CharCategory.WHITESPACE)).isEqualTo(1);
        assertThat(counts.get(CharCategory.CASE)).isEqualTo(26);
        assertThat(counts.get(CharCategory.ALNUM)).isEqualTo(36);
    }

    @Test
    @DisplayName("Special character check distinguishes special vs standard categories")
    void testIsSpecial() {
        assertThat(CharCategory.ALNUM.isSpecial()).isFalse();
        assertThat(CharCategory.WHITESPACE.isSpecial()).isFalse();
        assertThat(CharCategory.CASE.isSpecial()).isFalse();

        assertThat(CharCategory.BRACKETS.isSpecial()).isTrue();
        assertThat(CharCategory.PUNCT.isSpecial()).isTrue();
        assertThat(CharCategory.ASSIGN_CMP.isSpecial()).isTrue();
        assertThat(CharCategory.ARROWS.isSpecial()).isTrue();
        assertThat(CharCategory.LOGICAL.isSpecial()).isTrue();
        assertThat(CharCategory.ARITH.isSpecial()).isTrue();
        assertThat(CharCategory.IDENT.isSpecial()).isTrue();
        assertThat(CharCategory.QUOTES.isSpecial()).isTrue();
    }
}
