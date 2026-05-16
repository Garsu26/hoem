package dev.hoem.auth.application.command;

public record ConfirmPasswordResetCommand(String token, String newPassword) {
}