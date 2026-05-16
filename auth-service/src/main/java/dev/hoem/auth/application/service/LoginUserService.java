package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.LoginUserCommand;
import dev.hoem.auth.application.result.LoginUserResult;
import dev.hoem.auth.application.usecase.LoginUserUseCase;
import dev.hoem.auth.domain.exception.EmailNotVerifiedException;
import dev.hoem.auth.domain.exception.InvalidCredentialsException;
import dev.hoem.auth.domain.model.Session;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.port.MembershipRepository;
import dev.hoem.auth.domain.port.PasswordHasher;
import dev.hoem.auth.domain.port.SessionRepository;
import dev.hoem.auth.domain.port.TokenGenerator;
import dev.hoem.auth.domain.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LoginUserService implements LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserService.class);

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final SessionRepository sessionRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;
    private final long accessTokenExpirySeconds;
    private final long refreshTokenExpiryDays;

    public LoginUserService(
            UserRepository userRepository,
            MembershipRepository membershipRepository,
            SessionRepository sessionRepository,
            PasswordHasher passwordHasher,
            TokenGenerator tokenGenerator,
            @Value("${jwt.access-token-expiry-seconds}") long accessTokenExpirySeconds,
            @Value("${jwt.refresh-token-expiry-days}") long refreshTokenExpiryDays) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.sessionRepository = sessionRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpiryDays = refreshTokenExpiryDays;
    }

    @Override
    @Transactional
    public LoginUserResult execute(LoginUserCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException();
        }

        UUID householdId = membershipRepository.findFirstHouseholdId(user.getId()).orElse(null);

        String accessToken = tokenGenerator.generateAccessToken(user.getId(), householdId);
        String refreshToken = tokenGenerator.generateRefreshToken();

        Session session = Session.create(user.getId(), refreshToken, refreshTokenExpiryDays);
        sessionRepository.save(session);

        log.info("User logged in: userId={}", user.getId());
        return new LoginUserResult(accessToken, refreshToken, accessTokenExpirySeconds);
    }
}