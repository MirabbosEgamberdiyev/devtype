package com.devtype.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "oauth_accounts", schema = "identity")
public class OAuthAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 16)
    private String provider;

    @Column(name = "provider_uid", nullable = false, length = 100)
    private String providerUid;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected OAuthAccountEntity() {
    }

    public OAuthAccountEntity(Long userId, String provider, String providerUid) {
        this.userId = userId;
        this.provider = provider.toUpperCase();
        this.providerUid = providerUid;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
