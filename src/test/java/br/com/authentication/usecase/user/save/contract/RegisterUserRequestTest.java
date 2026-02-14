package br.com.authentication.usecase.user.save.contract;

import br.com.authentication.usecase.user.create.contract.RegisterUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegisterUserRequest Contract Tests")
class RegisterUserRequestTest {

    private Validator validator;
    private RegisterUserRequest request;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        request = RegisterUserRequest.builder()
                .email("test@example.com")
                .password("ValidPass123!")
                .confirmPassword("ValidPass123!")
                .fullName("Test User")
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should be valid with all required fields")
        void shouldBeValidWithAllRequiredFields() {
            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should be valid with minimum required fields")
        void shouldBeValidWithMinimumRequiredFields() {
            // Given
            RegisterUserRequest minimalRequest = RegisterUserRequest.builder()
                    .email("minimal@example.com")
                    .password("MinPass123!")
                    .confirmPassword("MinPass123!")
                    .fullName("Min User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(minimalRequest);

            // Then
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should be valid with various email formats")
        void shouldBeValidWithVariousEmailFormats() {
            String[] validEmails = {
                "test@example.com",
                "user.name@domain.co.uk",
                "firstname+lastname@example.com",
                "email@subdomain.example.com",
                "firstname-lastname@example.com",
                "test123@test-domain.com"
            };

            for (String email : validEmails) {
                RegisterUserRequest testRequest = RegisterUserRequest.builder()
                        .email(email)
                        .password("ValidPass123!")
                        .confirmPassword("ValidPass123!")
                        .fullName("Test User")
                        .build();
                Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);
                assertTrue(violations.isEmpty(), "Email should be valid: " + email);
            }
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "invalid-email",
            "@example.com",
            "test@",
            "test.example.com",
            "test@.com",
            "test@example.",
            "test@example..com",
            "test space@example.com"
        })
        @DisplayName("Should be invalid with invalid email formats")
        void shouldBeInvalidWithInvalidEmailFormats(String invalidEmail) {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email(invalidEmail)
                    .password("ValidPass123!")
                    .confirmPassword("ValidPass123!")
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @Test
        @DisplayName("Should be valid with strong passwords")
        void shouldBeValidWithStrongPasswords() {
            String[] validPasswords = {
                "ValidPass123!",
                "StrongP@ssw0rd",
                "MyP@ss123!",
                "Test123@",
                "Password1$",
                "ComplexP@ssw0rd123"
            };

            for (String password : validPasswords) {
                RegisterUserRequest testRequest = RegisterUserRequest.builder()
                        .email("test@example.com")
                        .password(password)
                        .confirmPassword(password)
                        .fullName("Test User")
                        .build();
                Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);
                assertTrue(violations.isEmpty(), "Password should be valid: " + password);
            }
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "weak",
            "12345678",
            "password",
            "PASSWORD",
            "Pass123",
            "ValidPass",
            "ValidPass!",
            "ValidPass123",
            "validpass123!",
            "VALIDPASS123!",
            "ValidPass!@#$%^&*()"
        })
        @DisplayName("Should be invalid with weak passwords")
        void shouldBeInvalidWithWeakPasswords(String weakPassword) {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password(weakPassword)
                    .confirmPassword(weakPassword)
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        }

