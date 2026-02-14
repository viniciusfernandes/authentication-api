package br.com.authentication.service;

import br.com.authentication.domain.model.User;
import br.com.authentication.domain.model.ExternalProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
    
    User createUser(String email, String password, String fullName);
    
    User createUserFromExternalProvider(String email, String fullName, 
                                      String externalProviderId, 
                                      ExternalProvider externalProvider);
    
    User findByEmail(String email);
    
    User findByEmailVerificationToken(String token);
    
    User findByPasswordResetToken(String token);
    
    User findByExternalProvider(String externalProviderId, ExternalProvider externalProvider);
    
    void verifyEmail(String token);
    
    void initiatePasswordReset(String email);
    
    void resetPassword(String token, String newPassword);
    
    User updateProfile(User user, String fullName, String phone, String profilePicture);
    
    void changePassword(User user, String currentPassword, String newPassword);
    
    void lockUser(Long userId);
    
    long countUsers();
}
