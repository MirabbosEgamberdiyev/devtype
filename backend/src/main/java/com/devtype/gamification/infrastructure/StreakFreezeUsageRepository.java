package com.devtype.gamification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface StreakFreezeUsageRepository extends JpaRepository<StreakFreezeUsageEntity, StreakFreezeUsageKey> {
    List<StreakFreezeUsageEntity> findByUserIdAndDayBetween(Long userId, LocalDate from, LocalDate to);
}
