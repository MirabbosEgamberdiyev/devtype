package com.devtype.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OneTimeTokenRepository extends JpaRepository<OneTimeTokenEntity, Long> {

    Optional<OneTimeTokenEntity> findByTokenHash(String tokenHash);

    Optional<OneTimeTokenEntity> findByTokenHashAndPurpose(String tokenHash, TokenPurpose purpose);
}
