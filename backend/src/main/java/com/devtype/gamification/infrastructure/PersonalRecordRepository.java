package com.devtype.gamification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PersonalRecordRepository extends JpaRepository<PersonalRecordEntity, PersonalRecordKey> {
    List<PersonalRecordEntity> findByUserId(Long userId);
    List<PersonalRecordEntity> findByUserIdAndLanguageId(Long userId, short languageId);
    Optional<PersonalRecordEntity> findByUserIdAndLanguageIdAndModeAndDurationLimitS(Long userId, short languageId, String mode, short durationLimitS);
}
