package com.devtype.analytics.application;

import com.devtype.analytics.infrastructure.*;
import com.devtype.content.api.LanguageCatalog;
import com.devtype.gamification.api.GamificationQuery;
import com.devtype.identity.api.UserDirectory;
import com.devtype.identity.api.UserView;
import com.devtype.training.api.SessionQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class UserDataExportService {

    private final UserDirectory userDirectory;
    private final GamificationQuery gamificationQuery;
    private final SessionQuery sessionQuery;
    private final UserLanguageStatsRepository languageStatsRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final UserCharStatsRepository charStatsRepository;
    private final UserBigramStatsRepository bigramStatsRepository;
    private final LanguageCatalog languageCatalog;

    public UserDataExportService(
            UserDirectory userDirectory,
            GamificationQuery gamificationQuery,
            SessionQuery sessionQuery,
            UserLanguageStatsRepository languageStatsRepository,
            UserDailyStatsRepository dailyStatsRepository,
            UserCharStatsRepository charStatsRepository,
            UserBigramStatsRepository bigramStatsRepository,
            LanguageCatalog languageCatalog
    ) {
        this.userDirectory = userDirectory;
        this.gamificationQuery = gamificationQuery;
        this.sessionQuery = sessionQuery;
        this.languageStatsRepository = languageStatsRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.charStatsRepository = charStatsRepository;
        this.bigramStatsRepository = bigramStatsRepository;
        this.languageCatalog = languageCatalog;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportUserData(Long userId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exportedAt", Instant.now().toString());

        // Profile
        UserView user = userDirectory.findById(userId).orElse(null);
        if (user != null) {
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("id", user.id());
            profile.put("username", user.username());
            profile.put("email", user.email());
            profile.put("role", user.role());
            profile.put("emailVerified", user.emailVerified());
            profile.put("timezone", user.timezone());
            data.put("profile", profile);
        }

        // Gamification
        var gamification = gamificationQuery.getSummary(userId);
        data.put("gamification", gamification);

        // Language stats
        var langStats = languageStatsRepository.findByUserId(userId).stream().map(s -> {
            String slug = languageCatalog.getPublishedLanguageSlugs().stream()
                    .filter(l -> languageCatalog.findLanguageIdBySlug(l).orElse((short) -1) == s.getLanguageId())
                    .findFirst().orElse("unknown");
            return Map.of(
                    "language", slug,
                    "modeGroup", s.getModeGroup(),
                    "sessionsCount", s.getSessionsCnt(),
                    "totalChars", s.getTotalChars(),
                    "totalTimeMs", s.getTotalTimeMs(),
                    "maxWpm", s.getMaxWpm(),
                    "recentWpm", s.getRecentWpm() != null ? s.getRecentWpm() : 0.0
            );
        }).toList();
        data.put("languageStats", langStats);

        // Sessions
        var sessions = sessionQuery.findSessions(userId, null, null, null, null, null, 100);
        data.put("sessions", sessions);

        return data;
    }
}
