package com.devtype.identity.application;

import com.devtype.identity.domain.*;
import com.devtype.shared.error.DomainException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    public UserService(
            UserRepository userRepository,
            UserSettingsRepository userSettingsRepository,
            RefreshTokenRepository refreshTokenRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.objectMapper = objectMapper;
    }

    public record UserProfileDto(
            Long id,
            String username,
            String email,
            String displayName,
            String bio,
            String countryCode,
            String timezone,
            String locale,
            boolean isPublic,
            boolean showOnLeaderboard,
            String role,
            boolean emailVerified,
            boolean onboarded,
            int onboardingStep
    ) {}

    public record UpdateProfileCommand(
            String displayName,
            String bio,
            String countryCode,
            String timezone,
            String locale,
            Boolean isPublic,
            Boolean showOnLeaderboard,
            String username
    ) {}

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Foydalanuvchi topilmadi", HttpStatus.NOT_FOUND));
        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileCommand cmd) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Foydalanuvchi topilmadi", HttpStatus.NOT_FOUND));

        if (cmd.username() != null && !cmd.username().equalsIgnoreCase(user.getUsername())) {
            // Check 30 days restriction (AC-UM-8)
            if (user.getUpdatedAt() != null && ChronoUnit.DAYS.between(user.getUpdatedAt(), Instant.now()) < 30) {
                throw new DomainException("USERNAME_CHANGE_TOO_SOON", "Username'ni 30 kunda faqat bir marta o'zgartirish mumkin", HttpStatus.BAD_REQUEST);
            }
            if (userRepository.existsByUsername(cmd.username())) {
                throw new DomainException("USERNAME_TAKEN", "Bu username band", HttpStatus.CONFLICT);
            }
            user.setUsername(cmd.username());
        }

        if (cmd.displayName() != null) user.setDisplayName(cmd.displayName());
        if (cmd.bio() != null) user.setBio(cmd.bio());
        if (cmd.countryCode() != null) user.setCountryCode(cmd.countryCode());
        if (cmd.timezone() != null) {
            user.setTimezone(cmd.timezone());
            user.setTimezoneChangedAt(Instant.now());
        }
        if (cmd.locale() != null) user.setLocale(cmd.locale());
        if (cmd.isPublic() != null) user.setPublic(cmd.isPublic());
        if (cmd.showOnLeaderboard() != null) user.setShowOnLeaderboard(cmd.showOnLeaderboard());

        userRepository.save(user);

        return toDto(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Foydalanuvchi topilmadi", HttpStatus.NOT_FOUND));

        user.setStatus(UserStatus.DELETED);
        user.setDeletionScheduledAt(Instant.now().plus(30, ChronoUnit.DAYS));
        userRepository.save(user);

        refreshTokenRepository.revokeAllForUser(userId, Instant.now());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSettings(Long userId) {
        return userSettingsRepository.findByUserId(userId)
                .map(s -> {
                    try {
                        return objectMapper.readValue(s.getSettings(), new TypeReference<Map<String, Object>>() {});
                    } catch (Exception e) {
                        return new LinkedHashMap<String, Object>();
                    }
                })
                .orElseGet(LinkedHashMap::new);
    }

    @Transactional
    public Map<String, Object> updateSettings(Long userId, Map<String, Object> newSettings) {
        UserSettingsEntity entity = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> new UserSettingsEntity(userId, "{}"));

        Map<String, Object> current = new LinkedHashMap<>();
        if (entity.getSettings() != null && !entity.getSettings().isBlank() && !entity.getSettings().equals("{}")) {
            try {
                current = objectMapper.readValue(entity.getSettings(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception ignored) {}
        }

        if (newSettings != null) {
            current.putAll(newSettings);
        }

        try {
            entity.setSettings(objectMapper.writeValueAsString(current));
        } catch (Exception e) {
            throw new DomainException("INVALID_SETTINGS", "Sozlamalarni saqlashda xatolik", HttpStatus.BAD_REQUEST);
        }

        entity.setUpdatedAt(Instant.now());
        userSettingsRepository.save(entity);

        return current;
    }

    private UserProfileDto toDto(UserEntity u) {
        return new UserProfileDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getDisplayName(),
                u.getBio(),
                u.getCountryCode(),
                u.getTimezone(),
                u.getLocale(),
                u.isPublic(),
                u.isShowOnLeaderboard(),
                u.getRole().name(),
                u.isEmailVerified(),
                u.isOnboarded(),
                u.getOnboardingStep()
        );
    }
}
