package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserExerciseBestRepository extends JpaRepository<UserExerciseBestEntity, UserExerciseBestKey> {

    Optional<UserExerciseBestEntity> findByUserIdAndExerciseKey(Long userId, UUID exerciseKey);

    List<UserExerciseBestEntity> findByUserIdAndExerciseKeyIn(Long userId, Collection<UUID> exerciseKeys);

    long countByUserIdAndCompletedTrue(Long userId);

    long countByUserIdAndExerciseKeyInAndCompletedTrue(Long userId, Collection<UUID> exerciseKeys);

    long countByUserIdAndExerciseKeyInAndBestAccuracyGreaterThanEqual(Long userId, Collection<UUID> exerciseKeys, java.math.BigDecimal accuracy);
}
