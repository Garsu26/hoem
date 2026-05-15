package dev.hoem.auth.controller;

import dev.hoem.auth.controller.dto.ErrorResponse;
import dev.hoem.auth.domain.exception.EmailAlreadyExistsException;
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
}