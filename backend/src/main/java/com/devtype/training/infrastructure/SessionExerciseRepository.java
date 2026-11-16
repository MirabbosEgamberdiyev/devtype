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
}
