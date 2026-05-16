package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.RequestPasswordResetCommand;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.model.VerificationToken;
import dev.hoem.auth.domain.port.EmailService;
import dev.hoem.auth.domain.port.UserRepository;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private EmailService emailService;

    private RequestPasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new RequestPasswordResetService(
                userRepository, tokenRepository, emailService, 1L, "http://localhost:3000");
    }

    @Test
    void givenExistingEmail_whenRequestReset_thenSavesTokenAndSendsEmail() {
        User user = User.reconstitute(
                java.util.UUID.randomUUID(), "ana@example.com", "$2a$12$hash", "Ana",
                true, java.time.Instant.now());
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new RequestPasswordResetCommand("ana@example.com"));

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getType()).isEqualTo(VerificationToken.TYPE_PASSWORD_RESET);

        verify(emailService).sendPasswordResetEmail(
                org.mockito.ArgumentMatchers.eq("ana@example.com"),
                org.mockito.ArgumentMatchers.eq("Ana"),
                org.mockito.ArgumentMatchers.contains("/reset-password?token="));
    }

    @Test
    void givenUnknownEmail_whenRequestReset_thenDoesNothing() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        service.execute(new RequestPasswordResetCommand("unknown@example.com"));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }
}