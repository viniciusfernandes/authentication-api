package br.com.authentication.util;

import br.com.authentication.domain.model.ExternalProvider;
import br.com.authentication.domain.model.User;
import br.com.authentication.domain.model.UserStatus;
import br.com.authentication.usecase.user.create.contract.RegisterUserRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 * Follows the Test Data Builder pattern for better test readability and maintainability.
 */
public class TestDataFactory {

    private TestDataFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a valid RegisterUserRequest for testing.
     */
    public static RegisterUserRequest createValidRegisterUserRequest() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.email = "test@example.com";
        request.password = "ValidPass123!";
        request.confirmPassword = "ValidPass123!";
        request.fullName = "Test User";
        return request;
    }

    /**
     * Creates a RegisterUserRequest with custom email.
     */
    public static RegisterUserRequest createRegisterUserRequestWithEmail(String email) {
        RegisterUserRequest request = createValidRegisterUserRequest();
        request.email = email;
        return request;
    }

    /**
     * Creates a RegisterUserRequest with custom password.
     */
    public static RegisterUserRequest createRegisterUserRequestWithPassword(String password) {
        RegisterUserRequest request = createValidRegisterUserRequest();
        request.password = password;
        request.confirmPassword = password;
        return request;
    }

    /**
     * Creates a RegisterUserRequest with custom full name.
     */
    public static RegisterUserRequest createRegisterUserRequestWithFullName(String fullName) {
        RegisterUserRequest request = createValidRegisterUserRequest();
        request.fullName = fullName;
        return request;
    }

    /**
     * Creates a RegisterUserRequest with mismatched passwords.
     */
    public static RegisterUserRequest createRegisterUserRequestWithMismatchedPasswords() {
        RegisterUserRequest request = createValidRegisterUserRequest();
        request.password = "ValidPass123!";
        request.confirmPassword = "DifferentPass123!";
        return request;
    }

    /**
     * Creates a valid User entity for testing.
     */
    public static User createValidUser() {
        User user = new User();
        user.id = 1L;
        user.email = "test@example.com";
        user.password = "hashedPassword123";
        user.fullName = "Test User";
        user.status = UserStatus.ACTIVE;
        user.emailVerified = true;
        user.createdAt = LocalDateTime.now();
        return user;
    }

    /**
     * Creates a User with custom email.
     */
    public static User createUserWithEmail(String email) {
        User user = createValidUser();
        user.email = email;
        return user;
    }

    /**
     * Creates a User with custom status.
     */
    public static User createUserWithStatus(UserStatus status) {
        User user = createValidUser();
        user.status = status;
        return user;
    }

    /**
     * Creates a pending User (not email verified).
     */
    public static User createPendingUser() {
        User user = createValidUser();
            user.status = UserStatus.PENDING_VERIFICATION;
        user.emailVerified = false;
        user.emailVerificationToken = generateToken();
        user.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24);
        return user;
    }

    /**
     * Creates a User with external provider.
     */
    public static User createUserWithExternalProvider(String email, String externalProviderId, ExternalProvider externalProvider) {
        User user = createValidUser();
        user.email = email;
        user.externalProviderId = externalProviderId;
        user.externalProvider = externalProvider;
        user.emailVerified = true;
        return user;
    }

    /**
     * Creates a User with password reset token.
     */
    public static User createUserWithPasswordResetToken() {
        User user = createValidUser();
        user.passwordResetToken = generateToken();
        user.passwordResetTokenExpiry = LocalDateTime.now().plusHours(1);
        return user;
    }

    /**
     * Creates a User with all fields populated.
     */
    public static User createUserWithAllFields() {
        User user = createValidUser();
        user.phone = "+1234567890";
        user.profilePicture = "https://example.com/profile.jpg";
        user.emailVerificationToken = generateToken();
        user.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24);
        user.passwordResetToken = generateToken();
        user.passwordResetTokenExpiry = LocalDateTime.now().plusHours(1);
        user.externalProviderId = "google_123456";
        user.externalProvider = ExternalProvider.GOOGLE;
        user.updatedAt = LocalDateTime.now();
        return user;
    }

    /**
     * Creates a User with expired verification token.
     */
    public static User createUserWithExpiredVerificationToken() {
        User user = createPendingUser();
        user.emailVerificationTokenExpiry = LocalDateTime.now().minusHours(1);
        return user;
    }

    /**
     * Creates a User with expired password reset token.
     */
    public static User createUserWithExpiredPasswordResetToken() {
        User user = createUserWithPasswordResetToken();
        user.passwordResetTokenExpiry = LocalDateTime.now().minusHours(1);
        return user;
    }

    /**
     * Creates a User with locked status.
     */
    public static User createLockedUser() {
        User user = createValidUser();
        user.status = UserStatus.LOCKED;
        return user;
    }

    /**
     * Creates a User with inactive status.
     */
    public static User createInactiveUser() {
        User user = createValidUser();
        user.status = UserStatus.INACTIVE;
        return user;
    }

    /**
     * Generates a random token for testing.
     */
    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Creates a valid email for testing.
     */
    public static String createValidEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * Creates a valid password for testing.
     */
    public static String createValidPassword() {
        return "ValidPass123!";
    }

    /**
     * Creates a valid full name for testing.
     */
    public static String createValidFullName() {
        return "Test User " + System.currentTimeMillis();
    }

    /**
     * Creates an invalid email for testing.
     */
    public static String createInvalidEmail() {
        return "invalid-email";
    }

    /**
     * Creates a weak password for testing.
     */
    public static String createWeakPassword() {
        return "weak";
    }

    /**
     * Creates an empty string for testing.
     */
    public static String createEmptyString() {
        return "";
    }

    /**
     * Creates a null value for testing.
     */
    public static String createNullString() {
        return null;
    }
}
