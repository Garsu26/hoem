package dev.hoem.auth.controller;

import dev.hoem.auth.application.command.LoginUserCommand;
import dev.hoem.auth.application.command.RegisterUserCommand;
import dev.hoem.auth.application.result.LoginUserResult;
import dev.hoem.auth.application.result.RegisterUserResult;
import dev.hoem.auth.application.usecase.LoginUserUseCase;
import dev.hoem.auth.application.usecase.RegisterUserUseCase;
import dev.hoem.auth.controller.dto.LoginRequest;
import dev.hoem.auth.controller.dto.LoginResponse;
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

    public AuthController(RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
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
}