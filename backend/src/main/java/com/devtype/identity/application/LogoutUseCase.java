package com.devtype.identity.application;

import com.devtype.identity.domain.RefreshTokenEntity;
import com.devtype.identity.domain.RefreshTokenRepository;
import com.devtype.identity.infrastructure.TokenHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = TokenHasher.hash(rawRefreshToken);
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);
        tokenOpt.ifPresent(token -> refreshTokenRepository.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    @Transactional
    public void executeAll(Long userId) {
        if (userId != null) {
            refreshTokenRepository.revokeAllForUser(userId, Instant.now());
        }
    }
}
