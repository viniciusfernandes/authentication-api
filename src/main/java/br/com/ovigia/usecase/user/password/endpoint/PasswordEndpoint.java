package br.com.ovigia.usecase.user.password.endpoint;

import br.com.ovigia.service.EmailService;
import br.com.ovigia.service.UserService;
import br.com.ovigia.usecase.user.password.contract.ForgotPasswordRequest;
import br.com.ovigia.usecase.user.password.contract.PasswordResponse;
import br.com.ovigia.usecase.user.password.contract.ResetPasswordRequest;
import br.com.ovigia.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordEndpoint {
    
    private final UserService userService;
    private final EmailService emailService;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.initiatePasswordReset(request.getEmail());
            
            var response = PasswordResponse.builder()
                    .message("Password reset email sent successfully")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            // Always return success to prevent email enumeration
            var response = PasswordResponse.builder()
                    .message("If the email exists, a password reset link has been sent")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<PasswordResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            
            var response = PasswordResponse.builder()
                    .message("Password reset successfully")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during password reset"));
        }
    }
}
