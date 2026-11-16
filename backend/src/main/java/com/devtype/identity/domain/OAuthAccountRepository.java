package com.devtype.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccountEntity, Long> {

    Optional<OAuthAccountEntity> findByProviderAndProviderUid(String provider, String providerUid);

    Optional<OAuthAccountEntity> findByUserIdAndProvider(Long userId, String provider);
}
