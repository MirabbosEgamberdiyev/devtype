package com.devtype.content.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {

    Optional<ExerciseEntity> findByExerciseKeyAndStatus(UUID exerciseKey, ExerciseStatus status);

    Optional<ExerciseEntity> findByExerciseKeyAndVersion(UUID exerciseKey, int version);

    Optional<ExerciseEntity> findByLanguageIdAndCodeHash(Short languageId, String codeHash);

    boolean existsByLanguageIdAndCodeHash(Short languageId, String codeHash);

    List<ExerciseEntity> findByLanguageIdAndTopicIdAndDifficultyAndStatus(
            Short languageId,
            Integer topicId,
            DifficultyLevel difficulty,
            ExerciseStatus status
    );

    List<ExerciseEntity> findByLanguageIdAndDifficultyAndStatus(
            Short languageId,
            DifficultyLevel difficulty,
            ExerciseStatus status
    );

    List<ExerciseEntity> findByTopicIdAndDifficultyAndStatus(
            Integer topicId,
            DifficultyLevel difficulty,
            ExerciseStatus status
    );

    long countByTopicIdAndDifficultyAndStatus(
            Integer topicId,
            DifficultyLevel difficulty,
            ExerciseStatus status
    );

    List<ExerciseEntity> findByLanguageIdAndRankedPoolTrueAndStatus(Short languageId, ExerciseStatus status);
}
