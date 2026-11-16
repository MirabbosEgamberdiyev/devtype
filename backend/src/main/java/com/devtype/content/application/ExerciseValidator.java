package com.devtype.content.application;

import com.devtype.content.domain.DifficultyLevel;
import com.devtype.content.domain.IndentStyle;
import com.devtype.content.domain.LanguageEntity;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ExerciseValidator {

    private static final List<String> FORBIDDEN_SQL_PATTERNS = List.of(
            "\\$\\$",
            "LANGUAGE\\s+plpgsql",
            "RAISE\\s+NOTICE",
            "RETURNS",
            "PERFORM",
            "DELIMITER",
            "DECLARE\\s+@"
    );

    public record ValidationResult(
            String codeHash,
            int charCount,
            int lineCount,
            short nestingDepth
    ) {}

    public ValidationResult validate(String code, DifficultyLevel difficulty, LanguageEntity language) {
        if (code == null || code.isBlank()) {
            throw new DomainException("CODE_EMPTY", "Exercise code cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // Must not contain CRLF
        if (code.contains("\r")) {
            throw new DomainException("INVALID_LINE_ENDINGS", "Code must use Unix LF line endings only", HttpStatus.BAD_REQUEST);
        }

        String[] lines = code.split("\n", -1);
        int lineCount = lines.length;
        int charCount = code.length();

        // 1. Length range by difficulty (BL-EM-1)
        int minChars;
        int maxChars;
        switch (difficulty) {
            case BEGINNER -> { minChars = 30; maxChars = 120; }
            case INTERMEDIATE -> { minChars = 80; maxChars = 300; }
            case ADVANCED -> { minChars = 200; maxChars = 600; }
            case EXPERT -> { minChars = 400; maxChars = 1000; }
            default -> throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }

        if (charCount < minChars || charCount > maxChars) {
            throw new DomainException(
                    "INVALID_CODE_LENGTH",
                    "Code length (" + charCount + ") is out of range [" + minChars + ", " + maxChars + "] for " + difficulty,
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2. Line length <= 80 and trailing whitespace (BL-EM-1)
        int maxDepth = 0;
        int consecutiveEmptyLines = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.length() > 80) {
                throw new DomainException(
                        "LINE_TOO_LONG",
                        "Line " + (i + 1) + " exceeds maximum 80 characters (found " + line.length() + ")",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (line.endsWith(" ") || line.endsWith("\t")) {
                throw new DomainException(
                        "TRAILING_WHITESPACE",
                        "Line " + (i + 1) + " contains trailing whitespace",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (language.getIndentStyle() == IndentStyle.SPACE && line.contains("\t")) {
                throw new DomainException(
                        "TAB_DISALLOWED",
                        "Line " + (i + 1) + " contains TAB character; language requires spaces",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (line.trim().isEmpty()) {
                consecutiveEmptyLines++;
                if (consecutiveEmptyLines > 1) {
                    throw new DomainException(
                            "CONSECUTIVE_BLANK_LINES",
                            "Multiple consecutive blank lines at line " + (i + 1),
                            HttpStatus.BAD_REQUEST
                    );
                }
            } else {
                consecutiveEmptyLines = 0;
            }

            // Estimate nesting depth from indentation
            int leadingSpaces = 0;
            while (leadingSpaces < line.length() && line.charAt(leadingSpaces) == ' ') {
                leadingSpaces++;
            }
            int depth = language.getIndentSize() > 0 ? leadingSpaces / language.getIndentSize() : 0;
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }

        // 3. Dialect-specific forbidden patterns (BL-EM-3)
        String slug = language.getSlug();
        if ("sql".equals(slug) || "plsql".equals(slug)) {
            for (String forbiddenRegex : FORBIDDEN_SQL_PATTERNS) {
                Pattern p = Pattern.compile(forbiddenRegex, Pattern.CASE_INSENSITIVE);
                if (p.matcher(code).find()) {
                    throw new DomainException(
                            "FORBIDDEN_PATTERN",
                            "Code contains disallowed syntax for " + slug + ": " + forbiddenRegex,
                            HttpStatus.BAD_REQUEST
                    );
                }
            }
        }

        // 4. Compute SHA-256 code hash
        String codeHash = sha256Hex(code);

        return new ValidationResult(codeHash, charCount, lineCount, (short) maxDepth);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm missing", e);
        }
    }
}
