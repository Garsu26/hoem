package dev.hoem.auth.application.usecase;

import dev.hoem.auth.application.command.LoginUserCommand;
import dev.hoem.auth.application.result.LoginUserResult;

public interface LoginUserUseCase {

    LoginUserResult execute(LoginUserCommand command);
}