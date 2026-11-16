package com.devtype.identity.api;

import java.util.Optional;
import java.util.UUID;

public interface GuestTokenVerifier {
    Optional<UUID> verifyAndExtractGuestId(String guestToken);
}
