package com.devtype.identity.application;

import com.devtype.identity.api.UserView;

public record AuthResult(
        String accessToken,
        long expiresIn,
        String rawRefreshToken,
        UserView user,
        int claimedSessions
) {
}
