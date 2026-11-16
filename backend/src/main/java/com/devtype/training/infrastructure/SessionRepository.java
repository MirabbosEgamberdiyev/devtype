package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, SessionKey> {

    Optional<SessionEntity> findFirstById(UUID id);
}
