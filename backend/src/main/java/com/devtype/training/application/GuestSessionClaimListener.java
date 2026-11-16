package com.devtype.training.application;

import com.devtype.identity.api.UserRegistered;
import com.devtype.training.infrastructure.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GuestSessionClaimListener {

    private static final Logger log = LoggerFactory.getLogger(GuestSessionClaimListener.class);

    private final SessionRepository sessionRepository;

    public GuestSessionClaimListener(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegistered event) {
        if (event.guestId() != null) {
            int claimed = sessionRepository.claimGuestSessions(event.guestId(), event.userId());
            log.info("Claimed {} guest sessions for newly registered user {}", claimed, event.userId());
        }
    }
}
