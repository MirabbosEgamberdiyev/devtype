package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionExerciseRepository extends JpaRepository<SessionExerciseEntity, SessionExerciseKey> {

    List<SessionExerciseEntity> findBySessionIdOrderBySeqAsc(UUID sessionId);

    Optional<SessionExerciseEntity> findBySessionIdAndSeq(UUID sessionId, short seq);

    short countBySessionId(UUID sessionId);

    List<SessionExerciseEntity> findBySessionIdIn(Collection<UUID> sessionIds);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(se) FROM SessionExerciseEntity se " +
            "JOIN SessionEntity s ON s.id = se.sessionId " +
            "WHERE s.userId = :userId AND s.status = 'COMPLETED' AND se.exerciseId = :exerciseId AND s.finishedAt >= :since")
    int countCompletedForExerciseSince(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("exerciseId") Long exerciseId,
            @org.springframework.data.repository.query.Param("since") java.time.Instant since
    );
}
