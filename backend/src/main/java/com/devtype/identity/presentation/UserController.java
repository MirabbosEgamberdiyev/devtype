package com.devtype.identity.presentation;

import com.devtype.identity.application.UserService;
import com.devtype.shared.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserService.UserProfileDto> getProfile(Authentication auth) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PatchMapping
    public ResponseEntity<UserService.UserProfileDto> updateProfile(
            @RequestBody UserService.UpdateProfileCommand cmd,
            Authentication auth
    ) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(userService.updateProfile(userId, cmd));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Authentication auth) {
        Long userId = requireUserId(auth);
        userService.deleteAccount(userId);
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings(Authentication auth) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(userService.getSettings(userId));
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(
            @RequestBody Map<String, Object> settings,
            Authentication auth
    ) {
        Long userId = requireUserId(auth);
        return ResponseEntity.ok(userService.updateSettings(userId, settings));
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new DomainException("UNAUTHORIZED", "Avtorizatsiya talab qilinadi", HttpStatus.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new DomainException("UNAUTHORIZED", "Yaroqsiz foydalanuvchi identifikatori", HttpStatus.UNAUTHORIZED);
        }
    }
}
