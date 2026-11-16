package com.devtype.identity.application;

import com.devtype.identity.domain.OneTimeTokenEntity;
import com.devtype.identity.domain.OneTimeTokenRepository;
import com.devtype.identity.domain.TokenPurpose;
import com.devtype.identity.domain.UserRepository;
import com.devtype.identity.infrastructure.TokenHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class ForgotPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordUseCase.class);

    private final UserRepository userRepository;
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ForgotPasswordUseCase(UserRepository userRepository, OneTimeTokenRepository oneTimeTokenRepository) {
        this.userRepository = userRepository;
        this.oneTimeTokenRepository = oneTimeTokenRepository;
    }

    @Transactional
    public void execute(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            OneTimeTokenEntity resetToken = new OneTimeTokenEntity(
                    user.getId(),
                    TokenPurpose.RESET_PASSWORD,
                    TokenHasher.hash(rawToken),
                    Instant.now().plus(1, ChronoUnit.HOURS)
            );
            oneTimeTokenRepository.save(resetToken);

            log.info("Password reset token generated for userId: {} (expires in 1h)", user.getId());
        });
    }
}
