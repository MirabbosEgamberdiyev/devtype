package com.devtype.analytics.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessedSessionRepository extends JpaRepository<ProcessedSessionEntity, UUID> {
}
