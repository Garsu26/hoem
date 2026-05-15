package dev.hoem.auth.application.result;

import java.util.UUID;

public record RegisterUserResult(UUID userId, String email) {
}