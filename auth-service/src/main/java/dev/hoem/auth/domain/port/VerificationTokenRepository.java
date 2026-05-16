package dev.hoem.auth.domain.port;

import dev.hoem.auth.domain.model.VerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository {

    VerificationToken save(VerificationToken token);

    Optional<VerificationToken> findByToken(String token);

    void markAsUsed(UUID tokenId);
}