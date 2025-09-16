package br.com.ovigia.domain.repository;

import br.com.ovigia.domain.model.ExternalProvider;
import br.com.ovigia.domain.model.User;
import br.com.ovigia.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("hashedPassword123")
                .fullName("Test User")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Find by Email Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by email when user exists")
        void shouldFindUserByEmailWhenUserExists() {
            // Given
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> foundUser = userRepository.findByEmail("test@example.com");

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals("test@example.com", foundUser.get().email);
            assertEquals("Test User", foundUser.get().fullName);
        }

        @Test
        @DisplayName("Should return empty when user does not exist")
        void shouldReturnEmptyWhenUserDoesNotExist() {
            // When
            Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

            // Then
            assertFalse(foundUser.isPresent());
        }

        @Test
        @DisplayName("Should be case sensitive for email search")
        void shouldBeCaseSensitiveForEmailSearch() {
            // Given
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> foundUser = userRepository.findByEmail("TEST@EXAMPLE.COM");

            // Then
            assertFalse(foundUser.isPresent());
        }
    }

    @Nested
    @DisplayName("Find by Email Verification Token Tests")
    class FindByEmailVerificationTokenTests {

        @Test
        @DisplayName("Should find user by email verification token")
        void shouldFindUserByEmailVerificationToken() {
            // Given
            String token = "verification-token-123";
            testUser.emailVerificationToken = token;
            testUser.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24);
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> foundUser = userRepository.findByEmailVerificationToken(token);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(token, foundUser.get().emailVerificationToken);
            assertEquals("test@example.com", foundUser.get().email);
        }

        @Test
        @DisplayName("Should return empty when token does not exist")
        void shouldReturnEmptyWhenTokenDoesNotExist() {
            // When
            Optional<User> foundUser = userRepository.findByEmailVerificationToken("invalid-token");

            // Then
            assertFalse(foundUser.isPresent());
        }
    }

    @Nested
    @DisplayName("Find by Password Reset Token Tests")
    class FindByPasswordResetTokenTests {

        @Test
        @DisplayName("Should find user by password reset token")
        void shouldFindUserByPasswordResetToken() {
            // Given
            String token = "reset-token-456";
            testUser.passwordResetToken = token;
            testUser.passwordResetTokenExpiry = LocalDateTime.now().plusHours(1);
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> foundUser = userRepository.findByPasswordResetToken(token);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(token, foundUser.get().passwordResetToken);
            assertEquals("test@example.com", foundUser.get().email);
        }

        @Test
        @DisplayName("Should return empty when reset token does not exist")
        void shouldReturnEmptyWhenResetTokenDoesNotExist() {
            // When
            Optional<User> foundUser = userRepository.findByPasswordResetToken("invalid-reset-token");

            // Then
            assertFalse(foundUser.isPresent());
        }
    }

    @Nested
    @DisplayName("Find by External Provider Tests")
    class FindByExternalProviderTests {

        @Test
        @DisplayName("Should find user by external provider")
        void shouldFindUserByExternalProvider() {
            // Given
            String externalProviderId = "google_123456";
            ExternalProvider externalProvider = ExternalProvider.GOOGLE;
            testUser.externalProviderId = externalProviderId;
            testUser.externalProvider = externalProvider;
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> foundUser = userRepository.findByExternalProviderIdAndExternalProvider(externalProviderId, externalProvider);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(externalProviderId, foundUser.get().externalProviderId);
            assertEquals(externalProvider, foundUser.get().externalProvider);
            assertEquals("test@example.com", foundUser.get().email);
        }

        @Test
        @DisplayName("Should return empty when external provider combination does not exist")
        void shouldReturnEmptyWhenExternalProviderCombinationDoesNotExist() {
            // When
            Optional<User> foundUser = userRepository.findByExternalProviderIdAndExternalProvider("facebook_789", ExternalProvider.FACEBOOK);

            // Then
            assertFalse(foundUser.isPresent());
        }

        @Test
        @DisplayName("Should handle different external providers correctly")
        void shouldHandleDifferentExternalProvidersCorrectly() {
            // Given
            User googleUser = User.builder()
                    .email("google@example.com")
                    .password("hashedPassword123")
                    .fullName("Google User")
                    .externalProviderId("google_123")
                    .externalProvider(ExternalProvider.GOOGLE)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            User facebookUser = User.builder()
                    .email("facebook@example.com")
                    .password("hashedPassword123")
                    .fullName("Facebook User")
                    .externalProviderId("facebook_456")
                    .externalProvider(ExternalProvider.FACEBOOK)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(googleUser);
            entityManager.persistAndFlush(facebookUser);

            // When
            Optional<User> foundGoogleUser = userRepository.findByExternalProviderIdAndExternalProvider("google_123", ExternalProvider.GOOGLE);
            Optional<User> foundFacebookUser = userRepository.findByExternalProviderIdAndExternalProvider("facebook_456", ExternalProvider.FACEBOOK);

            // Then
            assertTrue(foundGoogleUser.isPresent());
            assertEquals("google@example.com", foundGoogleUser.get().email);
            assertEquals(ExternalProvider.GOOGLE, foundGoogleUser.get().externalProvider);

            assertTrue(foundFacebookUser.isPresent());
            assertEquals("facebook@example.com", foundFacebookUser.get().email);
            assertEquals(ExternalProvider.FACEBOOK, foundFacebookUser.get().externalProvider);
        }
    }

    @Nested
    @DisplayName("Save User Tests")
    class SaveUserTests {

        @Test
        @DisplayName("Should save new user successfully")
        void shouldSaveNewUserSuccessfully() {
            // When
            User savedUser = userRepository.save(testUser);

            // Then
            assertNotNull(savedUser.id);
            assertEquals("test@example.com", savedUser.email);
            assertEquals("Test User", savedUser.fullName);
            assertEquals(UserStatus.ACTIVE, savedUser.status);
        }

        @Test
        @DisplayName("Should update existing user successfully")
        void shouldUpdateExistingUserSuccessfully() {
            // Given
            User savedUser = userRepository.save(testUser);
            String newFullName = "Updated Test User";
            
            // Create a new user object with updated data
            User updatedUserData = User.builder()
                    .id(savedUser.id)
                    .email(savedUser.email)
                    .password(savedUser.password)
                    .fullName(newFullName)
                    .status(savedUser.status)
                    .emailVerified(savedUser.emailVerified)
                    .createdAt(savedUser.createdAt)
                    .updatedAt(LocalDateTime.now())
                    .build();

            // When
            User updatedUser = userRepository.save(updatedUserData);

            // Then
            assertEquals(savedUser.id, updatedUser.id);
            assertEquals(newFullName, updatedUser.fullName);
            assertEquals("test@example.com", updatedUser.email);
        }

        @Test
        @DisplayName("Should handle user with all fields populated")
        void shouldHandleUserWithAllFieldsPopulated() {
            // Given
            testUser.phone = "+1234567890";
            testUser.profilePicture = "https://example.com/profile.jpg";
            testUser.emailVerificationToken = "token-123";
            testUser.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24);
            testUser.passwordResetToken = "reset-456";
            testUser.passwordResetTokenExpiry = LocalDateTime.now().plusHours(1);
            testUser.externalProviderId = "google_789";
            testUser.externalProvider = ExternalProvider.GOOGLE;
            testUser.updatedAt = LocalDateTime.now();

            // When
            User savedUser = userRepository.save(testUser);

            // Then
            assertNotNull(savedUser.id);
            assertEquals("+1234567890", savedUser.phone);
            assertEquals("https://example.com/profile.jpg", savedUser.profilePicture);
            assertEquals("token-123", savedUser.emailVerificationToken);
            assertEquals("reset-456", savedUser.passwordResetToken);
            assertEquals("google_789", savedUser.externalProviderId);
            assertEquals(ExternalProvider.GOOGLE, savedUser.externalProvider);
            assertNotNull(savedUser.updatedAt);
        }
    }

    @Nested
    @DisplayName("Count Users Tests")
    class CountUsersTests {

        @Test
        @DisplayName("Should return zero when no users exist")
        void shouldReturnZeroWhenNoUsersExist() {
            // When
            long count = userRepository.count();

            // Then
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Should return correct count when users exist")
        void shouldReturnCorrectCountWhenUsersExist() {
            // Given
            User user1 = User.builder()
                    .email("user1@example.com")
                    .password("hashedPassword123")
                    .fullName("User One")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            User user2 = User.builder()
                    .email("user2@example.com")
                    .password("hashedPassword123")
                    .fullName("User Two")
                    .status(UserStatus.PENDING_VERIFICATION)
                    .emailVerified(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(user1);
            entityManager.persistAndFlush(user2);

            // When
            long count = userRepository.count();

            // Then
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("Email Uniqueness Tests")
    class EmailUniquenessTests {

        @Test
        @DisplayName("Should enforce email uniqueness constraint")
        void shouldEnforceEmailUniquenessConstraint() {
            // Given
            User user1 = User.builder()
                    .email("duplicate@example.com")
                    .password("hashedPassword123")
                    .fullName("User One")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            User user2 = User.builder()
                    .email("duplicate@example.com") // Same email
                    .password("hashedPassword123")
                    .fullName("User Two")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            // When
            userRepository.save(user1);

            // Then
            assertThrows(Exception.class, () -> {
                userRepository.save(user2);
                entityManager.flush();
            });
        }
    }
}
