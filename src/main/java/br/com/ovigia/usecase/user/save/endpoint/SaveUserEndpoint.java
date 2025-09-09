package br.com.ovigia.usecase.user.save.endpoint;

import br.com.ovigia.usecase.user.save.contract.RegisterUserRequest;
import br.com.ovigia.usecase.user.save.contract.RegisterUserResponse;
import br.com.ovigia.usecase.user.save.mapping.UserMapper;
import br.com.ovigia.service.UserService;
import br.com.ovigia.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SaveUserEndpoint {
    
    private final UserService userService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUserResponse>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Passwords do not match"));
        }
        
        try {
            // Create user
            var user = userService.createUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName()
            );
            
            // Send verification email
            emailService.sendEmailVerification(user);
            
            // Map to response
            var response = userMapper.toResponse(user);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during registration"));
        }
    }
    
    // Inner class for API response structure
    public static class ApiResponse<T> {
        private T data;
        private String error;
        private boolean success;
        
        private ApiResponse(T data, String error, boolean success) {
            this.data = data;
            this.error = error;
            this.success = success;
        }
        
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(data, null, true);
        }
        
        public static <T> ApiResponse<T> error(String error) {
            return new ApiResponse<>(null, error, false);
        }
        
        // Getters
        public T getData() { return data; }
        public String getError() { return error; }
        public boolean isSuccess() { return success; }
    }
}
