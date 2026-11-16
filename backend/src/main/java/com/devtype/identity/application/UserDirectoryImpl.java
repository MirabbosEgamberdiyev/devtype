package com.devtype.identity.application;

import com.devtype.identity.api.UserDirectory;
import com.devtype.identity.api.UserView;
import com.devtype.identity.domain.UserEntity;
import com.devtype.identity.domain.UserRepository;
import com.devtype.identity.domain.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserDirectoryImpl implements UserDirectory {

    private final UserRepository userRepository;

    public UserDirectoryImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserView> findById(Long id) {
        return userRepository.findById(id).map(this::toView);
    }

    @Override
    public Optional<UserView> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toView);
    }

    @Override
    public Optional<UserView> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toView);
    }

    private UserView toView(UserEntity u) {
        return new UserView(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole().name(),
                u.isEmailVerified(),
                u.getStatus() == UserStatus.BLOCKED,
                null,
                u.getTimezone() != null ? u.getTimezone() : "UTC"
        );
    }
}
