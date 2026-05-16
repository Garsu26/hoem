package dev.hoem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String name;
    private boolean verified;
    private final Instant createdAt;

    private User(UUID id, String email, String passwordHash, String name,
            boolean verified, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.verified = verified;
        this.createdAt = createdAt;
    }

    public static User register(String email, String passwordHash, String name) {
        return new User(UUID.randomUUID(), email, passwordHash, name, false, Instant.now());
    }

    public static User reconstitute(UUID id, String email, String passwordHash,
            String name, boolean verified, Instant createdAt) {
        return new User(id, email, passwordHash, name, verified, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public boolean isVerified() {
        return verified;
    }

    public User withUpdatedPassword(String newPasswordHash) {
        return new User(this.id, this.email, newPasswordHash, this.name, this.verified, this.createdAt);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}