package com.devtype.content.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, Integer> {

    List<TopicEntity> findByLanguageIdOrderBySortOrderAsc(Short languageId);

    List<TopicEntity> findByLanguageIdAndStatusOrderBySortOrderAsc(Short languageId, TopicStatus status);

    Optional<TopicEntity> findByLanguageIdAndSlug(Short languageId, String slug);
}
