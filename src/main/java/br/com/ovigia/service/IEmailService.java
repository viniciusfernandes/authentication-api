package br.com.ovigia.service;

import br.com.ovigia.domain.model.User;

public interface IEmailService {
    
    void sendEmailVerification(User user);
    
    void sendPasswordResetEmail(User user);
}
