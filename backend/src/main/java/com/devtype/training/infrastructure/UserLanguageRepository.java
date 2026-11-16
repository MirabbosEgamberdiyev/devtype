package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguageEntity, UserLanguageKey> {

    Optional<UserLanguageEntity> findByUserIdAndLanguageId(Long userId, Short languageId);

    List<UserLanguageEntity> findByUserIdAndIsActiveTrue(Long userId);

    List<UserLanguageEntity> findByUserId(Long userId);

    Optional<UserLanguageEntity> findByUserIdAndIsPrimaryTrue(Long userId);
}
