package br.com.ovigia.usecase.user.save.contract;

import br.com.ovigia.domain.model.User;
import br.com.ovigia.domain.model.UserStatus;
import br.com.ovigia.usecase.user.create.contract.RegisterUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegisterUserResponse Contract Tests")
class RegisterUserResponseTest {

    private RegisterUserResponse response;
    private User testUser;

    @BeforeEach
    void setUp() {
        response = RegisterUserResponse.builder().build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword123")
                .fullName("Test User")
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phone("+1234567890")
                .profilePicture("https://example.com/profile.jpg")
                .emailVerificationToken("verification-token-123")
                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .passwordResetToken("reset-token-456")
                .passwordResetTokenExpiry(LocalDateTime.now().plusHours(1))
                .externalProviderId("google_123456")
                .externalProvider(br.com.ovigia.domain.model.ExternalProvider.GOOGLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Response Creation Tests")
    class ResponseCreationTests {

        @Test
        @DisplayName("Should create response with all fields")
        void shouldCreateResponseWithAllFields() {
            // When
            response.id = testUser.id;
            response.email = testUser.email;
            response.fullName = testUser.fullName;
            response.message = "User registered successfully";

            // Then
            assertNotNull(response);
            assertEquals(testUser.id, response.id);
            assertEquals(testUser.email, response.email);
            assertEquals(testUser.fullName, response.fullName);
            assertEquals("User registered successfully", response.message);
        }

        @Test
        @DisplayName("Should create response with minimal data")
        void shouldCreateResponseWithMinimalData() {
            // When
            response.id = 1L;
            response.email = "minimal@example.com";
            response.fullName = "Minimal User";
            response.message = "Registration completed";

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id);
            assertEquals("minimal@example.com", response.email);
            assertEquals("Minimal User", response.fullName);
            assertEquals("Registration completed", response.message);
        }
    }

    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should handle all field types correctly")
        void shouldHandleAllFieldTypesCorrectly() {
            // When
            response.id = 123L;
            response.email = "test@example.com";
            response.fullName = "Test User";
            response.message = "Success message";

            // Then
            assertTrue(response.id instanceof Long);
            assertTrue(response.email instanceof String);
            assertTrue(response.fullName instanceof String);
            assertTrue(response.message instanceof String);
        }

        @Test
        @DisplayName("Should handle null values correctly")
        void shouldHandleNullValuesCorrectly() {
            // When
            response.id = null;
            response.email = null;
            response.fullName = null;
            response.message = null;

            // Then
            assertNull(response.id);
            assertNull(response.email);
            assertNull(response.fullName);
            assertNull(response.message);
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create response using builder pattern")
        void shouldCreateResponseUsingBuilderPattern() {
            // When
            response = RegisterUserResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .fullName("Test User")
                    .message("User registered successfully")
                    .build();

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id);
            assertEquals("test@example.com", response.email);
            assertEquals("Test User", response.fullName);
            assertEquals("User registered successfully", response.message);
        }

        @Test
        @DisplayName("Should create response with partial data using builder")
        void shouldCreateResponseWithPartialDataUsingBuilder() {
            // When
            response = RegisterUserResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .fullName("Test User")
                    .build();

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id);
            assertEquals("test@example.com", response.email);
            assertEquals("Test User", response.fullName);
            assertNull(response.message);
        }
    }

    @Nested
    @DisplayName("Data Type Tests")
    class DataTypeTests {

        @Test
        @DisplayName("Should handle Long ID correctly")
        void shouldHandleLongIdCorrectly() {
            // Given
            Long testId = 999L;

            // When
            response.id = testId;

            // Then
            assertEquals(testId, response.id);
            assertTrue(response.id instanceof Long);
        }

        @Test
        @DisplayName("Should handle String fields correctly")
        void shouldHandleStringFieldsCorrectly() {
            // Given
            String testEmail = "user@example.com";
            String testFullName = "John Doe";
            String testMessage = "Registration successful";

            // When
            response.email = testEmail;
            response.fullName = testFullName;
            response.message = testMessage;

            // Then
            assertEquals(testEmail, response.email);
            assertEquals(testFullName, response.fullName);
            assertEquals(testMessage, response.message);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longString = "A".repeat(1000);
            String longEmail = "verylongemailaddress" + "A".repeat(100) + "@example.com";
            String longMessage = "This is a very long message " + "A".repeat(500);

            // When
            response.email = longEmail;
            response.fullName = longString;
            response.message = longMessage;

            // Then
            assertEquals(longEmail, response.email);
            assertEquals(longString, response.fullName);
            assertEquals(longMessage, response.message);
        }

        @Test
        @DisplayName("Should handle special characters in strings")
        void shouldHandleSpecialCharactersInStrings() {
            // Given
            String specialEmail = "test+special@example.co.uk";
            String specialFullName = "José María O'Connor-Smith Jr.";
            String specialMessage = "Registration completed! Please check your email for verification. 🎉";

            // When
            response.email = specialEmail;
            response.fullName = specialFullName;
            response.message = specialMessage;

            // Then
            assertEquals(specialEmail, response.email);
            assertEquals(specialFullName, response.fullName);
            assertEquals(specialMessage, response.message);
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // When
            response.email = "";
            response.fullName = "";
            response.message = "";

            // Then
            assertEquals("", response.email);
            assertEquals("", response.fullName);
            assertEquals("", response.message);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response with no-args constructor")
        void shouldCreateResponseWithNoArgsConstructor() {
            // When
            RegisterUserResponse newResponse = new RegisterUserResponse();

            // Then
            assertNotNull(newResponse);
            assertNull(newResponse.id);
            assertNull(newResponse.email);
            assertNull(newResponse.fullName);
            assertNull(newResponse.message);
        }

        @Test
        @DisplayName("Should create response with all-args constructor")
        void shouldCreateResponseWithAllArgsConstructor() {
            // Given
            Long id = 1L;
            String email = "test@example.com";
            String fullName = "Test User";
            String message = "Success";

            // When
            RegisterUserResponse newResponse = new RegisterUserResponse(id, email, fullName, message);

            // Then
            assertNotNull(newResponse);
            assertEquals(id, newResponse.id);
            assertEquals(email, newResponse.email);
            assertEquals(fullName, newResponse.fullName);
            assertEquals(message, newResponse.message);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should allow field modification after creation")
        void shouldAllowFieldModificationAfterCreation() {
            // Given
            response = new RegisterUserResponse();
            response.id = 1L;
            response.email = "original@example.com";
            response.fullName = "Original User";
            response.message = "Original message";

            // When
            response.id = 2L;
            response.email = "updated@example.com";
            response.fullName = "Updated User";
            response.message = "Updated message";

            // Then
            assertEquals(2L, response.id);
            assertEquals("updated@example.com", response.email);
            assertEquals("Updated User", response.fullName);
            assertEquals("Updated message", response.message);
        }
    }
}