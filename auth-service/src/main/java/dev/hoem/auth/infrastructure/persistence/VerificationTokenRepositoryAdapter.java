package dev.hoem.auth.infrastructure.persistence;

import dev.hoem.auth.domain.model.VerificationToken;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepository {

    private final VerificationTokenJpaRepository jpaRepository;

    public VerificationTokenRepositoryAdapter(VerificationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public VerificationToken save(VerificationToken token) {
        VerificationTokenJpaEntity entity = new VerificationTokenJpaEntity(
                token.getId(), token.getUserId(), token.getToken(),
                token.getExpiresAt(), token.getType());
        jpaRepository.save(entity);
        return token;
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(e -> VerificationToken.reconstitute(
                        e.getId(), e.getUserId(), e.getToken(),
                        e.getExpiresAt(), e.getUsedAt(), e.getType()));
    }

    @Override
    public void markAsUsed(UUID tokenId) {
        jpaRepository.findById(tokenId).ifPresent(e -> {
            e.setUsedAt(Instant.now());
            jpaRepository.save(e);
        });
    }
}