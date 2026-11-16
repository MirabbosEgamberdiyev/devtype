package com.devtype.training.domain;

import java.util.Map;
import java.util.Set;

/**
 * Character and Token Pattern Categories — docs/08 §2.
 * Strictly maps every printable ASCII character (0x20..0x7E), '\n', '\t'
 * and token patterns to exactly one category.
 */
public enum CharCategory {
    BRACKETS,
    PUNCT,
    ASSIGN_CMP,
    ARROWS,
    LOGICAL,
    ARITH,
    IDENT,
    QUOTES,
    WHITESPACE,
    CASE,
    ALNUM;

    private static final Map<String, CharCategory> KNOWN_PATTERN_CATEGORIES = Map.ofEntries(
            // ASSIGN_CMP: := == === != !== <= >= <> += -=
            Map.entry(":=", ASSIGN_CMP),
            Map.entry("==", ASSIGN_CMP),
            Map.entry("===", ASSIGN_CMP),
            Map.entry("!=", ASSIGN_CMP),
            Map.entry("!==", ASSIGN_CMP),
            Map.entry("<=", ASSIGN_CMP),
            Map.entry(">=", ASSIGN_CMP),
            Map.entry("<>", ASSIGN_CMP),
            Map.entry("+=", ASSIGN_CMP),
            Map.entry("-=", ASSIGN_CMP),

            // ARROWS: => -> <-
            Map.entry("=>", ARROWS),
            Map.entry("->", ARROWS),
            Map.entry("<-", ARROWS),

            // LOGICAL: && || ?? ?. ?:
            Map.entry("&&", LOGICAL),
            Map.entry("||", LOGICAL),
            Map.entry("??", LOGICAL),
            Map.entry("?.", LOGICAL),
            Map.entry("?:", LOGICAL),

            // ARITH: ++ -- ** //
            Map.entry("++", ARITH),
            Map.entry("--", ARITH),
            Map.entry("**", ARITH),
            Map.entry("//", ARITH)
    );

    private static final Set<CharCategory> SPECIAL_PRACTICE = Set.of(
            BRACKETS, PUNCT, ASSIGN_CMP, QUOTES, LOGICAL, ARITH, ARROWS, IDENT, CASE
    );

    public static CharCategory ofSingleChar(char ch) {
        if (ch == ' ' || ch == '\n' || ch == '\t') {
            return WHITESPACE;
        }
        if (ch >= 'A' && ch <= 'Z') {
            return CASE;
        }
        if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
            return ALNUM;
        }
        if (ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == '(' || ch == ')' || ch == '<' || ch == '>') {
            return BRACKETS;
        }
        if (ch == ';' || ch == ':' || ch == ',' || ch == '.') {
            return PUNCT;
        }
        if (ch == '=') {
            return ASSIGN_CMP;
        }
        if (ch == '!' || ch == '&' || ch == '|' || ch == '?') {
            return LOGICAL;
        }
        if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%' || ch == '^' || ch == '~') {
            return ARITH;
        }
        if (ch == '_' || ch == '$' || ch == '@' || ch == '#') {
            return IDENT;
        }
        if (ch == '\'' || ch == '"' || ch == '`' || ch == '\\') {
            return QUOTES;
        }
        return ALNUM;
    }

    public static CharCategory of(char ch, String pattern) {
        if (pattern != null && pattern.length() > 1) {
            CharCategory known = KNOWN_PATTERN_CATEGORIES.get(pattern);
            if (known != null) {
                return known;
            }
            return ofSingleChar(pattern.charAt(0));
        }
        return ofSingleChar(ch);
    }

    public boolean isSpecial() {
        return this != ALNUM && this != WHITESPACE && this != CASE;
    }
}
