package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.RequestPasswordResetCommand;
import dev.hoem.auth.application.usecase.RequestPasswordResetUseCase;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.model.VerificationToken;
import dev.hoem.auth.domain.port.EmailService;
import dev.hoem.auth.domain.port.UserRepository;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestPasswordResetService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final long resetTokenTtlHours;
    private final String appBaseUrl;

    public RequestPasswordResetService(
            UserRepository userRepository,
            VerificationTokenRepository tokenRepository,
            EmailService emailService,
            @Value("${app.password-reset-token.ttl-hours}") long resetTokenTtlHours,
            @Value("${app.base-url}") String appBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.resetTokenTtlHours = resetTokenTtlHours;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    @Transactional
    public void execute(RequestPasswordResetCommand command) {
        Optional<User> userOpt = userRepository.findByEmail(command.email());
        if (userOpt.isEmpty()) {
            log.debug("Password reset requested for unknown email — silently ignored");
            return;
        }

        User user = userOpt.get();
        String rawToken = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.generate(
                user.getId(), rawToken, resetTokenTtlHours, VerificationToken.TYPE_PASSWORD_RESET);
        tokenRepository.save(token);

        String resetLink = appBaseUrl + "/reset-password?token=" + rawToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);

        log.info("Password reset email sent: userId={}", user.getId());
    }
}