package dev.hoem.auth.domain.port;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String userName, String verificationLink);
}