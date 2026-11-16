package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionCharStatsRepository extends JpaRepository<SessionCharStatsEntity, SessionCharStatsKey> {

    List<SessionCharStatsEntity> findBySessionId(UUID sessionId);
}
