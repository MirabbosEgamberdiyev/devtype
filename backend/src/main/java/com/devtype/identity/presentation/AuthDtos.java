package com.devtype.identity.presentation;

import com.devtype.identity.api.UserView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 3, max = 20) String username,
            @NotBlank @Size(min = 8, max = 128) String password,
            String locale,
            String timezone,
            String guestToken
    ) {}

    public record LoginRequest(
            @NotBlank String login,
            @NotBlank String password
    ) {}

    public record VerifyEmailRequest(
            @NotBlank String token
    ) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 128) String newPassword
    ) {}

    public record OAuthProviderRequest(
            @NotBlank @Email String email,
            @NotBlank String providerUid,
            String displayName
    ) {}

    public record AuthResponse(
            String accessToken,
            long expiresIn,
            UserView user,
            Integer claimedSessions
    ) {}
}
