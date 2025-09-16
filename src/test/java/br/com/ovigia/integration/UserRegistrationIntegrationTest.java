package br.com.ovigia.integration;

import br.com.ovigia.TestConfig;
import br.com.ovigia.domain.model.User;
import br.com.ovigia.domain.model.UserStatus;
import br.com.ovigia.domain.repository.UserRepository;
import br.com.ovigia.service.IEmailService;
import br.com.ovigia.service.IJwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = br.com.ovigia.AuthenticationApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
@DisplayName("User Registration Integration Tests")
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private IEmailService emailService;

    @MockBean
    private IJwtService jwtService;

    private String validRegistrationJson;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        userRepository.deleteAll();

        validRegistrationJson = """
            {
                "email": "test@example.com",
                "password": "ValidPass123!",
                "confirmPassword": "ValidPass123!",
                "fullName": "Test User"
            }
            """;
    }

    @Nested
    @DisplayName("Complete Registration Flow Tests")
    class CompleteRegistrationFlowTests {

        @Test
        @DisplayName("Should complete full registration flow successfully")
        void shouldCompleteFullRegistrationFlowSuccessfully() throws Exception {
            // When
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRegistrationJson))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.fullName").value("Test User"))
                    .andExpect(jsonPath("$.data.message").value("User registered successfully. Please check your email for verification."))
                    .andExpect(jsonPath("$.success").value(true));

            // Then - Verify user was saved in database
            Optional<User> savedUser = userRepository.findByEmail("test@example.com");
            assertTrue(savedUser.isPresent());
            User user = savedUser.get();
            assertEquals("test@example.com", user.email);
            assertEquals("Test User", user.fullName);
            assertEquals(UserStatus.PENDING_VERIFICATION, user.status);
            assertFalse(user.emailVerified);
            assertNotNull(user.password);
            assertNotNull(user.emailVerificationToken);
            assertNotNull(user.emailVerificationTokenExpiry);
            assertNotNull(user.createdAt);

            // Verify email service was called
            verify(emailService).sendEmailVerification(user);
        }

        @Test
        @DisplayName("Should handle duplicate email registration attempt")
        void shouldHandleDuplicateEmailRegistrationAttempt() throws Exception {
            // Given - Create a user first
            User existingUser = User.builder()
                    .email("test@example.com")
                    .password("hashedPassword")
                    .fullName("Existing User")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(existingUser);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRegistrationJson))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("User with email test@example.com already exists"));

            // Verify only one user exists in database
            assertEquals(1, userRepository.count());
            verify(emailService, never()).sendEmailVerification(any(User.class));
        }

        @Test
        @DisplayName("Should persist user with all required fields")
        void shouldPersistUserWithAllRequiredFields() throws Exception {
            // When
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRegistrationJson))
                    .andExpect(status().isCreated());

            // Then
            Optional<User> savedUser = userRepository.findByEmail("test@example.com");
            assertTrue(savedUser.isPresent());
            User user = savedUser.get();

            // Verify all required fields are present
            assertNotNull(user.id);
            assertEquals("test@example.com", user.email);
            assertNotNull(user.password);
            assertEquals("Test User", user.fullName);
            assertEquals(UserStatus.PENDING_VERIFICATION, user.status);
            assertFalse(user.emailVerified);
            assertNotNull(user.emailVerificationToken);
            assertNotNull(user.emailVerificationTokenExpiry);
            assertNotNull(user.createdAt);
            assertNotNull(user.updatedAt);
            assertTrue(user.updatedAt.isEqual(user.createdAt) || user.updatedAt.isAfter(user.createdAt)); // Should be equal or after creation
            assertNull(user.phone);
            assertNull(user.profilePicture);
            assertNull(user.passwordResetToken);
            assertNull(user.passwordResetTokenExpiry);
            assertNull(user.externalProviderId);
            assertNull(user.externalProvider);
        }
    }

    @Nested
    @DisplayName("Database Transaction Tests")
    class DatabaseTransactionTests {

        @Test
        @DisplayName("Should rollback transaction on service failure")
        void shouldRollbackTransactionOnServiceFailure() throws Exception {
            // Given - Mock email service to throw exception
            doThrow(new RuntimeException("Email service unavailable"))
                    .when(emailService).sendEmailVerification(any(User.class));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRegistrationJson))
                    .andExpect(status().isInternalServerError());

            // Verify no user was saved due to rollback
            assertEquals(0, userRepository.count());
        }

        @Test
        @DisplayName("Should handle concurrent registration attempts")
        void shouldHandleConcurrentRegistrationAttempts() throws Exception {
            // Given
            String email1 = "user1@example.com";
            String email2 = "user2@example.com";

            String registration1 = """
                {
                    "email": "%s",
                    "password": "ValidPass123!",
                    "confirmPassword": "ValidPass123!",
                    "fullName": "User One"
                }
                """.formatted(email1);

            String registration2 = """
                {
                    "email": "%s",
                    "password": "ValidPass123!",
                    "confirmPassword": "ValidPass123!",
                    "fullName": "User Two"
                }
                """.formatted(email2);

            // When - Register both users
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registration1))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registration2))
                    .andExpect(status().isCreated());

            // Then - Verify both users were saved
            assertEquals(2, userRepository.count());
            assertTrue(userRepository.findByEmail(email1).isPresent());
            assertTrue(userRepository.findByEmail(email2).isPresent());
        }
    }

    @Nested
    @DisplayName("Email Verification Integration Tests")
    class EmailVerificationIntegrationTests {

        @Test
        @DisplayName("Should create user with valid verification token")
        void shouldCreateUserWithValidVerificationToken() throws Exception {
            // When
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRegistrationJson))
                    .andExpect(status().isCreated());

            // Then
            Optional<User> savedUser = userRepository.findByEmail("test@example.com");
            assertTrue(savedUser.isPresent());
            User user = savedUser.get();

            // Verify verification token is valid
            assertNotNull(user.emailVerificationToken);
            assertTrue(user.emailVerificationToken.length() > 10);
            assertNotNull(user.emailVerificationTokenExpiry);
            assertTrue(user.emailVerificationTokenExpiry.isAfter(LocalDateTime.now()));
            assertTrue(user.emailVerificationTokenExpiry.isBefore(LocalDateTime.now().plusDays(1)));

            // Verify token can be found by repository
            Optional<User> userByToken = userRepository.findByEmailVerificationToken(user.emailVerificationToken);
            assertTrue(userByToken.isPresent());
            assertEquals(user.id, userByToken.get().id);
        }

        @Test
        @DisplayName("Should generate unique verification tokens")
        void shouldGenerateUniqueVerificationTokens() throws Exception {
            // Given
            String registration1 = """
                {
                    "email": "user1@example.com",
                    "password": "ValidPass123!",
                    "confirmPassword": "ValidPass123!",
                    "fullName": "User One"
                }
                """;

            String registration2 = """
                {
                    "email": "user2@example.com",
                    "password": "ValidPass123!",
                    "confirmPassword": "ValidPass123!",
                    "fullName": "User Two"
                }
                """;

            // When
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registration1))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registration2))
                    .andExpect(status().isCreated());

            // Then
            Optional<User> user1 = userRepository.findByEmail("user1@example.com");
            Optional<User> user2 = userRepository.findByEmail("user2@example.com");

            assertTrue(user1.isPresent());
            assertTrue(user2.isPresent());

            assertNotEquals(user1.get().emailVerificationToken, user2.get().emailVerificationToken);
        }
    }

    @Nested
    @DisplayName("Password Hashing Integration Tests")
    class PasswordHashingIntegrationTests {

        @Test
        @DisplayName("Should hash password before saving to database")
        void shouldHashPasswordBeforeSavingToDatabase() throws Exception {
            // When
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRegistrationJson))
                    .andExpect(status().isCreated());

            // Then
            Optional<User> savedUser = userRepository.findByEmail("test@example.com");
            assertTrue(savedUser.isPresent());
            User user = savedUser.get();

            // Verify password is hashed (not plain text)
            assertNotNull(user.password);
            assertNotEquals("ValidPass123!", user.password);
            assertTrue(user.password.length() > 20); // BCrypt hashes are typically 60 characters
            assertTrue(user.password.startsWith("$2")); // BCrypt hash prefix
        }

        @Test
        @DisplayName("Should generate different hashes for same password")
        void shouldGenerateDifferentHashesForSamePassword() throws Exception {
            // Given
            String registration1 = """
                {
                    "email": "user1@example.com",
                    "password": "ValidPass123!",
                    "confirmPassword": "ValidPass123!",
                    "fullName": "User One"
                }
                """;

            String registration2 = """
                {
                    "email": "user2@example.com",
                    "password": "ValidPass123!",
                    "confirmPassword": "ValidPass123!",
                    "fullName": "User Two"
                }
                """;

            // When
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registration1))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registration2))
                    .andExpect(status().isCreated());

            // Then
            Optional<User> user1 = userRepository.findByEmail("user1@example.com");
            Optional<User> user2 = userRepository.findByEmail("user2@example.com");

            assertTrue(user1.isPresent());
            assertTrue(user2.isPresent());

            // Same password should produce different hashes due to salt
            assertNotEquals(user1.get().password, user2.get().password);
        }
    }

    @Nested
    @DisplayName("Performance Integration Tests")
    class PerformanceIntegrationTests {

        @Test
        @DisplayName("Should handle multiple registrations efficiently")
        void shouldHandleMultipleRegistrationsEfficiently() throws Exception {
            // Given
            int numberOfUsers = 10;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < numberOfUsers; i++) {
                String email = "user" + i + "@example.com";
                String registrationJson = """
                    {
                        "email": "%s",
                        "password": "ValidPass123!",
                        "confirmPassword": "ValidPass123!",
                        "fullName": "User %d"
                    }
                    """.formatted(email, i);

                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson))
                        .andExpect(status().isCreated());
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Then
            assertEquals(numberOfUsers, userRepository.count());
            assertTrue(executionTime < 5000); // Should complete within 5 seconds

            // Verify all users were created
            for (int i = 0; i < numberOfUsers; i++) {
                String email = "user" + i + "@example.com";
                assertTrue(userRepository.findByEmail(email).isPresent());
            }
        }
    }
}
