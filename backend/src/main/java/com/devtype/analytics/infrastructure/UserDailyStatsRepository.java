package com.devtype.analytics.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyStatsRepository extends JpaRepository<UserDailyStatsEntity, UserDailyStatsKey> {

    List<UserDailyStatsEntity> findByUserIdAndDayBetweenOrderByDayAsc(Long userId, LocalDate from, LocalDate to);

    List<UserDailyStatsEntity> findByUserIdAndLanguageIdAndDayBetweenOrderByDayAsc(Long userId, short languageId, LocalDate from, LocalDate to);

    Optional<UserDailyStatsEntity> findByUserIdAndDayAndLanguageIdAndModeGroup(Long userId, LocalDate day, short languageId, String modeGroup);

    @Query("SELECT d.day, SUM(d.timeMs), SUM(d.sessionsCnt), SUM(d.chars) FROM UserDailyStatsEntity d WHERE d.userId = :userId AND d.day >= :from AND d.day <= :to GROUP BY d.day ORDER BY d.day ASC")
    List<Object[]> sumActivityCalendarByUserId(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
