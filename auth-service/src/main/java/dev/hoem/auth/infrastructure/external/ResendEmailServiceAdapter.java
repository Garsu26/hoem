package dev.hoem.auth.infrastructure.external;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.hoem.auth.domain.port.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResendEmailServiceAdapter implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailServiceAdapter.class);

    private final Resend resend;
    private final String fromAddress;

    public ResendEmailServiceAdapter(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-address}") String fromAddress) {
        this.resend = new Resend(apiKey);
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String userName, String verificationLink) {
        String html = "<p>Hi " + userName + ",</p>"
                + "<p>Please verify your email address by clicking the link below:</p>"
                + "<p><a href=\"" + verificationLink + "\">Verify my account</a></p>"
                + "<p>This link expires in 24 hours.</p>";

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(toEmail)
                .subject("Verify your HOEM account")
                .html(html)
                .build();

        try {
            resend.emails().send(options);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Could not send verification email", ex);
        }
    }
}