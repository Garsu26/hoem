package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.ConfirmPasswordResetCommand;
import dev.hoem.auth.application.usecase.ConfirmPasswordResetUseCase;
import dev.hoem.auth.domain.exception.InvalidOrExpiredTokenException;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.model.VerificationToken;
import dev.hoem.auth.domain.port.PasswordHasher;
import dev.hoem.auth.domain.port.UserRepository;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmPasswordResetService implements ConfirmPasswordResetUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmPasswordResetService.class);

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public ConfirmPasswordResetService(
            VerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordHasher passwordHasher) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public void execute(ConfirmPasswordResetCommand command) {
        VerificationToken token = tokenRepository.findByToken(command.token())
                .orElseThrow(InvalidOrExpiredTokenException::new);

        if (!VerificationToken.TYPE_PASSWORD_RESET.equals(token.getType())
                || token.isExpired()
                || token.isUsed()) {
            throw new InvalidOrExpiredTokenException();
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(InvalidOrExpiredTokenException::new);

        String newHash = passwordHasher.hash(command.newPassword());
        userRepository.save(user.withUpdatedPassword(newHash));
        tokenRepository.markAsUsed(token.getId());

        log.info("Password reset confirmed: userId={}", user.getId());
    }
}