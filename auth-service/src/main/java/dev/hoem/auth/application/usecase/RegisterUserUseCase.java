package dev.hoem.auth.application.usecase;

import dev.hoem.auth.application.command.RegisterUserCommand;
import dev.hoem.auth.application.result.RegisterUserResult;

public interface RegisterUserUseCase {

    RegisterUserResult execute(RegisterUserCommand command);
}