package dev.hoem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class VerificationToken {

    private final UUID id;
    private final UUID userId;
    private final String token;
    private final Instant expiresAt;

    private VerificationToken(UUID id, UUID userId, String token, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static VerificationToken generate(UUID userId, String token, long ttlHours) {
        return new VerificationToken(
                UUID.randomUUID(),
                userId,
                token,
                Instant.now().plusSeconds(ttlHours * 3600));
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}