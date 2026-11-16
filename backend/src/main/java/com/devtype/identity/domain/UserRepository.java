package com.devtype.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :login OR u.username = :login")
    Optional<UserEntity> findByLogin(@Param("login") String login);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
