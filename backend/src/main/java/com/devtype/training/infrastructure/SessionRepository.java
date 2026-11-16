package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, SessionKey> {

    Optional<SessionEntity> findFirstById(UUID id);

    List<SessionEntity> findByUserIdAndStatusOrderByStartedAtAsc(Long userId, String status);

    List<SessionEntity> findByGuestIdAndStatusOrderByStartedAtAsc(UUID guestId, String status);

    @Query("SELECT s FROM SessionEntity s WHERE s.userId = :userId AND s.languageId = :languageId ORDER BY s.startedAt DESC LIMIT 20")
    List<SessionEntity> findRecentByUserAndLanguage(@Param("userId") Long userId, @Param("languageId") short languageId);

    @Query("SELECT s FROM SessionEntity s WHERE s.guestId = :guestId AND s.languageId = :languageId ORDER BY s.startedAt DESC LIMIT 20")
    List<SessionEntity> findRecentByGuestAndLanguage(@Param("guestId") UUID guestId, @Param("languageId") short languageId);

    @Modifying
    @Query("UPDATE SessionEntity s SET s.userId = :userId WHERE s.guestId = :guestId AND s.userId IS NULL")
    int claimGuestSessions(@Param("guestId") UUID guestId, @Param("userId") Long userId);
}
