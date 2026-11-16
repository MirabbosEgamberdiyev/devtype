package com.devtype.training.application;

import com.devtype.shared.error.DomainException;
import com.devtype.training.infrastructure.SessionEntity;
import com.devtype.training.infrastructure.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AbandonSessionUseCase {

    private final SessionRepository sessionRepository;

    public AbandonSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public void execute(UUID sessionId) {
        SessionEntity session = sessionRepository.findFirstById(sessionId)
                .orElseThrow(() -> new DomainException("SESSION_NOT_FOUND", "Sessiya topilmadi: " + sessionId, HttpStatus.NOT_FOUND));

        if ("STARTED".equals(session.getStatus())) {
            session.setStatus("ABANDONED");
            sessionRepository.save(session);
        }
    }
}
