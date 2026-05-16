package dev.hoem.auth.domain.exception;

public class InvalidOrExpiredTokenException extends RuntimeException {

    public InvalidOrExpiredTokenException() {
        super("Token is invalid, expired, or already used.");
    }
}