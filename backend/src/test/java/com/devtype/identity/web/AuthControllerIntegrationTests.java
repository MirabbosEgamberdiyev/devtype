package com.devtype.identity.web;

import com.devtype.identity.application.GuestResult;
import com.devtype.identity.domain.*;
import com.devtype.identity.infrastructure.TokenHasher;
import com.devtype.identity.presentation.AuthDtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Identity Auth Controller Integration Tests")
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OneTimeTokenRepository oneTimeTokenRepository;

    @Nested
    @DisplayName("Registration Tests (AC-UM-1, AC-UM-7)")
    class RegistrationTests {

        @Test
        @Tag("AC-UM-1")
        @DisplayName("AC-UM-1: Successful registration returns 201, JWT and refresh cookie")
        void successfulRegistration() throws Exception {
            String uniqueEmail = "user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String uniqueUsername = "u_" + UUID.randomUUID().toString().substring(0, 8);

            RegisterRequest request = new RegisterRequest(
                    uniqueEmail,
                    uniqueUsername,
                    "SecurePassword123!",
                    "uzl",
                    "Asia/Tashkent",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.expiresIn").value(900))
                    .andExpect(jsonPath("$.user.username").value(uniqueUsername.toLowerCase()))
                    .andExpect(jsonPath("$.user.email").value(uniqueEmail.toLowerCase()))
                    .andExpect(cookie().exists("dt_refresh"))
                    .andExpect(cookie().httpOnly("dt_refresh", true));
        }

        @Test
        @Tag("AC-UM-1")
        @DisplayName("AC-UM-1: Duplicate email returns 409 EMAIL_TAKEN")
        void duplicateEmailFails() throws Exception {
            String email = "dup_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String u1 = "u_" + UUID.randomUUID().toString().substring(0, 8);
            String u2 = "u_" + UUID.randomUUID().toString().substring(0, 8);

            RegisterRequest r1 = new RegisterRequest(email, u1, "Password123!", "en", "UTC", null);
            RegisterRequest r2 = new RegisterRequest(email, u2, "Password123!", "en", "UTC", null);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(r1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(r2)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));
        }

        @Test
        @Tag("AC-UM-1")
        @DisplayName("AC-UM-1: Password under 8 characters returns 400 WEAK_PASSWORD")
        void shortPasswordFails() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "short_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com",
                    "u_" + UUID.randomUUID().toString().substring(0, 8),
                    "short",
                    "en",
                    "UTC",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("AC-UM-7")
        @DisplayName("AC-UM-7: Registration with guest token claims session count")
        void registerWithGuestTokenClaimsSession() throws Exception {
            MvcResult guestResult = mockMvc.perform(post("/api/v1/auth/guest"))
                    .andExpect(status().isOk())
                    .andReturn();

            GuestResult guestData = objectMapper.readValue(guestResult.getResponse().getContentAsString(), GuestResult.class);

            RegisterRequest request = new RegisterRequest(
                    "guest_claim_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com",
                    "u_" + UUID.randomUUID().toString().substring(0, 8),
                    "Password123!",
                    "uzl",
                    "Asia/Samarkand",
                    guestData.guestToken()
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.claimedSessions").value(1));
        }
    }

    @Nested
    @DisplayName("Login & Refresh Token Rotation (AC-UM-3, AC-UM-6)")
    class LoginAndRefreshTests {

        @Test
        @Tag("AC-UM-3")
        @DisplayName("AC-UM-3: Login with email or username succeeds; wrong password returns 401")
        void loginScenarios() throws Exception {
            String email = "login_test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String username = "u_" + UUID.randomUUID().toString().substring(0, 8);
            String password = "CorrectPassword123!";

            RegisterRequest registerReq = new RegisterRequest(email, username, password, "en", "UTC", null);
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerReq)))
                    .andExpect(status().isCreated());

            // Login with email
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(cookie().exists("dt_refresh"));

            // Login with username
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                    .andExpect(status().isOk());

            // Wrong password returns 401
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "WrongPassword!"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }

        @Test
        @Tag("AC-UM-6")
        @DisplayName("AC-UM-6: Refresh token rotation; reuse revokes whole family")
        void refreshTokenRotationAndReuseDetection() throws Exception {
            String email = "rot_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String username = "u_" + UUID.randomUUID().toString().substring(0, 8);
            String password = "Password123!";

            MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RegisterRequest(email, username, password, "en", "UTC", null))))
                    .andExpect(status().isCreated())
                    .andReturn();

            var refreshCookie1 = regResult.getResponse().getCookie("dt_refresh");
            assertThat(refreshCookie1).isNotNull();

            // First Refresh: Rotation succeeds, returns new refresh cookie
            MvcResult refreshResult1 = mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(cookie().exists("dt_refresh"))
                    .andReturn();

            var refreshCookie2 = refreshResult1.getResponse().getCookie("dt_refresh");
            assertThat(refreshCookie2).isNotNull();
            assertThat(refreshCookie2.getValue()).isNotEqualTo(refreshCookie1.getValue());

            // Second Refresh with rotated cookie2 succeeds
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie2))
                    .andExpect(status().isOk());

            // TOKEN REUSE ATTACK: Using old cookie1 again must trigger family revocation (AC-UM-6)
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie1))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("TOKEN_REUSE_DETECTED"));

            // After family revocation, even cookie2 is rejected
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie2))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Email Verification Tests (AC-UM-2)")
    class EmailVerificationTests {

        @Test
        @Tag("AC-UM-2")
        @DisplayName("AC-UM-2: Valid token verifies email; expired or used token returns 410 GONE")
        void verifyEmailLifecycle() throws Exception {
            String email = "verify_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String username = "u_" + UUID.randomUUID().toString().substring(0, 8);
            String rawToken = UUID.randomUUID().toString().replace("-", "");
            String tokenHash = TokenHasher.hash(rawToken);

            UserEntity user = new UserEntity(email, username, "HashedPassword123!", "en", "UTC");
            user = userRepository.save(user);

            OneTimeTokenEntity token = new OneTimeTokenEntity(
                    user.getId(),
                    TokenPurpose.VERIFY_EMAIL,
                    tokenHash,
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );
            oneTimeTokenRepository.save(token);

            // Valid token succeeds (200 OK)
            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new VerifyEmailRequest(rawToken))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("VERIFIED"));

            UserEntity verifiedUser = userRepository.findById(user.getId()).orElseThrow();
            assertThat(verifiedUser.isEmailVerified()).isTrue();

            // Re-using same token returns 410 TOKEN_USED
            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new VerifyEmailRequest(rawToken))))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.code").value("TOKEN_USED"));

            // Expired token returns 410 TOKEN_EXPIRED
            String expiredRawToken = UUID.randomUUID().toString().replace("-", "");
            OneTimeTokenEntity expiredToken = new OneTimeTokenEntity(
                    user.getId(),
                    TokenPurpose.VERIFY_EMAIL,
                    TokenHasher.hash(expiredRawToken),
                    Instant.now().minus(1, ChronoUnit.HOURS)
            );
            oneTimeTokenRepository.save(expiredToken);

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new VerifyEmailRequest(expiredRawToken))))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"));
        }
    }

    @Nested
    @DisplayName("Password Reset Tests (AC-UM-5)")
    class PasswordResetTests {

        @Test
        @Tag("AC-UM-5")
        @DisplayName("AC-UM-5: Forgot password always returns 202; reset updates password and revokes sessions")
        void forgotAndResetPasswordFlow() throws Exception {
            String email = "reset_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String username = "u_" + UUID.randomUUID().toString().substring(0, 8);
            String oldPassword = "OldPassword123!";
            String newPassword = "NewPassword123!";

            MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RegisterRequest(email, username, oldPassword, "en", "UTC", null))))
                    .andExpect(status().isCreated())
                    .andReturn();

            var oldRefreshCookie = regResult.getResponse().getCookie("dt_refresh");

            // Forgot password returns 202 ACCEPTED for existing user
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email))))
                    .andExpect(status().isAccepted());

            // Forgot password also returns 202 ACCEPTED for non-existent user (anti-enumeration)
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("nonexistent@example.com"))))
                    .andExpect(status().isAccepted());

            // Seed a reset token for the user
            UserEntity user = userRepository.findByEmail(email).orElseThrow();
            String rawResetToken = UUID.randomUUID().toString().replace("-", "");
            OneTimeTokenEntity resetToken = new OneTimeTokenEntity(
                    user.getId(),
                    TokenPurpose.RESET_PASSWORD,
                    TokenHasher.hash(rawResetToken),
                    Instant.now().plus(1, ChronoUnit.HOURS)
            );
            oneTimeTokenRepository.save(resetToken);

            // Reset password succeeds
            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ResetPasswordRequest(rawResetToken, newPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PASSWORD_RESET_SUCCESS"));

            // Old refresh token must now be revoked (AC-UM-5 requirement)
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(oldRefreshCookie))
                    .andExpect(status().isUnauthorized());

            // Old password fails to login
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(email, oldPassword))))
                    .andExpect(status().isUnauthorized());

            // New password succeeds to login
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(email, newPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(cookie().exists("dt_refresh"));
        }
    }

    @Nested
    @DisplayName("OAuth Provider Tests (AC-UM-4)")
    class OAuthTests {

        @Test
        @Tag("AC-UM-4")
        @DisplayName("AC-UM-4: OAuth callback creates account on first login, links on subsequent")
        void oauthProviderFlow() throws Exception {
            String uid = "gh_" + UUID.randomUUID().toString().substring(0, 8);
            String email = "oauth_" + uid + "@github.dev";

            // First OAuth login: registers user
            mockMvc.perform(post("/api/v1/auth/oauth/github/callback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OAuthProviderRequest(email, uid, "GitHub User"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(cookie().exists("dt_refresh"));

            // Subsequent OAuth login with same UID: logs in existing user
            mockMvc.perform(post("/api/v1/auth/oauth/github/callback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OAuthProviderRequest(email, uid, "GitHub User"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(cookie().exists("dt_refresh"));
        }
    }

    @Nested
    @DisplayName("User Status & Blocking Tests (AC-UM-12)")
    class UserStatusTests {

        @Test
        @Tag("AC-UM-12")
        @DisplayName("AC-UM-12: Blocked user cannot login (403 USER_BLOCKED)")
        void blockedUserCannotLogin() throws Exception {
            String email = "blocked_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String username = "u_" + UUID.randomUUID().toString().substring(0, 8);
            String password = "Password123!";

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RegisterRequest(email, username, password, "en", "UTC", null))))
                    .andExpect(status().isCreated());

            // Block user
            UserEntity user = userRepository.findByEmail(email).orElseThrow();
            user.setStatus(UserStatus.BLOCKED);
            user.setBlockedReason("Violation of community guidelines");
            userRepository.save(user);

            // Attempt login returns 403 USER_BLOCKED
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("USER_BLOCKED"));
        }
    }
}
