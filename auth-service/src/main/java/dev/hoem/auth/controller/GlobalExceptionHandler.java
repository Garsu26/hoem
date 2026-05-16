package dev.hoem.auth.controller;

import dev.hoem.auth.controller.dto.ErrorResponse;
import dev.hoem.auth.domain.exception.EmailAlreadyExistsException;
import dev.hoem.auth.domain.exception.EmailNotVerifiedException;
import dev.hoem.auth.domain.exception.InvalidCredentialsException;
import dev.hoem.auth.domain.exception.InvalidOrExpiredTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        boolean passwordTooShort = ex.getBindingResult().getFieldErrors().stream()
                .anyMatch(e -> "password".equals(e.getField()) && "Size".equals(e.getCode()));
        if (passwordTooShort) {
            return new ErrorResponse("PASSWORD_TOO_SHORT", "Password must be at least 8 characters.");
        }
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return new ErrorResponse("VALIDATION_ERROR", detail);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailConflict(EmailAlreadyExistsException ex) {
        return new ErrorResponse("EMAIL_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ErrorResponse("INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleEmailNotVerified(EmailNotVerifiedException ex) {
        return new ErrorResponse("EMAIL_NOT_VERIFIED", ex.getMessage());
    }

    @ExceptionHandler(InvalidOrExpiredTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidOrExpiredToken(InvalidOrExpiredTokenException ex) {
        return new ErrorResponse("INVALID_OR_EXPIRED_TOKEN", ex.getMessage());
    }
}