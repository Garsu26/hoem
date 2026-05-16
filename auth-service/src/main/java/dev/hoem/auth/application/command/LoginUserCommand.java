package dev.hoem.auth.application.command;

public record LoginUserCommand(String email, String rawPassword) {
}