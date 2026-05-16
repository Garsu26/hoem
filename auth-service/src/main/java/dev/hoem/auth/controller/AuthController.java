package dev.hoem.auth.controller;

import dev.hoem.auth.application.command.ConfirmPasswordResetCommand;
import dev.hoem.auth.application.command.LoginUserCommand;
import dev.hoem.auth.application.command.RegisterUserCommand;
import dev.hoem.auth.application.command.RequestPasswordResetCommand;
import dev.hoem.auth.application.result.LoginUserResult;
import dev.hoem.auth.application.result.RegisterUserResult;
import dev.hoem.auth.application.usecase.ConfirmPasswordResetUseCase;
import dev.hoem.auth.application.usecase.LoginUserUseCase;
import dev.hoem.auth.application.usecase.RegisterUserUseCase;
import dev.hoem.auth.application.usecase.RequestPasswordResetUseCase;
import dev.hoem.auth.controller.dto.LoginRequest;
import dev.hoem.auth.controller.dto.LoginResponse;
import dev.hoem.auth.controller.dto.PasswordResetConfirmDto;
import dev.hoem.auth.controller.dto.PasswordResetRequestDto;
import dev.hoem.auth.controller.dto.RegisterRequest;
import dev.hoem.auth.controller.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ConfirmPasswordResetUseCase confirmPasswordResetUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.confirmPasswordResetUseCase = confirmPasswordResetUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserResult result = registerUserUseCase.execute(
                new RegisterUserCommand(request.email(), request.password(), request.name()));
        return new RegisterResponse(result.userId(), result.email(),
                "Account created. Check your email to verify your address.");
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginUserResult result = loginUserUseCase.execute(
                new LoginUserCommand(request.email(), request.password()));
        return new LoginResponse(result.accessToken(), result.refreshToken(),
                "Bearer", result.expiresIn());
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        requestPasswordResetUseCase.execute(new RequestPasswordResetCommand(request.email()));
    }

    @PostMapping("/password-reset/confirm")
    public void confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDto request) {
        confirmPasswordResetUseCase.execute(
                new ConfirmPasswordResetCommand(request.token(), request.newPassword()));
    }
}