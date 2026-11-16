package com.devtype.analytics.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserBigramStatsRepository extends JpaRepository<UserBigramStatsEntity, UserBigramStatsKey> {
    List<UserBigramStatsEntity> findByUserIdAndLanguageId(Long userId, short languageId);
    Optional<UserBigramStatsEntity> findByUserIdAndLanguageIdAndToken(Long userId, short languageId, String token);
}
