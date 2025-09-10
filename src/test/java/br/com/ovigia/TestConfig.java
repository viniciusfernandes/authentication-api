package br.com.ovigia;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public JavaMailSender testMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(587);
        mailSender.setUsername("test@example.com");
        mailSender.setPassword("test-password");
        return mailSender;
    }
}

