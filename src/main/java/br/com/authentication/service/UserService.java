package br.com.authentication.service;

import br.com.authentication.domain.exception.ConflictException;
import br.com.authentication.domain.exception.NotFoundException;
import br.com.authentication.domain.model.Claim;
import br.com.authentication.domain.model.User;
import br.com.authentication.domain.model.UserStatus;
import br.com.authentication.domain.repository.ClaimRepository;
import br.com.authentication.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements IUserService {
    
    private final UserRepository userRepository;
    private final ClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Value("${app.email-verification-enabled:true}")
    private boolean emailVerificationEnabled;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    public User createUser(String email, String password, String fullName, List<ClaimAssignment> claims) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email " + email + " already exists");
        }

        // Validate the claims before anything is sent or saved.
        Map<Claim, String> resolvedClaims = resolveClaims(claims);
        
        String hashedPassword = passwordEncoder.encode(password);
        User.UserBuilder userBuilder = User.builder()
                .email(email)
                .password(hashedPassword)
                .fullName(fullName);

        if (emailVerificationEnabled) {
            String verificationToken = UUID.randomUUID().toString();
            LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

            userBuilder
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                    .emailVerificationTokenExpiry(tokenExpiry);

            User user = userBuilder.build();
            // Send email verification before saving to ensure rollback works
            emailService.sendEmailVerification(user);
            return saveWithClaims(user, resolvedClaims);
        }

        User user = userBuilder
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .emailVerificationToken(null)
                .emailVerificationTokenExpiry(null)
                .build();
        
        return saveWithClaims(user, resolvedClaims);
    }

    public User updateUser(Long userId, String fullName, String phone, String profilePicture,
                           List<ClaimAssignment> claims) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // claims == null keeps the current claims; an empty list removes all of them.
        Map<Claim, String> resolvedClaims = claims == null ? null : resolveClaims(claims);

        user.fullName = fullName;
        user.phone = phone;
        user.profilePicture = profilePicture;

        if (resolvedClaims != null) {
            user.retainClaims(resolvedClaims.keySet().stream().map(claim -> claim.id).collect(Collectors.toSet()));
            resolvedClaims.forEach(user::addClaim);
        }

        return userRepository.save(user);
    }

    // The user must exist (have an id) before its UserClaim rows, so it is saved first.
    private User saveWithClaims(User user, Map<Claim, String> resolvedClaims) {
        User saved = userRepository.save(user);
        if (!resolvedClaims.isEmpty()) {
            resolvedClaims.forEach(saved::addClaim);
            saved = userRepository.save(saved);
        }
        return saved;
    }

    /** Loads the claims by id and validates each value against the claim type. */
    private Map<Claim, String> resolveClaims(List<ClaimAssignment> assignments) {
        Map<Claim, String> resolved = new LinkedHashMap<>();
        if (assignments == null) {
            return resolved;
        }

        Set<Long> seenIds = new HashSet<>();
        for (ClaimAssignment assignment : assignments) {
            if (!seenIds.add(assignment.claimId())) {
                throw new IllegalArgumentException("Claim " + assignment.claimId() + " was informed more than once");
            }

            Claim claim = claimRepository.findById(assignment.claimId())
                    .orElseThrow(() -> new NotFoundException("Claim not found with id: " + assignment.claimId()));

            claim.valueType.parse(assignment.value()); // throws IllegalArgumentException when invalid
            resolved.put(claim, assignment.value().trim());
        }
        return resolved;
    }
    
    public User createUserFromExternalProvider(String email, String fullName, 
                                             String externalProviderId, 
                                             br.com.authentication.domain.model.ExternalProvider externalProvider) {
        if (userRepository.findByExternalProviderIdAndExternalProvider(externalProviderId, externalProvider).isPresent()) {
            throw new IllegalArgumentException("User with external provider " + externalProvider + " and ID " + externalProviderId + " already exists");
        }
        
        User user = User.builder()
                .email(email)
                .password(null) // No password for external provider users
                .fullName(fullName)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .externalProviderId(externalProviderId)
                .externalProvider(externalProvider)
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
                                     br.com.authentication.domain.model.ExternalProvider externalProvider) {
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
        
        user.passwordResetToken = resetToken;
        user.passwordResetTokenExpiry = tokenExpiry;
        userRepository.save(user);
        emailService.sendPasswordResetEmail(user);
    }
    
    public void resetPassword(String token, String newPassword) {
        User user = findByPasswordResetToken(token);
        
        if (!user.isPasswordResetTokenValid()) {
            throw new IllegalArgumentException("Password reset token has expired");
        }
        
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.password = hashedPassword;
        user.clearPasswordResetToken();
        userRepository.save(user);
    }
    
    public User updateProfile(User user, String fullName, String phone, String profilePicture) {
        user.fullName = fullName;
        user.phone = phone;
        user.profilePicture = profilePicture;
        return userRepository.save(user);
    }
    
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.password = hashedPassword;
        userRepository.save(user);
    }
    
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.lock();
        userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }
}
