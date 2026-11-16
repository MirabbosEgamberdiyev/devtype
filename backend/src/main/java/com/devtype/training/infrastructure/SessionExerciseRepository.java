package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionExerciseRepository extends JpaRepository<SessionExerciseEntity, SessionExerciseKey> {

    List<SessionExerciseEntity> findBySessionIdOrderBySeqAsc(UUID sessionId);
}
