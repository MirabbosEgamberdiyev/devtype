package com.devtype.content.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Short> {

    Optional<LanguageEntity> findBySlug(String slug);

    List<LanguageEntity> findAllByOrderBySortOrderAsc();

    List<LanguageEntity> findByStatusOrderBySortOrderAsc(LanguageStatus status);

    boolean existsBySlug(String slug);
}
