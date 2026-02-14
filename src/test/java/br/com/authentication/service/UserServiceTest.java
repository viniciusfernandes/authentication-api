package br.com.authentication.service;

import br.com.authentication.domain.model.User;
import br.com.authentication.domain.model.UserStatus;
import br.com.authentication.domain.model.ExternalProvider;
import br.com.authentication.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IEmailService emailService;

    @Mock
    private IJwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String testEmail;
    private String testPassword;
    private String testFullName;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "ValidPass123!";
        testFullName = "Test User";

        testUser = User.builder()
                .id(1L)
                .email(testEmail)
                .password("hashedPassword123")
                .fullName(testFullName)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUserSuccessfullyWithValidData() {
            // Given
            String hashedPassword = "hashedPassword123";
            when(passwordEncoder.encode(testPassword)).thenReturn(hashedPassword);
            when(userRepository.existsByEmail(testEmail)).thenReturn(false);
            
            // Create a user with the expected status for the mock return
            User expectedUser = User.builder()
                    .id(1L)
                    .email(testEmail)
                    .password(hashedPassword)
                    .fullName(testFullName)
                    .status(UserStatus.PENDING_VERIFICATION)
                    .emailVerified(false)
                    .emailVerificationToken("verification-token-123")
                    .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            when(userRepository.save(any(User.class))).thenReturn(expectedUser);

            // When
            User createdUser = userService.createUser(testEmail, testPassword, testFullName);

            // Then
            assertNotNull(createdUser);
            assertEquals(testEmail, createdUser.email);
            assertEquals(testFullName, createdUser.fullName);
            assertEquals(hashedPassword, createdUser.password);
            assertEquals(UserStatus.PENDING_VERIFICATION, createdUser.status);
            assertFalse(createdUser.emailVerified);
            assertNotNull(createdUser.emailVerificationToken);
            assertNotNull(createdUser.emailVerificationTokenExpiry);

            verify(passwordEncoder).encode(testPassword);
            verify(userRepository).existsByEmail(testEmail);
            verify(userRepository).save(any(User.class));
            verify(emailService).sendEmailVerification(createdUser);
        }

        @Test
        @DisplayName("Should throw exception when user already exists")
        void shouldThrowExceptionWhenUserAlreadyExists() {
            // Given
            when(userRepository.existsByEmail(testEmail)).thenReturn(true);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                userService.createUser(testEmail, testPassword, testFullName);
            });

            verify(userRepository).existsByEmail(testEmail);
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendEmailVerification(any(User.class));
        }

        @Test
        @DisplayName("Should create user with external provider")
        void shouldCreateUserWithExternalProvider() {
            // Given
            String externalProviderId = "google_123456";
            ExternalProvider externalProvider = ExternalProvider.GOOGLE;
            when(userRepository.findByExternalProviderIdAndExternalProvider(externalProviderId, externalProvider))
                    .thenReturn(Optional.empty());
            
            // Create a user with the expected external provider fields for the mock return
            User expectedUser = User.builder()
                    .id(1L)
                    .email(testEmail)
                    .password(null)
                    .fullName(testFullName)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .externalProviderId(externalProviderId)
                    .externalProvider(externalProvider)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            when(userRepository.save(any(User.class))).thenReturn(expectedUser);

            // When
            User createdUser = userService.createUserFromExternalProvider(
                    testEmail, testFullName, externalProviderId, externalProvider);

            // Then
            assertNotNull(createdUser);
            assertEquals(testEmail, createdUser.email);
            assertEquals(testFullName, createdUser.fullName);
            assertEquals(externalProviderId, createdUser.externalProviderId);
            assertEquals(externalProvider, createdUser.externalProvider);
            assertEquals(UserStatus.ACTIVE, createdUser.status);
            assertTrue(createdUser.emailVerified);

            verify(userRepository).findByExternalProviderIdAndExternalProvider(externalProviderId, externalProvider);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when external provider user already exists")
        void shouldThrowExceptionWhenExternalProviderUserAlreadyExists() {
            // Given
            String externalProviderId = "google_123456";
            ExternalProvider externalProvider = ExternalProvider.GOOGLE;
            when(userRepository.findByExternalProviderIdAndExternalProvider(externalProviderId, externalProvider))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                userService.createUserFromExternalProvider(
                        testEmail, testFullName, externalProviderId, externalProvider);
            });

            verify(userRepository).findByExternalProviderIdAndExternalProvider(externalProviderId, externalProvider);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Find User Tests")
    class FindUserTests {

        @Test
        @DisplayName("Should find user by email when user exists")
        void shouldFindUserByEmailWhenUserExists() {
            // Given
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

            // When
            User foundUser = userService.findByEmail(testEmail);

            // Then
            assertNotNull(foundUser);
            assertEquals(testEmail, foundUser.email);
            verify(userRepository).findByEmail(testEmail);
        }

        @Test
        @DisplayName("Should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // Given
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UsernameNotFoundException.class, () -> {
                userService.findByEmail(testEmail);
            });
            verify(userRepository).findByEmail(testEmail);
        }

        @Test
        @DisplayName("Should find user by email verification token")
        void shouldFindUserByEmailVerificationToken() {
            // Given
            String token = "verification-token-123";
            testUser = User.builder()
                    .id(testUser.id)
                    .email(testUser.email)
                    .password(testUser.password)
                    .fullName(testUser.fullName)
                    .status(testUser.status)
                    .emailVerified(testUser.emailVerified)
                    .emailVerificationToken(token)
                    .createdAt(testUser.createdAt)
                    .build();
            when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));

            // When
            User foundUser = userService.findByEmailVerificationToken(token);

            // Then
            assertNotNull(foundUser);
            assertEquals(token, foundUser.emailVerificationToken);
            verify(userRepository).findByEmailVerificationToken(token);
        }

        @Test
        @DisplayName("Should find user by password reset token")
        void shouldFindUserByPasswordResetToken() {
            // Given
            String token = "reset-token-456";
            testUser = User.builder()
                    .id(testUser.id)
                    .email(testUser.email)
                    .password(testUser.password)
                    .fullName(testUser.fullName)
                    .status(testUser.status)
                    .emailVerified(testUser.emailVerified)
                    .passwordResetToken(token)
                    .createdAt(testUser.createdAt)
                    .build();
            when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(testUser));

            // When
            User foundUser = userService.findByPasswordResetToken(token);

            // Then
            assertNotNull(foundUser);
            assertEquals(token, foundUser.passwordResetToken);
            verify(userRepository).findByPasswordResetToken(token);
        }
    }

    @Nested
    @DisplayName("Email Verification Tests")
    class EmailVerificationTests {

        @Test
        @DisplayName("Should verify email successfully with valid token")
        void shouldVerifyEmailSuccessfullyWithValidToken() {
            // Given
            String token = "verification-token-123";
            testUser = User.builder()
                    .id(testUser.id)
                    .email(testUser.email)
                    .password(testUser.password)
                    .fullName(testUser.fullName)
                    .status(testUser.status)
                    .emailVerified(testUser.emailVerified)
                    .emailVerificationToken(token)
                    .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(1))
                    .createdAt(testUser.createdAt)
                    .build();
            when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.verifyEmail(token);

            // Then
            assertTrue(testUser.emailVerified);
            assertNull(testUser.emailVerificationToken);
            assertNull(testUser.emailVerificationTokenExpiry);
            assertEquals(UserStatus.ACTIVE, testUser.status);

            verify(userRepository).findByEmailVerificationToken(token);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when verification token is invalid")
        void shouldThrowExceptionWhenVerificationTokenIsInvalid() {
            // Given
            String token = "invalid-token";
            when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                userService.verifyEmail(token);
            });

            verify(userRepository).findByEmailVerificationToken(token);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when verification token is expired")
        void shouldThrowExceptionWhenVerificationTokenIsExpired() {
            // Given
            String token = "expired-token";
            testUser = User.builder()
                    .id(testUser.id)
                    .email(testUser.email)
                    .password(testUser.password)
                    .fullName(testUser.fullName)
                    .status(testUser.status)
                    .emailVerified(testUser.emailVerified)
                    .emailVerificationToken(token)
                    .emailVerificationTokenExpiry(LocalDateTime.now().minusHours(1)) // Expired
                    .createdAt(testUser.createdAt)
                    .build();
            when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                userService.verifyEmail(token);
            });

            verify(userRepository).findByEmailVerificationToken(token);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("Should initiate password reset successfully")
        void shouldInitiatePasswordResetSuccessfully() {
            // Given
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.initiatePasswordReset(testEmail);

            // Then
            assertNotNull(testUser.passwordResetToken);
            assertNotNull(testUser.passwordResetTokenExpiry);

            verify(userRepository).findByEmail(testEmail);
            verify(userRepository).save(testUser);
            verify(emailService).sendPasswordResetEmail(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user does not exist for password reset")
        void shouldThrowExceptionWhenUserDoesNotExistForPasswordReset() {
            // Given
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UsernameNotFoundException.class, () -> {
                userService.initiatePasswordReset(testEmail);
            });

            verify(userRepository).findByEmail(testEmail);
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendPasswordResetEmail(any(User.class));
        }

        @Test
        @DisplayName("Should reset password successfully with valid token")
        void shouldResetPasswordSuccessfullyWithValidToken() {
            // Given
            String token = "reset-token-456";
            String newPassword = "NewPass123!";
            String hashedPassword = "hashedNewPassword123";
            testUser = User.builder()
                    .id(testUser.id)
                    .email(testUser.email)
                    .password(testUser.password)
                    .fullName(testUser.fullName)
                    .status(testUser.status)
                    .emailVerified(testUser.emailVerified)
                    .passwordResetToken(token)
                    .passwordResetTokenExpiry(LocalDateTime.now().plusHours(1))
                    .createdAt(testUser.createdAt)
                    .build();
            when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(newPassword)).thenReturn(hashedPassword);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.resetPassword(token, newPassword);

            // Then
            assertEquals(hashedPassword, testUser.password);
            assertNull(testUser.passwordResetToken);
            assertNull(testUser.passwordResetTokenExpiry);

            verify(userRepository).findByPasswordResetToken(token);
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when reset token is invalid")
        void shouldThrowExceptionWhenResetTokenIsInvalid() {
            // Given
            String token = "invalid-token";
            String newPassword = "NewPass123!";
            when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                userService.resetPassword(token, newPassword);
            });

            verify(userRepository).findByPasswordResetToken(token);
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("UserDetailsService Tests")
    class UserDetailsServiceTests {

        @Test
        @DisplayName("Should load user by username successfully")
        void shouldLoadUserByUsernameSuccessfully() {
            // Given
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

            // When
            UserDetails userDetails = userService.loadUserByUsername(testEmail);

            // Then
            assertNotNull(userDetails);
            assertEquals(testEmail, userDetails.getUsername());
            assertEquals(testUser.password, userDetails.getPassword());
            assertTrue(userDetails.isEnabled());
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());

            verify(userRepository).findByEmail(testEmail);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user does not exist")
        void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
            // Given
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UsernameNotFoundException.class, () -> {
                userService.loadUserByUsername(testEmail);
            });

            verify(userRepository).findByEmail(testEmail);
        }
    }

    @Nested
    @DisplayName("Profile Management Tests")
    class ProfileManagementTests {

        @Test
        @DisplayName("Should update profile successfully")
        void shouldUpdateProfileSuccessfully() {
            // Given
            String newFullName = "Updated Full Name";
            String newPhone = "+1234567890";
            String newProfilePicture = "https://example.com/profile.jpg";
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.updatedAt = LocalDateTime.now();
                return user;
            });

            // When
            User updatedUser = userService.updateProfile(testUser, newFullName, newPhone, newProfilePicture);

            // Then
            assertEquals(newFullName, updatedUser.fullName);
            assertEquals(newPhone, updatedUser.phone);
            assertEquals(newProfilePicture, updatedUser.profilePicture);
            assertNotNull(updatedUser.updatedAt);

            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            String currentPassword = "CurrentPass123!";
            String newPassword = "NewPass123!";
            String hashedCurrentPassword = "hashedCurrentPassword";
            String hashedNewPassword = "hashedNewPassword";
            testUser = User.builder()
                    .id(testUser.id)
                    .email(testUser.email)
                    .password(hashedCurrentPassword)
                    .fullName(testUser.fullName)
                    .status(testUser.status)
                    .emailVerified(testUser.emailVerified)
                    .createdAt(testUser.createdAt)
                    .build();
            when(passwordEncoder.matches(currentPassword, hashedCurrentPassword)).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.updatedAt = LocalDateTime.now();
                return user;
            });

            // When
            userService.changePassword(testUser, currentPassword, newPassword);

            // Then
            assertEquals(hashedNewPassword, testUser.password);
            assertNotNull(testUser.updatedAt);

            verify(passwordEncoder).matches(currentPassword, hashedCurrentPassword);
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
            // Given
            String currentPassword = "WrongPassword123!";
            String newPassword = "NewPass123!";
            String hashedCurrentPassword = "hashedCurrentPassword";
            testUser = User.builder()
                    .id(testUser.id)
                    .email(testUser.email)
                    .password(hashedCurrentPassword)
                    .fullName(testUser.fullName)
                    .status(testUser.status)
                    .emailVerified(testUser.emailVerified)
                    .createdAt(testUser.createdAt)
                    .build();
            when(passwordEncoder.matches(currentPassword, hashedCurrentPassword)).thenReturn(false);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                userService.changePassword(testUser, currentPassword, newPassword);
            });

            verify(passwordEncoder).matches(currentPassword, hashedCurrentPassword);
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {

        @Test
        @DisplayName("Should lock user successfully")
        void shouldLockUserSuccessfully() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.updatedAt = LocalDateTime.now();
                return user;
            });

            // When
            userService.lockUser(userId);

            // Then
            assertEquals(UserStatus.LOCKED, testUser.status);
            assertNotNull(testUser.updatedAt);

            verify(userRepository).findById(userId);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should count users correctly")
        void shouldCountUsersCorrectly() {
            // Given
            long expectedCount = 5L;
            when(userRepository.count()).thenReturn(expectedCount);

            // When
            long actualCount = userService.countUsers();

            // Then
            assertEquals(expectedCount, actualCount);
            verify(userRepository).count();
        }
    }
}
