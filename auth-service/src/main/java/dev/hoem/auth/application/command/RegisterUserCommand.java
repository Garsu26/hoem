package dev.hoem.auth.application.command;

public record RegisterUserCommand(String email, String rawPassword, String name) {
}