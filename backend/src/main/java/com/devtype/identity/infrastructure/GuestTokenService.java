package com.devtype.identity.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class GuestTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final byte[] secretKey;
    private final long expirationDays;

    public GuestTokenService(
            @Value("${devtype.auth.guest-token-secret}") String guestSecret,
            @Value("${devtype.auth.guest-token-expiration-days:7}") long expirationDays) {
        this.secretKey = guestSecret.getBytes(StandardCharsets.UTF_8);
        this.expirationDays = expirationDays;
    }

    public String generateGuestToken(UUID guestId) {
        long now = Instant.now().getEpochSecond();
        String payload = guestId.toString() + "." + now;
        String signature = sign(payload);
        return payload + "." + signature;
    }

    public Optional<UUID> verifyAndExtractGuestId(String guestToken) {
        if (guestToken == null || guestToken.isBlank()) {
            return Optional.empty();
        }

        String[] parts = guestToken.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        String guestIdStr = parts[0];
        String timestampStr = parts[1];
        String providedSignature = parts[2];

        try {
            UUID guestId = UUID.fromString(guestIdStr);
            long issuedAt = Long.parseLong(timestampStr);
            long maxAgeSeconds = expirationDays * 86400;

            if (Instant.now().getEpochSecond() - issuedAt > maxAgeSeconds) {
                return Optional.empty(); // Token expired
            }

            String expectedPayload = guestIdStr + "." + timestampStr;
            String expectedSignature = sign(expectedPayload);

            if (MessageDigest.isEqual(
                    providedSignature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return Optional.of(guestId);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey, HMAC_ALGORITHM));
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC signature for guest token", e);
        }
    }
}
