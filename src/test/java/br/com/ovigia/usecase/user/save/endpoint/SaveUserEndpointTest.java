package br.com.ovigia.usecase.user.save.endpoint;

import br.com.ovigia.config.TestSecurityConfig;
import br.com.ovigia.domain.model.User;
import br.com.ovigia.domain.model.UserStatus;
import br.com.ovigia.service.IEmailService;
import br.com.ovigia.service.IJwtService;
import br.com.ovigia.service.IUserService;
import br.com.ovigia.usecase.user.create.contract.RegisterUserRequest;
import br.com.ovigia.usecase.user.create.contract.RegisterUserResponse;
import br.com.ovigia.usecase.user.create.endpoint.SaveUserEndpoint;
import br.com.ovigia.usecase.user.create.mapping.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaveUserEndpoint.class)
@Import(TestSecurityConfig.class)
@DisplayName("SaveUserEndpoint Tests")
class SaveUserEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    @MockBean
    private IEmailService emailService;

    @MockBean
    private IJwtService jwtService;

    @MockBean
    private UserMapper userMapper;

    private RegisterUserRequest validRequest;
    private User mockUser;
    private RegisterUserResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = RegisterUserRequest.builder()
                .email("test@example.com")
                .password("ValidPass123!")
                .confirmPassword("ValidPass123!")
                .fullName("Test User")
                .build();

        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword123")
                .fullName("Test User")
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .emailVerificationToken("verification-token-123")
                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();

        mockResponse = RegisterUserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .message("User registered successfully. Please check your email for verification.")
                .build();
    }

    @Nested
    @DisplayName("Successful Registration Tests")
    class SuccessfulRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfullyWithValidData() throws Exception {
            // Given
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.fullName").value("Test User"))
                    .andExpect(jsonPath("$.data.message").value("User registered successfully. Please check your email for verification."))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(userService).createUser("test@example.com", "ValidPass123!", "Test User");
        }

        @Test
        @DisplayName("Should return 201 status code for successful registration")
        void shouldReturn201StatusCodeForSuccessfulRegistration() throws Exception {
            // Given
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400WhenEmailIsMissing() throws Exception {
            // Given
            validRequest.email = null;

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when email is empty")
        void shouldReturn400WhenEmailIsEmpty() throws Exception {
            // Given
            validRequest.email = "";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void shouldReturn400WhenEmailFormatIsInvalid() throws Exception {
            // Given
            validRequest.email = "invalid-email";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password is missing")
        void shouldReturn400WhenPasswordIsMissing() throws Exception {
            // Given
            validRequest.password = null;

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password is too short")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            // Given
            validRequest.password = "Short1!";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password does not contain uppercase letter")
        void shouldReturn400WhenPasswordDoesNotContainUppercaseLetter() throws Exception {
            // Given
            validRequest.password = "validpass123!";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password does not contain number")
        void shouldReturn400WhenPasswordDoesNotContainNumber() throws Exception {
            // Given
            validRequest.password = "ValidPass!";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password does not contain special character")
        void shouldReturn400WhenPasswordDoesNotContainSpecialCharacter() throws Exception {
            // Given
            validRequest.password = "ValidPass123";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when confirm password does not match")
        void shouldReturn400WhenConfirmPasswordDoesNotMatch() throws Exception {
            // Given
            validRequest.confirmPassword = "DifferentPass123!";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when full name is missing")
        void shouldReturn400WhenFullNameIsMissing() throws Exception {
            // Given
            validRequest.fullName = null;

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when full name is empty")
        void shouldReturn400WhenFullNameIsEmpty() throws Exception {
            // Given
            validRequest.fullName = "";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Business Logic Error Tests")
    class BusinessLogicErrorTests {

        @Test
        @DisplayName("Should return 409 when user already exists")
        void shouldReturn409WhenUserAlreadyExists() throws Exception {
            // Given
            when(userService.createUser(anyString(), anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("User already exists"));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("User already exists"))
                    .andExpect(jsonPath("$.data").doesNotExist());

            verify(userService).createUser("test@example.com", "ValidPass123!", "Test User");
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void shouldReturn500WhenServiceThrowsUnexpectedException() throws Exception {
            // Given
            when(userService.createUser(anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Database connection failed"))
                    .andExpect(jsonPath("$.data").doesNotExist());

            verify(userService).createUser("test@example.com", "ValidPass123!", "Test User");
        }
    }

    @Nested
    @DisplayName("Request Content Type Tests")
    class RequestContentTypeTests {

        @Test
        @DisplayName("Should return 415 when content type is not JSON")
        void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .content("email=test@example.com&password=ValidPass123!&confirmPassword=ValidPass123!&fullName=Test User"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed JSON")
        void shouldReturn400WhenRequestBodyIsMalformedJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Response Structure Tests")
    class ResponseStructureTests {

        @Test
        @DisplayName("Should return response in correct format")
        void shouldReturnResponseInCorrectFormat() throws Exception {
            // Given
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.success").isBoolean())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Should not expose sensitive information in response")
        void shouldNotExposeSensitiveInformationInResponse() throws Exception {
            // Given
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.password").doesNotExist())
                    .andExpect(jsonPath("$.data.emailVerificationToken").doesNotExist())
                    .andExpect(jsonPath("$.data.passwordResetToken").doesNotExist())
                    .andExpect(jsonPath("$.data.message").exists());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long full name")
        void shouldHandleVeryLongFullName() throws Exception {
            // Given
            validRequest.fullName = "A".repeat(100); // Very long name (max allowed)
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());

            verify(userService).createUser("test@example.com", "ValidPass123!", validRequest.fullName);
        }

        @Test
        @DisplayName("Should handle special characters in full name")
        void shouldHandleSpecialCharactersInFullName() throws Exception {
            // Given
            validRequest.fullName = "José María O'Connor-Smith";
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());

            verify(userService).createUser("test@example.com", "ValidPass123!", "José María O'Connor-Smith");
        }

        @Test
        @DisplayName("Should handle international email domains")
        void shouldHandleInternationalEmailDomains() throws Exception {
            // Given
            validRequest.email = "test@example.co.uk";
            when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());

            verify(userService).createUser("test@example.co.uk", "ValidPass123!", "Test User");
        }
    }
}
