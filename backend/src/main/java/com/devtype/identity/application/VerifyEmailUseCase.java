package com.devtype.identity.application;

import com.devtype.identity.domain.*;
import com.devtype.identity.infrastructure.TokenHasher;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final OneTimeTokenRepository oneTimeTokenRepository;

    public VerifyEmailUseCase(UserRepository userRepository, OneTimeTokenRepository oneTimeTokenRepository) {
        this.userRepository = userRepository;
        this.oneTimeTokenRepository = oneTimeTokenRepository;
    }

    @Transactional
    public void execute(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new DomainException("TOKEN_INVALID", "Verification token is required", HttpStatus.GONE);
        }

        String tokenHash = TokenHasher.hash(rawToken);
        OneTimeTokenEntity token = oneTimeTokenRepository.findByTokenHashAndPurpose(tokenHash, TokenPurpose.VERIFY_EMAIL)
                .orElseThrow(() -> new DomainException("TOKEN_INVALID", "Verification token is invalid", HttpStatus.GONE));

        if (token.isUsed()) {
            throw new DomainException("TOKEN_USED", "Verification token has already been used", HttpStatus.GONE);
        }

        if (token.isExpired()) {
            throw new DomainException("TOKEN_EXPIRED", "Verification token has expired", HttpStatus.GONE);
        }

        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", HttpStatus.GONE));

        user.setEmailVerified(true);
        token.setUsedAt(Instant.now());

        userRepository.save(user);
        oneTimeTokenRepository.save(token);
    }
}
