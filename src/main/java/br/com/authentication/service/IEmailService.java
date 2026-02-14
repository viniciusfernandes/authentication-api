package br.com.authentication.service;

import br.com.authentication.domain.model.User;

public interface IEmailService {
    
    void sendEmailVerification(User user);
    
    void sendPasswordResetEmail(User user);
}
