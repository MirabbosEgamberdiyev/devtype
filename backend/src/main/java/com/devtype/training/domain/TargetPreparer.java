package com.devtype.training.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Target Preparation — docs/07 §1.
 * Pure deterministic function matching frontend/src/engine/prepare.ts 1-to-1.
 */
public final class TargetPreparer {

    private TargetPreparer() {
    }

    private static boolean isWordChar(char ch) {
        return (ch >= 'A' && ch <= 'Z') ||
               (ch >= 'a' && ch <= 'z') ||
               (ch >= '0' && ch <= '9') ||
               ch == '_';
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    private record RawCharEntry(char ch, int line, int col, boolean auto) {}

    public static List<TargetChar> prepareSingleTarget(
            String rawCode,
            LanguageConfig langConfig,
            boolean strictIndent,
            int exerciseSeq
    ) {
        if (rawCode == null || rawCode.isEmpty()) {
            return List.of();
        }

        // Rule 1: strip \r
        String code = rawCode.replace("\r", "");

        // Rule 4: strip trailing \n
        while (code.endsWith("\n")) {
            code = code.substring(0, code.length() - 1);
        }

        if (code.isEmpty()) {
            return List.of();
        }

        String[] lines = code.split("\n", -1);
        List<RawCharEntry> rawEntries = new ArrayList<>();

        // Sort patterns descending by length (greedy)
        List<String> sortedPatterns = new ArrayList<>(langConfig.tokenPatterns());
        sortedPatterns.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (int l = 0; l < lines.length; l++) {
            String lineText = lines[l];

            // Leading indent
            int firstNonWsIndex = 0;
            while (firstNonWsIndex < lineText.length() &&
                   (lineText.charAt(firstNonWsIndex) == ' ' || lineText.charAt(firstNonWsIndex) == '\t')) {
                firstNonWsIndex++;
            }

            for (int c = 0; c < lineText.length(); c++) {
                char ch = lineText.charAt(c);
                boolean isLeadingIndent = c < firstNonWsIndex;
                boolean auto = isLeadingIndent && !strictIndent;
                rawEntries.add(new RawCharEntry(ch, l, c, auto));
            }

            // Newline between lines
            if (l < lines.length - 1) {
                rawEntries.add(new RawCharEntry('\n', l, lineText.length(), false));
            }
        }

        // Pattern matching
        int[] patternIds = new int[rawEntries.size()];
        Arrays.fill(patternIds, -1);
        int nextPatternGroupId = 0;

        if (!sortedPatterns.isEmpty()) {
            int i = 0;
            while (i < rawEntries.size()) {
                boolean matched = false;
                for (String pattern : sortedPatterns) {
                    int patLen = pattern.length();
                    if (patLen > 0 && i + patLen <= rawEntries.size()) {
                        boolean matches = true;
                        for (int p = 0; p < patLen; p++) {
                            if (rawEntries.get(i + p).ch != pattern.charAt(p)) {
                                matches = false;
                                break;
                            }
                        }
                        if (matches) {
                            int grpId = nextPatternGroupId++;
                            for (int p = 0; p < patLen; p++) {
                                patternIds[i + p] = grpId;
                            }
                            i += patLen;
                            matched = true;
                            break;
                        }
                    }
                }
                if (!matched) {
                    i++;
                }
            }
        }

        // WordId assignment
        int currentWordId = 0;
        int[] wordIds = new int[rawEntries.size()];
        Character prevChar = null;

        for (int i = 0; i < rawEntries.size(); i++) {
            char ch = rawEntries.get(i).ch;

            if (i > 0 && prevChar != null) {
                char prev = prevChar;
                if (isWhitespace(prev)) {
                    if (!isWhitespace(ch)) {
                        currentWordId++;
                    }
                } else if (!isWhitespace(ch)) {
                    boolean prevIsWord = isWordChar(prev);
                    boolean currIsWord = isWordChar(ch);
                    if (prevIsWord != currIsWord) {
                        currentWordId++;
                    }
                }
            }

            wordIds[i] = currentWordId;
            prevChar = ch;
        }

        // Final TargetChar list
        List<TargetChar> targets = new ArrayList<>(rawEntries.size());
        for (int i = 0; i < rawEntries.size(); i++) {
            RawCharEntry entry = rawEntries.get(i);
            targets.add(new TargetChar(
                    entry.ch,
                    entry.line,
                    entry.col,
                    entry.auto,
                    wordIds[i],
                    patternIds[i],
                    exerciseSeq
            ));
        }

        return targets;
    }

    public static List<List<TargetChar>> prepareTargets(
            List<String> snippets,
            LanguageConfig langConfig,
            boolean strictIndent
    ) {
        List<List<TargetChar>> result = new ArrayList<>(snippets.size());
        for (int i = 0; i < snippets.size(); i++) {
            result.add(prepareSingleTarget(snippets.get(i), langConfig, strictIndent, i));
        }
        return result;
    }
}
