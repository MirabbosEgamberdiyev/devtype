package com.devtype.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", schema = "identity")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "citext")
    private String email;

    @Column(nullable = false, unique = true, length = 20, columnDefinition = "citext")
    private String username;

    @Column(name = "display_name", length = 40)
    private String displayName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "country_code", columnDefinition = "char(2)")
    @org.hibernate.annotations.JdbcType(org.hibernate.type.descriptor.jdbc.CharJdbcType.class)
    private String countryCode;

    @Column(name = "avatar_key", length = 200)
    private String avatarKey;

    @Column(length = 160)
    private String bio;

    @Column(nullable = false, length = 64)
    private String timezone = "UTC";

    @Column(name = "timezone_changed_at")
    private Instant timezoneChangedAt;

    @Column(nullable = false, length = 4)
    private String locale = "en";

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(name = "show_on_leaderboard", nullable = false)
    private boolean showOnLeaderboard = true;

    @Column(name = "totp_secret")
    private byte[] totpSecret;

    @Column(name = "totp_enabled", nullable = false)
    private boolean totpEnabled = false;

    @Column(nullable = false)
    private boolean onboarded = false;

    @Column(name = "onboarding_step", nullable = false)
    private short onboardingStep = 1;

    @Column(length = 16)
    private String experience;

    @Column(name = "blocked_reason", length = 300)
    private String blockedReason;

    @Column(name = "deletion_scheduled_at")
    private Instant deletionScheduledAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    @Column(nullable = false)
    private int version = 0;

    protected UserEntity() {
    }

    public UserEntity(String email, String username, String passwordHash, String locale, String timezone) {
        this.email = email.toLowerCase().trim();
        this.username = username.toLowerCase().trim();
        this.passwordHash = passwordHash;
        this.locale = (locale != null && !locale.isBlank()) ? locale : "en";
        this.timezone = (timezone != null && !timezone.isBlank()) ? timezone : "UTC";
        this.role = Role.USER;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
        this.onboarded = false;
        this.onboardingStep = 1;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Instant getTimezoneChangedAt() {
        return timezoneChangedAt;
    }

    public void setTimezoneChangedAt(Instant timezoneChangedAt) {
        this.timezoneChangedAt = timezoneChangedAt;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isShowOnLeaderboard() {
        return showOnLeaderboard;
    }

    public void setShowOnLeaderboard(boolean showOnLeaderboard) {
        this.showOnLeaderboard = showOnLeaderboard;
    }

    public byte[] getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(byte[] totpSecret) {
        this.totpSecret = totpSecret;
    }

    public boolean isTotpEnabled() {
        return totpEnabled;
    }

    public void setTotpEnabled(boolean totpEnabled) {
        this.totpEnabled = totpEnabled;
    }

    public boolean isOnboarded() {
        return onboarded;
    }

    public void setOnboarded(boolean onboarded) {
        this.onboarded = onboarded;
    }

    public short getOnboardingStep() {
        return onboardingStep;
    }

    public void setOnboardingStep(short onboardingStep) {
        this.onboardingStep = onboardingStep;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getBlockedReason() {
        return blockedReason;
    }

    public void setBlockedReason(String blockedReason) {
        this.blockedReason = blockedReason;
    }

    public Instant getDeletionScheduledAt() {
        return deletionScheduledAt;
    }

    public void setDeletionScheduledAt(Instant deletionScheduledAt) {
        this.deletionScheduledAt = deletionScheduledAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public int getVersion() {
        return version;
    }
}
