package br.com.authentication.usecase.user.password.endpoint;

import br.com.authentication.service.IEmailService;
import br.com.authentication.service.IUserService;
import br.com.authentication.usecase.user.password.contract.ForgotPasswordRequest;
import br.com.authentication.usecase.user.password.contract.PasswordResponse;
import br.com.authentication.usecase.user.password.contract.ResetPasswordRequest;
import br.com.authentication.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordEndpoint {
    
    private final IUserService userService;
    private final IEmailService emailService;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.initiatePasswordReset(request.email);
            
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
            userService.resetPassword(request.token, request.newPassword);
            
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
