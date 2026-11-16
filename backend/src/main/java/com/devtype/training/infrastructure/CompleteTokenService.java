package com.devtype.training.infrastructure;

import com.devtype.shared.error.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class CompleteTokenService {

    private final byte[] secretKey;

    public CompleteTokenService(
            @Value("${devtype.security.complete-token-secret:devtype-vertical-slice-hmac-secret-key-32-bytes!}") String secret) {
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(UUID sessionId, Instant startedAt, Instant expiresAt) {
        String payload = sessionId + ":" + startedAt.toEpochMilli() + ":" + expiresAt.toEpochMilli();
        byte[] signature = sign(payload);
        String encPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String encSig = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        return encPayload + "." + encSig;
    }

    public void verifyToken(UUID expectedSessionId, String token, Instant expectedStartedAt, Instant expectedExpiresAt) {
        if (token == null || !token.contains(".")) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Complete token formati noto'g'ri", HttpStatus.UNAUTHORIZED);
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Complete token formati noto'g'ri", HttpStatus.UNAUTHORIZED);
        }

        String payload;
        byte[] actualSig;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            actualSig = Base64.getUrlDecoder().decode(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Token base64 dekodlanmadi", HttpStatus.UNAUTHORIZED);
        }

        byte[] expectedSig = sign(payload);
        if (!MessageDigest.isEqual(expectedSig, actualSig)) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Token imzosi noto'g'ri", HttpStatus.UNAUTHORIZED);
        }

        String[] fields = payload.split(":");
        if (fields.length != 3) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Token payload noto'g'ri", HttpStatus.UNAUTHORIZED);
        }

        UUID tokenSessionId = UUID.fromString(fields[0]);
        long tokenStartedAt = Long.parseLong(fields[1]);
        long tokenExpiresAt = Long.parseLong(fields[2]);

        if (!tokenSessionId.equals(expectedSessionId)) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Token sessiya identifikatori mos kelmadi", HttpStatus.UNAUTHORIZED);
        }
        if (tokenStartedAt != expectedStartedAt.toEpochMilli()) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Token boshlanish vaqti mos kelmadi", HttpStatus.UNAUTHORIZED);
        }
        if (tokenExpiresAt != expectedExpiresAt.toEpochMilli()) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Token tugash vaqti mos kelmadi", HttpStatus.UNAUTHORIZED);
        }
        if (Instant.now().isAfter(expectedExpiresAt)) {
            throw new DomainException("COMPLETE_TOKEN_INVALID", "Token muddati tugagan", HttpStatus.UNAUTHORIZED);
        }
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC imzo generatsiyasida xato", e);
        }
    }
}
