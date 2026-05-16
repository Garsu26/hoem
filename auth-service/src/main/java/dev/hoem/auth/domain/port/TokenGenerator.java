package dev.hoem.auth.domain.port;

import java.util.UUID;

public interface TokenGenerator {

    String generateAccessToken(UUID userId, UUID householdId);

    String generateRefreshToken();
}