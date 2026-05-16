package dev.hoem.auth.infrastructure.persistence;

import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.port.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getName(), user.isVerified(), user.getCreatedAt());
        UserJpaEntity saved = jpaRepository.save(entity);
        return User.reconstitute(saved.getId(), saved.getEmail(), saved.getPasswordHash(),
                saved.getName(), saved.isVerified(), saved.getCreatedAt());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(e -> User.reconstitute(e.getId(), e.getEmail(), e.getPasswordHash(),
                        e.getName(), e.isVerified(), e.getCreatedAt()));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(e -> User.reconstitute(e.getId(), e.getEmail(), e.getPasswordHash(),
                        e.getName(), e.isVerified(), e.getCreatedAt()));
    }
}