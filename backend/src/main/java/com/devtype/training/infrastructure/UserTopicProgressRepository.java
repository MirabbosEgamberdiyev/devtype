package com.devtype.training.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicProgressRepository extends JpaRepository<UserTopicProgressEntity, UserTopicProgressKey> {

    Optional<UserTopicProgressEntity> findByUserIdAndTopicIdAndDifficulty(Long userId, Integer topicId, String difficulty);

    List<UserTopicProgressEntity> findByUserIdAndTopicId(Long userId, Integer topicId);

    List<UserTopicProgressEntity> findByUserId(Long userId);

    long countByUserIdAndStateIn(Long userId, List<String> states);
}
