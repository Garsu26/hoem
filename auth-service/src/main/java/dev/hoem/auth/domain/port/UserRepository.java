package dev.hoem.auth.domain.port;

import dev.hoem.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);
}