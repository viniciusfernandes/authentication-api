package br.com.ovigia.usecase.user.verify.endpoint;

import br.com.ovigia.service.UserService;
import br.com.ovigia.usecase.user.verify.contract.VerifyEmailRequest;
import br.com.ovigia.usecase.user.verify.contract.VerifyEmailResponse;
import br.com.ovigia.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerifyEmailEndpoint {
    
    private final UserService userService;
    
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            userService.verifyEmail(request.getToken());
            
            var response = VerifyEmailResponse.builder()
                    .message("Email verified successfully")
                    .verified(true)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during email verification"));
        }
    }
}
