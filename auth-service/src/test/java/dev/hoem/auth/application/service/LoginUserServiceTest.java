package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.LoginUserCommand;
import dev.hoem.auth.application.result.LoginUserResult;
import dev.hoem.auth.domain.exception.EmailNotVerifiedException;
import dev.hoem.auth.domain.exception.InvalidCredentialsException;
import dev.hoem.auth.domain.model.Session;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.port.MembershipRepository;
import dev.hoem.auth.domain.port.PasswordHasher;
import dev.hoem.auth.domain.port.SessionRepository;
import dev.hoem.auth.domain.port.TokenGenerator;
import dev.hoem.auth.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenGenerator tokenGenerator;

    private LoginUserService service;

    @BeforeEach
    void setUp() {
        service = new LoginUserService(
                userRepository, membershipRepository, sessionRepository,
                passwordHasher, tokenGenerator, 3600L, 30L);
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnsAccessAndRefreshTokens() {
        User verified = verifiedUser();
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(verified));
        when(passwordHasher.matches("secret12", verified.getPasswordHash())).thenReturn(true);
        when(membershipRepository.findFirstHouseholdId(verified.getId()))
                .thenReturn(Optional.empty());
        when(tokenGenerator.generateAccessToken(any(), any())).thenReturn("access.token.jwt");
        when(tokenGenerator.generateRefreshToken()).thenReturn("refresh-uuid");
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginUserResult result = service.execute(
                new LoginUserCommand("ana@example.com", "secret12"));

        assertThat(result.accessToken()).isEqualTo("access.token.jwt");
        assertThat(result.refreshToken()).isEqualTo("refresh-uuid");
        assertThat(result.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void givenUnknownEmail_whenLogin_thenThrowsInvalidCredentials() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new LoginUserCommand("unknown@example.com", "secret12")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void givenWrongPassword_whenLogin_thenThrowsInvalidCredentials() {
        User verified = verifiedUser();
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(verified));
        when(passwordHasher.matches("wrong", verified.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(
                new LoginUserCommand("ana@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void givenUnverifiedUser_whenLogin_thenThrowsEmailNotVerified() {
        User unverified = User.register("new@example.com", "$2a$12$hash", "New");
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(unverified));
        when(passwordHasher.matches("secret12", unverified.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> service.execute(
                new LoginUserCommand("new@example.com", "secret12")))
                .isInstanceOf(EmailNotVerifiedException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void givenValidLogin_thenSessionIsSavedWithCorrectUserId() {
        User verified = verifiedUser();
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(verified));
        when(passwordHasher.matches("secret12", verified.getPasswordHash())).thenReturn(true);
        when(membershipRepository.findFirstHouseholdId(verified.getId()))
                .thenReturn(Optional.empty());
        when(tokenGenerator.generateAccessToken(any(), any())).thenReturn("access.token.jwt");
        when(tokenGenerator.generateRefreshToken()).thenReturn("refresh-uuid");
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new LoginUserCommand("ana@example.com", "secret12"));

        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void givenUserWithHousehold_whenLogin_thenHouseholdIdPassedToTokenGenerator() {
        User verified = verifiedUser();
        UUID householdId = UUID.randomUUID();
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(verified));
        when(passwordHasher.matches("secret12", verified.getPasswordHash())).thenReturn(true);
        when(membershipRepository.findFirstHouseholdId(verified.getId()))
                .thenReturn(Optional.of(householdId));
        when(tokenGenerator.generateAccessToken(verified.getId(), householdId))
                .thenReturn("access.token.jwt");
        when(tokenGenerator.generateRefreshToken()).thenReturn("refresh-uuid");
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new LoginUserCommand("ana@example.com", "secret12"));

        verify(tokenGenerator).generateAccessToken(verified.getId(), householdId);
    }

    private User verifiedUser() {
        return User.reconstitute(UUID.randomUUID(), "ana@example.com",
                "$2a$12$hash", "Ana", true, Instant.now());
    }
}