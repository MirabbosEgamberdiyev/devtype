package com.devtype.identity.application;

import java.util.UUID;

public record GuestResult(UUID guestId, String guestToken) {
}
