package dev.hoem.auth.application.usecase;

import dev.hoem.auth.application.command.RequestPasswordResetCommand;

public interface RequestPasswordResetUseCase {

    void execute(RequestPasswordResetCommand command);
}