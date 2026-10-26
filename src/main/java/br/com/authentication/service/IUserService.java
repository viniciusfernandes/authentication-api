package br.com.authentication.service;

import br.com.authentication.domain.model.User;
import br.com.authentication.domain.model.ExternalProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface IUserService extends UserDetailsService {
    
    User createUser(String email, String password, String fullName, List<ClaimAssignment> claims);

    /** Updates the user data; {@code claims == null} keeps the current claims, otherwise they are replaced. */
    User updateUser(Long userId, String fullName, String phone, String profilePicture,
                    List<ClaimAssignment> claims);
    
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
