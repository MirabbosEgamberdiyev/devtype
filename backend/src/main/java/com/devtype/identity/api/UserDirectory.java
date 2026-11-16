package com.devtype.identity.api;

import java.util.Optional;

public interface UserDirectory {
    Optional<UserView> findById(Long id);
    Optional<UserView> findByUsername(String username);
    Optional<UserView> findByEmail(String email);
}
