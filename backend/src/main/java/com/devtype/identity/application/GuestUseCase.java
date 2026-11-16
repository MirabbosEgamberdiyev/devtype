package com.devtype.identity.application;

import com.devtype.identity.infrastructure.GuestTokenService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GuestUseCase {

    private final GuestTokenService guestTokenService;

    public GuestUseCase(GuestTokenService guestTokenService) {
        this.guestTokenService = guestTokenService;
    }

    public GuestResult execute() {
        UUID guestId = UUID.randomUUID();
        String guestToken = guestTokenService.generateGuestToken(guestId);
        return new GuestResult(guestId, guestToken);
    }
}
