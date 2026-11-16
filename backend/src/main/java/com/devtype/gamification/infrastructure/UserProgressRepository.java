package com.devtype.gamification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgressEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM UserProgressEntity p WHERE p.userId = :userId")
    Optional<UserProgressEntity> findByIdForUpdate(@Param("userId") Long userId);
}