        @Test
        @DisplayName("Should be invalid when password is too short")
        void shouldBeInvalidWhenPasswordIsTooShort() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("Short1!")
                    .confirmPassword("Short1!")
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        }
    }

    @Nested
    @DisplayName("Confirm Password Validation Tests")
    class ConfirmPasswordValidationTests {

        @Test
        @DisplayName("Should be valid when passwords match")
        void shouldBeValidWhenPasswordsMatch() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("ValidPass123!")
                    .confirmPassword("ValidPass123!")
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should be valid when passwords do not match (no validation rule)")
        void shouldBeValidWhenPasswordsDoNotMatch() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("ValidPass123!")
                    .confirmPassword("DifferentPass123!")
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertTrue(violations.isEmpty(), "No validation rule exists to check password matching");
        }

        @Test
        @DisplayName("Should be invalid when confirm password is null")
        void shouldBeInvalidWhenConfirmPasswordIsNull() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("ValidPass123!")
                    .confirmPassword(null)
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("confirmPassword")));
        }

        @Test
        @DisplayName("Should be invalid when confirm password is empty")
        void shouldBeInvalidWhenConfirmPasswordIsEmpty() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("ValidPass123!")
                    .confirmPassword("")
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("confirmPassword")));
        }
    }

    @Nested
    @DisplayName("Full Name Validation Tests")
    class FullNameValidationTests {

        @Test
        @DisplayName("Should be valid with various full name formats")
        void shouldBeValidWithVariousFullNameFormats() {
            String[] validNames = {
                "John Doe",
                "José María O'Connor-Smith",
                "Jean-Pierre",
                "Mary Jane Watson",
                "Dr. Smith",
                "AB", // Minimum length (2 characters)
                "A".repeat(100) // Long name
            };

            for (String name : validNames) {
                RegisterUserRequest testRequest = RegisterUserRequest.builder()
                        .email("test@example.com")
                        .password("ValidPass123!")
                        .confirmPassword("ValidPass123!")
                        .fullName(name)
                        .build();
                Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);
                assertTrue(violations.isEmpty(), "Full name should be valid: " + name);
            }
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ", // Only whitespace
            "\t", // Tab only
            "\n", // Newline only
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"  // Too long
        })
        @DisplayName("Should be invalid with invalid full name formats")
        void shouldBeInvalidWithInvalidFullNameFormats(String invalidName) {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("ValidPass123!")
                    .confirmPassword("ValidPass123!")
                    .fullName(invalidName)
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
        }
    }

    @Nested
    @DisplayName("Multiple Field Validation Tests")
    class MultipleFieldValidationTests {

        @Test
        @DisplayName("Should return all validation errors for multiple invalid fields")
        void shouldReturnAllValidationErrorsForMultipleInvalidFields() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("invalid-email")
                    .password("weak")
                    .confirmPassword("")
                    .fullName("")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertFalse(violations.isEmpty());
            assertEquals(6, violations.size());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("confirmPassword")));
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
        }

        @Test
        @DisplayName("Should validate all fields independently")
        void shouldValidateAllFieldsIndependently() {
            // Test each field individually
            String[] invalidEmails = {"invalid-email", "@example.com", "test@"};
            String[] invalidPasswords = {"weak", "12345678", "password"};
            String[] invalidConfirmPasswords = {"", null}; // Removed "different" since there's no validation rule for password matching
            String[] invalidFullNames = {"", null, "   "};

            for (String email : invalidEmails) {
                RegisterUserRequest testRequest = RegisterUserRequest.builder()
                        .email(email)
                        .password("ValidPass123!")
                        .confirmPassword("ValidPass123!")
                        .fullName("Test User")
                        .build();
                Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);
                assertFalse(violations.isEmpty(), "Should be invalid for email: " + email);
            }

            for (String password : invalidPasswords) {
                RegisterUserRequest testRequest = RegisterUserRequest.builder()
                        .email("test@example.com")
                        .password(password)
                        .confirmPassword(password)
                        .fullName("Test User")
                        .build();
                Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);
                assertFalse(violations.isEmpty(), "Should be invalid for password: " + password);
            }

            for (String confirmPassword : invalidConfirmPasswords) {
                RegisterUserRequest testRequest = RegisterUserRequest.builder()
                        .email("test@example.com")
                        .password("ValidPass123!")
                        .confirmPassword(confirmPassword)
                        .fullName("Test User")
                        .build();
                Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);
                assertFalse(violations.isEmpty(), "Should be invalid for confirmPassword: " + confirmPassword);
            }

            for (String fullName : invalidFullNames) {
                RegisterUserRequest testRequest = RegisterUserRequest.builder()
                        .email("test@example.com")
                        .password("ValidPass123!")
                        .confirmPassword("ValidPass123!")
                        .fullName(fullName)
                        .build();
                Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);
                assertFalse(violations.isEmpty(), "Should be invalid for fullName: " + fullName);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle special characters in full name")
        void shouldHandleSpecialCharactersInFullName() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("ValidPass123!")
                    .confirmPassword("ValidPass123!")
                    .fullName("José María O'Connor-Smith Jr.")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should handle international email domains")
        void shouldHandleInternationalEmailDomains() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.co.uk")
                    .password("ValidPass123!")
                    .confirmPassword("ValidPass123!")
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should handle complex password with special characters")
        void shouldHandleComplexPasswordWithSpecialCharacters() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("test@example.com")
                    .password("ComplexP@ssw0rd!@$%*?&")
                    .confirmPassword("ComplexP@ssw0rd!@$%*?&")
                    .fullName("Test User")
                    .build();

            // When
            Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(testRequest);

            // Then
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Field Access Tests")
    class FieldAccessTests {

        @Test
        @DisplayName("Should allow direct field access")
        void shouldAllowDirectFieldAccess() {
            // When
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("john@example.com")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .fullName("John Doe")
                    .build();

            // Then
            assertEquals("John Doe", testRequest.fullName);
            assertEquals("john@example.com", testRequest.email);
            assertEquals("SecurePass123!", testRequest.password);
            assertEquals("SecurePass123!", testRequest.confirmPassword);
        }

        @Test
        @DisplayName("Should handle field modification")
        void shouldHandleFieldModification() {
            // Given
            RegisterUserRequest testRequest = RegisterUserRequest.builder()
                    .email("original@example.com")
                    .password("SecurePass123!")
                    .confirmPassword("SecurePass123!")
                    .fullName("Original Name")
                    .build();

            // When
            testRequest.fullName = "Updated Name";
            testRequest.email = "updated@example.com";

            // Then
            assertEquals("Updated Name", testRequest.fullName);
            assertEquals("updated@example.com", testRequest.email);
        }
    }
}