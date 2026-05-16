package dev.hoem.auth.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Session {

    private final UUID id;
    private final UUID userId;
    private final String refreshToken;
    private final Instant expiresAt;
    private final Instant createdAt;

    private Session(UUID id, UUID userId, String refreshToken,
            Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static Session create(UUID userId, String refreshToken, long expiryDays) {
        Instant now = Instant.now();
        return new Session(UUID.randomUUID(), userId, refreshToken,
                now.plus(expiryDays, ChronoUnit.DAYS), now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}