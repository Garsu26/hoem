package dev.hoem.auth.domain.port;

import dev.hoem.auth.domain.model.VerificationToken;

public interface VerificationTokenRepository {

    VerificationToken save(VerificationToken token);
}