package com.devtype.identity.infrastructure;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordSecurityService {

    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public PasswordSecurityService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        // Pre-computed hash to perform timing-safe dummy check when user is not found
        this.dummyHash = this.passwordEncoder.encode("devtype-timing-dummy-password");
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            // Constant-time mitigation
            passwordEncoder.matches(rawPassword, dummyHash);
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
