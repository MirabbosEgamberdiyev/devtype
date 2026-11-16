package com.devtype.training.application;

import com.devtype.identity.api.GuestTokenVerifier;
import com.devtype.shared.error.DomainException;
import com.devtype.training.infrastructure.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ClaimGuestSessionsUseCase {

    private final SessionRepository sessionRepository;
    private final GuestTokenVerifier guestTokenVerifier;

    public ClaimGuestSessionsUseCase(
            SessionRepository sessionRepository,
            GuestTokenVerifier guestTokenVerifier) {
        this.sessionRepository = sessionRepository;
        this.guestTokenVerifier = guestTokenVerifier;
    }

    public record ClaimResult(int claimed) {}

    @Transactional
    public ClaimResult execute(Long userId, String guestToken) {
        if (userId == null) {
            throw new DomainException("UNAUTHORIZED", "Foydalanuvchi tizimga kirmagan", HttpStatus.UNAUTHORIZED);
        }

        Optional<UUID> guestIdOpt = guestTokenVerifier.verifyAndExtractGuestId(guestToken);
        if (guestIdOpt.isEmpty()) {
            throw new DomainException("INVALID_GUEST_TOKEN", "Yaroqsiz mehmon tokeni", HttpStatus.BAD_REQUEST);
        }

        int count = sessionRepository.claimGuestSessions(guestIdOpt.get(), userId);
        return new ClaimResult(count);
    }
}
