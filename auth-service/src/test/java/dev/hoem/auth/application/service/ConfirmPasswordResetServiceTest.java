package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.ConfirmPasswordResetCommand;
import dev.hoem.auth.domain.exception.InvalidOrExpiredTokenException;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.model.VerificationToken;
import dev.hoem.auth.domain.port.PasswordHasher;
import dev.hoem.auth.domain.port.UserRepository;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class ConfirmPasswordResetServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;

    private ConfirmPasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new ConfirmPasswordResetService(tokenRepository, userRepository, passwordHasher);
    }

    @Test
    void givenValidToken_whenConfirm_thenUpdatesPasswordAndMarksTokenUsed() {
        UUID userId = UUID.randomUUID();
        VerificationToken token = VerificationToken.reconstitute(
                UUID.randomUUID(), userId, "raw-token",
                Instant.now().plusSeconds(3600), null, VerificationToken.TYPE_PASSWORD_RESET);
        User user = User.reconstitute(userId, "ana@example.com", "$2a$12$old", "Ana",
                true, Instant.now());

        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHasher.hash("newpass1")).thenReturn("$2a$12$new");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new ConfirmPasswordResetCommand("raw-token", "newpass1"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("$2a$12$new");
        verify(tokenRepository).markAsUsed(token.getId());
    }

    @Test
    void givenUnknownToken_whenConfirm_thenThrows() {
        when(tokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ConfirmPasswordResetCommand("bad-token", "newpass1")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenExpiredToken_whenConfirm_thenThrows() {
        VerificationToken token = VerificationToken.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), "raw-token",
                Instant.now().minusSeconds(1), null, VerificationToken.TYPE_PASSWORD_RESET);

        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute(new ConfirmPasswordResetCommand("raw-token", "newpass1")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenAlreadyUsedToken_whenConfirm_thenThrows() {
        VerificationToken token = VerificationToken.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), "raw-token",
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(60),
                VerificationToken.TYPE_PASSWORD_RESET);

        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute(new ConfirmPasswordResetCommand("raw-token", "newpass1")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenWrongTokenType_whenConfirm_thenThrows() {
        VerificationToken token = VerificationToken.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), "raw-token",
                Instant.now().plusSeconds(3600), null, VerificationToken.TYPE_EMAIL_VERIFICATION);

        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute(new ConfirmPasswordResetCommand("raw-token", "newpass1")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(userRepository, never()).save(any());
    }
}