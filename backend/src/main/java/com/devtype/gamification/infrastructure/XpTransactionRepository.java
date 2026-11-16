package com.devtype.gamification.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface XpTransactionRepository extends JpaRepository<XpTransactionEntity, Long> {

    Optional<XpTransactionEntity> findByUserIdAndSourceTypeAndSourceId(Long userId, String sourceType, String sourceId);

    List<XpTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM XpTransactionEntity t WHERE t.userId = :userId")
    long sumAmountByUserId(@Param("userId") Long userId);
}
