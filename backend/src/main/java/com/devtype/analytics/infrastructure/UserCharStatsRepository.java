package com.devtype.analytics.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCharStatsRepository extends JpaRepository<UserCharStatsEntity, UserCharStatsKey> {
    List<UserCharStatsEntity> findByUserIdAndLanguageId(Long userId, short languageId);
    Optional<UserCharStatsEntity> findByUserIdAndLanguageIdAndToken(Long userId, short languageId, String token);
}
