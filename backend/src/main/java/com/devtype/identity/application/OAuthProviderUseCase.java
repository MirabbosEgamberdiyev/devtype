package com.devtype.identity.application;

import com.devtype.identity.api.UserView;
import com.devtype.identity.domain.*;
import com.devtype.identity.infrastructure.JwtTokenService;
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
public class OAuthProviderUseCase {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthProviderUseCase(
            UserRepository userRepository,
            OAuthAccountRepository oauthAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResult execute(String provider, String email, String providerUid, String displayName, String userAgent, String ip) {
        String normalizedProvider = provider.toUpperCase().trim();
        if (!"GITHUB".equals(normalizedProvider) && !"GOOGLE".equals(normalizedProvider)) {
            throw new DomainException("PROVIDER_NOT_SUPPORTED", "OAuth provider not supported: " + provider, HttpStatus.BAD_REQUEST);
        }

        Optional<OAuthAccountEntity> oauthOpt = oauthAccountRepository.findByProviderAndProviderUid(normalizedProvider, providerUid);
        UserEntity user;

        if (oauthOpt.isPresent()) {
            user = userRepository.findById(oauthOpt.get().getUserId())
                    .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", HttpStatus.UNAUTHORIZED));
        } else {
            Optional<UserEntity> existingUserOpt = userRepository.findByEmail(email.trim());
            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
            } else {
                String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
                if (baseUsername.length() < 3) baseUsername = "dev_" + baseUsername;
                if (baseUsername.length() > 15) baseUsername = baseUsername.substring(0, 15);
                if (!Character.isLetter(baseUsername.charAt(0))) baseUsername = "u_" + baseUsername;

                String candidateUsername = baseUsername;
                int suffix = 1;
                while (userRepository.existsByUsername(candidateUsername)) {
                    candidateUsername = baseUsername + suffix++;
                }

                user = new UserEntity(
                        email.trim().toLowerCase(),
                        candidateUsername.toLowerCase(),
                        null,
                        "en",
                        "UTC"
                );
                user.setEmailVerified(true);
                user.setDisplayName(displayName);
                user = userRepository.save(user);
            }

            OAuthAccountEntity newLink = new OAuthAccountEntity(user.getId(), normalizedProvider, providerUid);
            oauthAccountRepository.save(newLink);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new DomainException("USER_BLOCKED", "User is blocked", HttpStatus.FORBIDDEN);
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String rawRefreshToken = generateSecureToken();
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(
                user.getId(),
                TokenHasher.hash(rawRefreshToken),
                UUID.randomUUID(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                userAgent,
                ip
        );
        refreshTokenRepository.save(refreshTokenEntity);

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

        return new AuthResult(accessToken, jwtTokenService.getExpirationSeconds(), rawRefreshToken, userView, 0);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
