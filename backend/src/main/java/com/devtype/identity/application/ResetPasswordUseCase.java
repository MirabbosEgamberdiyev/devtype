package com.devtype.identity.application;

import com.devtype.identity.domain.*;
import com.devtype.identity.infrastructure.PasswordSecurityService;
import com.devtype.identity.infrastructure.TokenHasher;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordSecurityService passwordSecurityService;

    public ResetPasswordUseCase(
            UserRepository userRepository,
            OneTimeTokenRepository oneTimeTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordSecurityService passwordSecurityService) {
        this.userRepository = userRepository;
        this.oneTimeTokenRepository = oneTimeTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordSecurityService = passwordSecurityService;
    }

    @Transactional
    public void execute(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new DomainException("TOKEN_INVALID", "Reset token is required", HttpStatus.GONE);
        }

        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 128) {
            throw new DomainException("WEAK_PASSWORD", "Password must be at least 8 characters", HttpStatus.BAD_REQUEST);
        }

        String tokenHash = TokenHasher.hash(rawToken);
        OneTimeTokenEntity token = oneTimeTokenRepository.findByTokenHashAndPurpose(tokenHash, TokenPurpose.RESET_PASSWORD)
                .orElseThrow(() -> new DomainException("TOKEN_INVALID", "Reset token is invalid", HttpStatus.GONE));

        if (token.isUsed()) {
            throw new DomainException("TOKEN_USED", "Reset token has already been used", HttpStatus.GONE);
        }

        if (token.isExpired()) {
            throw new DomainException("TOKEN_EXPIRED", "Reset token has expired", HttpStatus.GONE);
        }

        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", HttpStatus.GONE));

        // Update password hash
        user.setPasswordHash(passwordSecurityService.encode(newPassword));
        userRepository.save(user);

        // Invalidate all existing refresh tokens (FR-UM-5, AC-UM-5)
        refreshTokenRepository.revokeAllForUser(user.getId(), Instant.now());

        // Mark token as used
        token.setUsedAt(Instant.now());
        oneTimeTokenRepository.save(token);
    }
}
