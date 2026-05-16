package dev.hoem.auth.application.result;

public record LoginUserResult(String accessToken, String refreshToken, long expiresIn) {
}