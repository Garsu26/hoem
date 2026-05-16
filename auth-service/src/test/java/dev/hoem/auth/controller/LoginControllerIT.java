package dev.hoem.auth.controller;

import dev.hoem.auth.application.result.LoginUserResult;
import dev.hoem.auth.application.usecase.LoginUserUseCase;
import dev.hoem.auth.application.usecase.RegisterUserUseCase;
import dev.hoem.auth.domain.exception.EmailNotVerifiedException;
import dev.hoem.auth.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
class LoginControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @Test
    void givenValidCredentials_thenReturns200WithTokens() throws Exception {
        when(loginUserUseCase.execute(any()))
                .thenReturn(new LoginUserResult("access.token.jwt", "refresh-uuid", 3600L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ana@example.com",
                                  "password": "secret12"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-uuid"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void givenWrongPassword_thenReturns401WithInvalidCredentials() throws Exception {
        when(loginUserUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ana@example.com",
                                  "password": "wrongpass"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void givenUnverifiedAccount_thenReturns403WithEmailNotVerified() throws Exception {
        when(loginUserUseCase.execute(any())).thenThrow(new EmailNotVerifiedException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "password": "secret12"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void givenInvalidEmailFormat_thenReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "secret12"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}