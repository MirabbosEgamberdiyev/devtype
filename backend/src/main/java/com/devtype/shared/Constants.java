package com.devtype.shared;

import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

/**
 * Kanonik konstantalar — docs/00-conventions.md §4.
 * Kodda ushbu qiymatlar yagona manba sifatida xizmat qiladi.
 */
public final class Constants {

    private Constants() {}

    // 4.1 Tillar va lokallar
    public static final List<String> SUPPORTED_LOCALES = List.of("uzl", "uzc", "ru", "en");
    public static final String SOURCE_LOCALE = "en";
    public static final List<String> RELEASE_1_LANGUAGES = List.of("java", "python", "javascript", "sql", "plsql");
    public static final List<String> RELEASE_2_LANGUAGES = List.of("typescript", "csharp", "go", "kotlin", "cpp", "bash");
    public static final List<String> RELEASE_3_LANGUAGES = List.of("c", "php", "rust", "html", "css");
    public static final Set<String> ALL_SUPPORTED_LANGUAGES = Set.of(
        "java", "python", "javascript", "typescript", "sql", "plsql",
        "c", "cpp", "csharp", "php", "go", "kotlin", "rust", "html", "css", "bash"
    );

    // 4.2 Kontent
    public static final int MAX_LINE_LENGTH = 80;
    public static final int MIN_SNIPPETS_PER_TOPIC_LEVEL = 5;
    public static final int MIN_OBSERVATIONS_FOR_STATS = 30;
    public static final int RECENT_EXCLUDED_SESSIONS_COUNT = 20;

    // 4.3 O'quv progressi
    public static final double COMPLETION_ACCURACY_THRESHOLD = 95.0;
    public static final double MASTERED_ACCURACY_THRESHOLD = 98.0;
    public static final double LEVEL_UNLOCK_COMPLETION_PERCENT = 80.0;
    public static final double ACCURACY_MODE_MIN_ACCURACY = 97.0;

    // 4.4 Rejimlar va Chegaralar
    public static final int FREE_MODE_CONSECUTIVE_ERRORS_LIMIT = 8;
    public static final int INACTIVITY_PAUSE_SECONDS = 15;
    public static final int HIDDEN_PAGE_ABANDON_SECONDS = 2;
    public static final int SESSION_EXPIRATION_HOURS = 24;
    public static final int MAX_OPEN_STARTED_SESSIONS_PER_USER = 3;
    public static final int CODE_SPRINT_SNIPPET_COUNT = 5;
    public static final int WEAKNESS_SNIPPET_COUNT = 3;
    public static final int SPECIAL_CHARS_TARGET_LENGTH = 150;

    // 4.5 Metrikalar va yaxlitlash
    public static final int METRIC_SCALE = 2;
    public static final RoundingMode METRIC_ROUNDING = RoundingMode.HALF_UP;

    // 4.6 Validatsiya (Anti-cheat)
    public static final long TIMING_TOLERANCE_MS = 2000L;
    public static final double SUSPICIOUS_WPM_THRESHOLD = 220.0;
    public static final int SUSPICION_FLAG_THRESHOLD = 50;
    public static final int MAX_KEYLOG_REQUEST_BYTES = 256 * 1024;
    public static final int MAX_KEYLOG_DECOMPRESSED_BYTES = 1024 * 1024;
    public static final int MAX_KEYLOG_EVENTS = 50000;
    public static final int MIN_SESSION_KEYSTROKES = 20;
    public static final int MIN_SESSION_DURATION_SECONDS = 5;

    // 4.7 Gamification
    public static final int DAILY_XP_CAP = 3000;
    public static final int STREAK_MIN_ACTIVE_SECONDS = 180;
    public static final int STREAK_FREEZES_PER_MONTH = 2;
    public static final double LEVEL_POWER = 1.5;
    public static final int BONUS_PERSONAL_RECORD_XP = 25;
    public static final int BONUS_FIRST_SESSION_OF_DAY_XP = 10;
    public static final int BONUS_SPRINT_COMPLETION_XP = 30;
    public static final int BONUS_DAILY_PLAN_3_OF_3_XP = 20;
    public static final int BONUS_GOAL_ACHIEVED_XP = 100;

    // 4.9 Xavfsizlik
    public static final int PASSWORD_MIN_LENGTH = 10;
    public static final int PASSWORD_MAX_LENGTH = 128;
    public static final int ACCESS_TOKEN_TTL_MINUTES = 15;
    public static final int REFRESH_TOKEN_TTL_DAYS = 30;
    public static final int GUEST_TOKEN_TTL_DAYS = 7;
    public static final int EMAIL_VERIFICATION_TOKEN_TTL_HOURS = 24;
    public static final int PASSWORD_RESET_TOKEN_TTL_HOURS = 1;

    // 4.10 Unumdorlik mo'ljallari
    public static final long KEYPRESS_LATENCY_P95_MS = 16L;
    public static final long API_P95_MS = 200L;
    public static final long COMPLETE_API_P95_MS = 400L;
}
