package dev.hoem.auth.controller;

import dev.hoem.auth.application.command.RegisterUserCommand;
import dev.hoem.auth.application.result.RegisterUserResult;
import dev.hoem.auth.application.usecase.RegisterUserUseCase;
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

    public AuthController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserResult result = registerUserUseCase.execute(
                new RegisterUserCommand(request.email(), request.password(), request.name()));
        return new RegisterResponse(result.userId(), result.email(),
                "Account created. Check your email to verify your address.");
    }
}