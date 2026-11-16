package com.devtype.analytics.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserLanguageStatsRepository extends JpaRepository<UserLanguageStatsEntity, UserLanguageStatsKey> {
    List<UserLanguageStatsEntity> findByUserId(Long userId);
    List<UserLanguageStatsEntity> findByUserIdAndModeGroup(Long userId, String modeGroup);
    Optional<UserLanguageStatsEntity> findByUserIdAndLanguageIdAndModeGroup(Long userId, short languageId, String modeGroup);
}
