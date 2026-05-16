package dev.hoem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class VerificationToken {

    public static final String TYPE_EMAIL_VERIFICATION = "email_verification";
    public static final String TYPE_PASSWORD_RESET = "password_reset";

    private final UUID id;
    private final UUID userId;
    private final String token;
    private final Instant expiresAt;
    private final Instant usedAt;
    private final String type;

    private VerificationToken(UUID id, UUID userId, String token, Instant expiresAt,
            Instant usedAt, String type) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.type = type;
    }

    public static VerificationToken generate(UUID userId, String token, long ttlHours) {
        return generate(userId, token, ttlHours, TYPE_EMAIL_VERIFICATION);
    }

    public static VerificationToken generate(UUID userId, String token, long ttlHours, String type) {
        return new VerificationToken(
                UUID.randomUUID(),
                userId,
                token,
                Instant.now().plusSeconds(ttlHours * 3600),
                null,
                type);
    }

    public static VerificationToken reconstitute(UUID id, UUID userId, String token,
            Instant expiresAt, Instant usedAt, String type) {
        return new VerificationToken(id, userId, token, expiresAt, usedAt, type);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
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

    public Instant getUsedAt() {
        return usedAt;
    }

    public String getType() {
        return type;
    }
}