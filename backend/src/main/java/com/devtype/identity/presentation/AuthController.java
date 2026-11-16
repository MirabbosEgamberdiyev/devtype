package com.devtype.identity.presentation;

import com.devtype.identity.application.*;
import com.devtype.identity.presentation.AuthDtos.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "dt_refresh";
    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GuestUseCase guestUseCase;
    private final OAuthProviderUseCase oauthProviderUseCase;

    public AuthController(
            RegisterUseCase registerUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            ForgotPasswordUseCase forgotPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            GuestUseCase guestUseCase,
            OAuthProviderUseCase oauthProviderUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.guestUseCase = guestUseCase;
        this.oauthProviderUseCase = oauthProviderUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        RegisterCommand command = new RegisterCommand(
                request.email(),
                request.username(),
                request.password(),
                request.locale(),
                request.timezone(),
                request.guestToken(),
                servletRequest.getHeader(HttpHeaders.USER_AGENT),
                servletRequest.getRemoteAddr()
        );

        AuthResult result = registerUseCase.execute(command);
        setRefreshCookie(servletResponse, result.rawRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                result.accessToken(),
                result.expiresIn(),
                result.user(),
                result.claimedSessions()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        LoginCommand command = new LoginCommand(
                request.login(),
                request.password(),
                servletRequest.getHeader(HttpHeaders.USER_AGENT),
                servletRequest.getRemoteAddr()
        );

        AuthResult result = loginUseCase.execute(command);
        setRefreshCookie(servletResponse, result.rawRefreshToken());

        return ResponseEntity.ok(new AuthResponse(
                result.accessToken(),
                result.expiresIn(),
                result.user(),
                null
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String cookieRefreshToken,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        String token = cookieRefreshToken;
        if ((token == null || token.isBlank()) && body != null) {
            token = body.get("refreshToken");
        }

        AuthResult result = refreshTokenUseCase.execute(
                token,
                servletRequest.getHeader(HttpHeaders.USER_AGENT),
                servletRequest.getRemoteAddr()
        );
        setRefreshCookie(servletResponse, result.rawRefreshToken());

        return ResponseEntity.ok(new AuthResponse(
                result.accessToken(),
                result.expiresIn(),
                result.user(),
                null
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String cookieRefreshToken,
            HttpServletResponse servletResponse) {

        logoutUseCase.execute(cookieRefreshToken);
        clearRefreshCookie(servletResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.execute(request.token());
        return ResponseEntity.ok(Map.of("status", "VERIFIED"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request.email());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "ACCEPTED"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletResponse servletResponse) {

        resetPasswordUseCase.execute(request.token(), request.newPassword());
        clearRefreshCookie(servletResponse);
        return ResponseEntity.ok(Map.of("status", "PASSWORD_RESET_SUCCESS"));
    }

    @PostMapping("/guest")
    public ResponseEntity<GuestResult> guest() {
        GuestResult result = guestUseCase.execute();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/oauth/{provider}/callback")
    public ResponseEntity<AuthResponse> oauthCallback(
            @PathVariable String provider,
            @Valid @RequestBody OAuthProviderRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        AuthResult result = oauthProviderUseCase.execute(
                provider,
                request.email(),
                request.providerUid(),
                request.displayName(),
                servletRequest.getHeader(HttpHeaders.USER_AGENT),
                servletRequest.getRemoteAddr()
        );
        setRefreshCookie(servletResponse, result.rawRefreshToken());

        return ResponseEntity.ok(new AuthResponse(
                result.accessToken(),
                result.expiresIn(),
                result.user(),
                null
        ));
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false) // Set to true in prod via reverse proxy header
                .sameSite("Lax")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(Duration.ofDays(30))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
