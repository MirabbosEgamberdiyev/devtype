package com.devtype.identity.application;

import com.devtype.identity.api.UserView;
import com.devtype.identity.domain.*;
import com.devtype.identity.infrastructure.JwtTokenService;
import com.devtype.identity.infrastructure.TokenHasher;
import com.devtype.shared.error.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenUseCase(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResult execute(String rawRefreshToken, String userAgent, String ip) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new DomainException("INVALID_TOKEN", "Refresh token is missing", HttpStatus.UNAUTHORIZED);
        }

        String tokenHash = TokenHasher.hash(rawRefreshToken);
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            throw new DomainException("INVALID_TOKEN", "Refresh token not found", HttpStatus.UNAUTHORIZED);
        }

        RefreshTokenEntity token = tokenOpt.get();

        // 1. Check for Token Reuse Attack (AC-UM-6, docs/16 §2.2)
        if (token.isReplaced() || token.isRevoked()) {
            log.warn("Security Alert: Refresh token reuse detected for familyId: {}, userId: {}! Revoking family.",
                    token.getFamilyId(), token.getUserId());
            refreshTokenRepository.revokeFamily(token.getFamilyId(), Instant.now());
            throw new DomainException("TOKEN_REUSE_DETECTED", "Refresh token reuse detected. All sessions revoked.", HttpStatus.UNAUTHORIZED);
        }

        // 2. Check Expiration
        if (token.isExpired()) {
            throw new DomainException("TOKEN_EXPIRED", "Refresh token has expired", HttpStatus.UNAUTHORIZED);
        }

        // 3. Check User status
        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", HttpStatus.UNAUTHORIZED));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new DomainException("USER_BLOCKED", "User is blocked", HttpStatus.FORBIDDEN);
        }

        // 4. Generate New Refresh Token (Rotation within same family)
        String newRawRefreshToken = generateSecureToken();
        RefreshTokenEntity newToken = new RefreshTokenEntity(
                user.getId(),
                TokenHasher.hash(newRawRefreshToken),
                token.getFamilyId(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                userAgent,
                ip
        );
        newToken = refreshTokenRepository.save(newToken);

        // 5. Mark previous token as replaced
        token.setReplacedBy(newToken.getId());
        token.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(token);

        // 6. Generate New Access Token
        String accessToken = jwtTokenService.generateAccessToken(user);

        UserView userView = new UserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.isEmailVerified(),
                false,
                null
        );

        return new AuthResult(
                accessToken,
                jwtTokenService.getExpirationSeconds(),
                newRawRefreshToken,
                userView,
                0
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
