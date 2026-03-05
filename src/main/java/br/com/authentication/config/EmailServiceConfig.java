package br.com.authentication.config;

import br.com.authentication.domain.model.User;
import br.com.authentication.service.EmailService;
import br.com.authentication.service.IEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@Slf4j
public class EmailServiceConfig {

    @Bean
    @Primary
    public IEmailService emailService(
            @Value("${email-service.enable:false}") boolean emailServiceEnabled,
            JavaMailSender javaMailSender
    ) {
        if (emailServiceEnabled) {
            log.info("Email service is ENABLED. Using real EmailService implementation.");
            return new EmailService(javaMailSender);
        }

        log.info("Email service is DISABLED. Using mock EmailService implementation.");
        return new MockEmailService();
    }

    private static class MockEmailService implements IEmailService {

        @Override
        public void sendEmailVerification(User user) {
            log.info("[MOCK EMAIL SERVICE] sendEmailVerification called for user: {}", user.email);
        }

        @Override
        public void sendPasswordResetEmail(User user) {
            log.info("[MOCK EMAIL SERVICE] sendPasswordResetEmail called for user: {}", user.email);
        }
    }
}

