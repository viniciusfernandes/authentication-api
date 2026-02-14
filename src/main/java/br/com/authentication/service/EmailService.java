package br.com.authentication.service;

import br.com.authentication.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    public void sendEmailVerification(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.email);
            message.setSubject("Verify your email address");
            message.setText(buildEmailVerificationMessage(user));
            
            mailSender.send(message);
            log.info("Email verification sent to: {}", user.email);
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", user.email, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    public void sendPasswordResetEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.email);
            message.setSubject("Reset your password");
            message.setText(buildPasswordResetMessage(user));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    private String buildEmailVerificationMessage(User user) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + user.emailVerificationToken;
        
        return String.format("""
                Hello %s,
                
                Thank you for registering with our service. Please click the link below to verify your email address:
                
                %s
                
                This link will expire in 24 hours.
                
                If you did not create an account, please ignore this email.
                
                Best regards,
                The Authentication Team
                """, user.fullName, verificationUrl);
    }
    
    private String buildPasswordResetMessage(User user) {
        String resetUrl = frontendUrl + "/reset-password?token=" + user.passwordResetToken;
        
        return String.format("""
                Hello %s,
                
                You requested to reset your password. Please click the link below to reset your password:
                
                %s
                
                This link will expire in 1 hour.
                
                If you did not request this password reset, please ignore this email.
                
                Best regards,
                The Authentication Team
                """, user.fullName, resetUrl);
    }
}
