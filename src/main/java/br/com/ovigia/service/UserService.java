package br.com.ovigia.service;

import br.com.ovigia.domain.model.User;
import br.com.ovigia.domain.model.UserStatus;
import br.com.ovigia.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    public User createUser(String email, String password, String fullName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        String hashedPassword = passwordEncoder.encode(password);
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);
        
        User user = User.builder()
                .email(email)
                .password(hashedPassword)
                .fullName(fullName)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiry(tokenExpiry)
                .roles(java.util.Set.of(br.com.ovigia.domain.model.Role.USER))
                .build();
        
        return userRepository.save(user);
    }
    
    public User createUserFromExternalProvider(String email, String fullName, 
                                             String externalProviderId, 
                                             br.com.ovigia.domain.model.ExternalProvider externalProvider) {
        User user = User.builder()
                .email(email)
                .password(null) // No password for external provider users
                .fullName(fullName)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .externalProviderId(externalProviderId)
                .externalProvider(externalProvider)
                .roles(java.util.Set.of(br.com.ovigia.domain.model.Role.USER))
                .build();
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    @Transactional(readOnly = true)
    public User findByEmailVerificationToken(String token) {
        return userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
    }
    
    @Transactional(readOnly = true)
    public User findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));
    }
    
    @Transactional(readOnly = true)
    public User findByExternalProvider(String externalProviderId, 
                                     br.com.ovigia.domain.model.ExternalProvider externalProvider) {
        return userRepository.findByExternalProviderIdAndExternalProvider(externalProviderId, externalProvider)
                .orElse(null);
    }
    
    public void verifyEmail(String token) {
        User user = findByEmailVerificationToken(token);
        
        if (!user.isEmailVerificationTokenValid()) {
            throw new IllegalArgumentException("Verification token has expired");
        }
        
        user.activate();
        userRepository.save(user);
    }
    
    public void initiatePasswordReset(String email) {
        User user = findByEmail(email);
        
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(1);
        
        user.setPasswordResetToken(resetToken, tokenExpiry);
        userRepository.save(user);
    }
    
    public void resetPassword(String token, String newPassword) {
        User user = findByPasswordResetToken(token);
        
        if (!user.isPasswordResetTokenValid()) {
            throw new IllegalArgumentException("Password reset token has expired");
        }
        
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.clearPasswordResetToken();
        userRepository.save(user);
    }
    
    public User updateProfile(User user, String fullName, String phone, String profilePicture) {
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setProfilePicture(profilePicture);
        return userRepository.save(user);
    }
    
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
    
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.lock();
        userRepository.save(user);
    }
}
