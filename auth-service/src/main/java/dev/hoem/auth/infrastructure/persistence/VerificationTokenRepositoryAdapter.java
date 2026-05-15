package dev.hoem.auth.infrastructure.persistence;

import dev.hoem.auth.domain.model.VerificationToken;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.springframework.stereotype.Component;

@Component
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepository {

    private final VerificationTokenJpaRepository jpaRepository;

    public VerificationTokenRepositoryAdapter(VerificationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public VerificationToken save(VerificationToken token) {
        VerificationTokenJpaEntity entity = new VerificationTokenJpaEntity(
                token.getId(), token.getUserId(), token.getToken(), token.getExpiresAt());
        jpaRepository.save(entity);
        return token;
    }
}