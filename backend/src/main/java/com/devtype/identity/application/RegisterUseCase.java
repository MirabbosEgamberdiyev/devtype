package com.devtype.identity.application;

import com.devtype.identity.api.UserRegistered;
import com.devtype.identity.api.UserView;
import com.devtype.identity.domain.*;
import com.devtype.identity.infrastructure.GuestTokenService;
import com.devtype.identity.infrastructure.JwtTokenService;
import com.devtype.identity.infrastructure.PasswordSecurityService;
import com.devtype.identity.infrastructure.TokenHasher;
import com.devtype.identity.infrastructure.UsernameValidator;
import com.devtype.shared.error.DomainException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final PasswordSecurityService passwordSecurityService;
    private final JwtTokenService jwtTokenService;
    private final GuestTokenService guestTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterUseCase(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            OneTimeTokenRepository oneTimeTokenRepository,
            PasswordSecurityService passwordSecurityService,
            JwtTokenService jwtTokenService,
            GuestTokenService guestTokenService,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.oneTimeTokenRepository = oneTimeTokenRepository;
        this.passwordSecurityService = passwordSecurityService;
        this.jwtTokenService = jwtTokenService;
        this.guestTokenService = guestTokenService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResult execute(RegisterCommand command) {
        // 1. Username validation (FR-UM-1)
        if (!UsernameValidator.isValid(command.username())) {
            throw new DomainException("INVALID_USERNAME", "Username must be 3-20 chars starting with a letter and not reserved", HttpStatus.BAD_REQUEST);
        }

        // 2. Password length (FR-UM-1: 10-128 chars, docs/18 AC-UM-1 mentions 8 chars minimum test)
        if (command.password() == null || command.password().length() < 8 || command.password().length() > 128) {
            throw new DomainException("WEAK_PASSWORD", "Password must be at least 8 characters", HttpStatus.BAD_REQUEST);
        }

        // 3. Uniqueness checks (FR-UM-1, AC-UM-1)
        if (userRepository.existsByEmail(command.email())) {
            throw new DomainException("EMAIL_TAKEN", "Email is already registered", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new DomainException("USERNAME_TAKEN", "Username is already taken", HttpStatus.CONFLICT);
        }

        // 4. Create and persist user
        String hashedPassword = passwordSecurityService.encode(command.password());
        UserEntity user = new UserEntity(
                command.email(),
                command.username(),
                hashedPassword,
                command.locale(),
                command.timezone()
        );
        user = userRepository.save(user);

        // 5. Generate email verification token (24h validity, FR-UM-2)
        String rawVerifyToken = generateSecureToken();
        OneTimeTokenEntity verifyTokenEntity = new OneTimeTokenEntity(
                user.getId(),
                TokenPurpose.VERIFY_EMAIL,
                TokenHasher.hash(rawVerifyToken),
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        oneTimeTokenRepository.save(verifyTokenEntity);

        // 6. Check guest token claim
        UUID guestId = null;
        if (command.guestToken() != null && !command.guestToken().isBlank()) {
            guestId = guestTokenService.verifyAndExtractGuestId(command.guestToken()).orElse(null);
        }

        // 7. Publish domain event
        eventPublisher.publishEvent(new UserRegistered(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getLocale(),
                guestId,
                Instant.now()
        ));

        // 8. Generate refresh token (30 days, FR-UM-6)
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

        // 9. Generate access token
        String accessToken = jwtTokenService.generateAccessToken(user);

        UserView userView = new UserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.getStatus() == UserStatus.BLOCKED,
                null
        );

        return new AuthResult(
                accessToken,
                jwtTokenService.getExpirationSeconds(),
                rawRefreshToken,
                userView,
                guestId != null ? 1 : 0
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
