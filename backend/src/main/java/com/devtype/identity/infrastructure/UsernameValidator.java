package com.devtype.identity.infrastructure;

import java.util.Set;
import java.util.regex.Pattern;

public final class UsernameValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{2,19}$");

    private static final Set<String> RESERVED_USERNAMES = Set.of(
            "admin", "api", "me", "settings", "root", "auth", "support",
            "system", "null", "undefined", "uzl", "uzc", "ru", "en",
            "practice", "typing", "leaderboard", "dashboard", "profile",
            "logout", "login", "register", "devtype", "help", "about",
            "terms", "privacy", "contact", "status", "healthz"
    );

    private UsernameValidator() {
    }

    public static boolean isValid(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return false;
        }
        return !RESERVED_USERNAMES.contains(username.toLowerCase());
    }
}
