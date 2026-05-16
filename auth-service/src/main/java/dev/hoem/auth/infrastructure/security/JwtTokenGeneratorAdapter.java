package dev.hoem.auth.infrastructure.security;

import dev.hoem.auth.domain.port.TokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenGeneratorAdapter implements TokenGenerator {

    private final SecretKey signingKey;
    private final long accessTokenExpirySeconds;

    public JwtTokenGeneratorAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-seconds}") long accessTokenExpirySeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
    }

    @Override
    public String generateAccessToken(UUID userId, UUID householdId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userId", userId.toString())
                .claim("householdId", householdId != null ? householdId.toString() : null)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpirySeconds)))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}