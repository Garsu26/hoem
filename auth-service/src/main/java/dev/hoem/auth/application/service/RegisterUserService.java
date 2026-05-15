package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.RegisterUserCommand;
import dev.hoem.auth.application.result.RegisterUserResult;
import dev.hoem.auth.application.usecase.RegisterUserUseCase;
import dev.hoem.auth.domain.exception.EmailAlreadyExistsException;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.model.VerificationToken;
import dev.hoem.auth.domain.port.EmailService;
import dev.hoem.auth.domain.port.PasswordHasher;
import dev.hoem.auth.domain.port.UserRepository;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;
    private final long tokenTtlHours;
    private final String appBaseUrl;

    public RegisterUserService(
            UserRepository userRepository,
            VerificationTokenRepository tokenRepository,
            PasswordHasher passwordHasher,
            EmailService emailService,
            @Value("${app.verification-token.ttl-hours}") long tokenTtlHours,
            @Value("${app.base-url}") String appBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordHasher = passwordHasher;
        this.emailService = emailService;
        this.tokenTtlHours = tokenTtlHours;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    @Transactional
    public RegisterUserResult execute(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        String passwordHash = passwordHasher.hash(command.rawPassword());
        User user = User.register(command.email(), passwordHash, command.name());
        User saved = userRepository.save(user);

        String rawToken = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.generate(saved.getId(), rawToken, tokenTtlHours);
        tokenRepository.save(token);

        String verificationLink = appBaseUrl + "/verify?token=" + rawToken;
        emailService.sendVerificationEmail(saved.getEmail(), saved.getName(), verificationLink);

        log.info("User registered: userId={}", saved.getId());
        return new RegisterUserResult(saved.getId(), saved.getEmail());
    }
}