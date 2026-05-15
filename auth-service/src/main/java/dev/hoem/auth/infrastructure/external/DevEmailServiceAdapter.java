package dev.hoem.auth.infrastructure.external;

import dev.hoem.auth.domain.port.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevEmailServiceAdapter implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(DevEmailServiceAdapter.class);

    @Override
    public void sendVerificationEmail(String toEmail, String userName, String verificationLink) {
        log.info("=================================================");
        log.info("  DEV MODE — verification email not sent");
        log.info("  To:   {}", toEmail);
        log.info("  Name: {}", userName);
        log.info("  Link: {}", verificationLink);
        log.info("=================================================");
    }
}