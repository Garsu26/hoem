package dev.hoem.auth.application.service;

import dev.hoem.auth.application.command.RegisterUserCommand;
import dev.hoem.auth.application.result.RegisterUserResult;
import dev.hoem.auth.domain.exception.EmailAlreadyExistsException;
import dev.hoem.auth.domain.model.User;
import dev.hoem.auth.domain.port.EmailService;
import dev.hoem.auth.domain.port.PasswordHasher;
import dev.hoem.auth.domain.port.UserRepository;
import dev.hoem.auth.domain.port.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private EmailService emailService;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(
                userRepository, tokenRepository, passwordHasher, emailService,
                24L, "http://localhost:3000");
    }

    @Test
    void givenNewEmail_whenRegister_thenReturnsUserIdAndEmail() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordHasher.hash("secret12")).thenReturn("$2a$12$hash");
        User saved = User.register("ana@example.com", "$2a$12$hash", "Ana");
        when(userRepository.save(any())).thenReturn(saved);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserResult result = service.execute(
                new RegisterUserCommand("ana@example.com", "secret12", "Ana"));

        assertThat(result.email()).isEqualTo("ana@example.com");
        assertThat(result.userId()).isNotNull();
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void givenDuplicateEmail_whenRegister_thenThrowsEmailAlreadyExistsException() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(
                new RegisterUserCommand("ana@example.com", "secret12", "Ana")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void givenValidRegistration_thenVerificationTokenIsSaved() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("$2a$12$hash");
        User saved = User.register("ana@example.com", "$2a$12$hash", "Ana");
        when(userRepository.save(any())).thenReturn(saved);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new RegisterUserCommand("ana@example.com", "secret12", "Ana"));

        verify(tokenRepository).save(any());
    }
}