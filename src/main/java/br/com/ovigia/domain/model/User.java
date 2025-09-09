package br.com.ovigia.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String fullName;
    
    private String phone;
    
    private String profilePicture;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Column(name = "email_verified")
    private boolean emailVerified;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;
    
    @Column(name = "external_provider_id")
    private String externalProviderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "external_provider")
    private ExternalProvider externalProvider;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (roles == null || roles.isEmpty()) {
            roles = Set.of(Role.USER);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }
    
    @Override
    public String getUsername() {
        return email;
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
    
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetTokenExpiry = null;
    }
}
