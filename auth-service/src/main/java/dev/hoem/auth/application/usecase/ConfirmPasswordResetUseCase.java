package dev.hoem.auth.application.usecase;

import dev.hoem.auth.application.command.ConfirmPasswordResetCommand;

public interface ConfirmPasswordResetUseCase {

    void execute(ConfirmPasswordResetCommand command);
}