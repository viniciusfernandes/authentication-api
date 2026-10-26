package br.com.authentication.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Column(nullable = false, unique = true)
    public String email;
    
    @Column(nullable = false)
    public String password;
    
    @Column(nullable = false)
    public String fullName;
    
    public String phone;
    
    public String profilePicture;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserStatus status;
    
    @Column(name = "email_verified")
    public boolean emailVerified;
    
    @Column(name = "email_verification_token")
    public String emailVerificationToken;
    
    @Column(name = "email_verification_token_expiry")
    public LocalDateTime emailVerificationTokenExpiry;
    
    @Column(name = "password_reset_token")
    public String passwordResetToken;
    
    @Column(name = "password_reset_token_expiry")
    public LocalDateTime passwordResetTokenExpiry;
    
    @Column(name = "external_provider_id")
    public String externalProviderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "external_provider")
    public ExternalProvider externalProvider;
    
    // Extra claims written into the JWT (N-N with the claim catalog, value kept per user).
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    public Set<UserClaim> claims = new HashSet<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // No roles/authorities in this phase of the project: access is decided only by authentication.
        return List.of();
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE && emailVerified;
    }
    
    // Domain methods
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiry = null;
    }
    
    public void lock() {
        this.status = UserStatus.LOCKED;
    }
    
    public void setEmailVerificationToken(String token, LocalDateTime expiry) {
        this.emailVerificationToken = token;
        this.emailVerificationTokenExpiry = expiry;
    }
    
    public void setPasswordResetToken(String token, LocalDateTime expiry) {
        this.passwordResetToken = token;
        this.passwordResetTokenExpiry = expiry;
    }
    
    public boolean isEmailVerificationTokenValid() {
        return emailVerificationToken != null && 
               emailVerificationTokenExpiry != null && 
               LocalDateTime.now().isBefore(emailVerificationTokenExpiry);
    }
    
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null && 
               passwordResetTokenExpiry != null && 
               LocalDateTime.now().isBefore(passwordResetTokenExpiry);
    }
    
    public void clearEmailVerificationToken() {
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiry = null;
    }
    
    /** Sets (or replaces) the value of a claim for this user. */
    public void addClaim(Claim claim, String value) {
        // Updates in place when the user already has the claim (avoids delete + insert of the same key).
        for (UserClaim userClaim : claims) {
            if (userClaim.claim.id.equals(claim.id)) {
                userClaim.value = value;
                return;
            }
        }
        claims.add(new UserClaim(this, claim, value));
    }

    /** Removes every claim of the user whose id is not in {@code claimIdsToKeep}. */
    public void retainClaims(Set<Long> claimIdsToKeep) {
        claims.removeIf(userClaim -> !claimIdsToKeep.contains(userClaim.claim.id));
    }

    public void removeClaim(Claim claim) {
        claims.removeIf(userClaim -> userClaim.claim.id.equals(claim.id));
    }

    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetTokenExpiry = null;
    }
}
