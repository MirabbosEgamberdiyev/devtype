package com.devtype.identity.application;

import com.devtype.identity.api.UserView;
import com.devtype.identity.domain.*;
import com.devtype.identity.infrastructure.JwtTokenService;
import com.devtype.identity.infrastructure.PasswordSecurityService;
import com.devtype.identity.infrastructure.TokenHasher;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordSecurityService passwordSecurityService;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public LoginUseCase(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordSecurityService passwordSecurityService,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordSecurityService = passwordSecurityService;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResult execute(LoginCommand command) {
        Optional<UserEntity> userOpt = userRepository.findByLogin(command.login().trim());
        UserEntity user = userOpt.orElse(null);

        boolean passwordValid = passwordSecurityService.matches(
                command.password(),
                user != null ? user.getPasswordHash() : null
        );

        if (user == null || !passwordValid) {
            // Timing-safe generic error (FR-UM-3, AC-UM-3)
            throw new DomainException("INVALID_CREDENTIALS", "Email/username yoki parol noto'g'ri", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new DomainException("USER_BLOCKED", "Akkaunt bloklangan: " + user.getBlockedReason(), HttpStatus.FORBIDDEN);
        }

        // Reactivate if account deletion was scheduled within 30 days (FR-UM-11)
        if (user.getStatus() == UserStatus.DELETED && user.getDeletionScheduledAt() != null) {
            user.setStatus(UserStatus.ACTIVE);
            user.setDeletionScheduledAt(null);
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Generate new Refresh Token with fresh family_id (FR-UM-6)
        String rawRefreshToken = generateSecureToken();
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(
                user.getId(),
                TokenHasher.hash(rawRefreshToken),
                UUID.randomUUID(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                command.userAgent(),
                command.ip()
        );
        refreshTokenRepository.save(refreshTokenEntity);

        // Generate new Access Token
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
                rawRefreshToken,
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
