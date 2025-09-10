package br.com.ovigia.usecase.user.login.endpoint;

import br.com.ovigia.service.JwtService;
import br.com.ovigia.service.UserService;
import br.com.ovigia.usecase.user.login.contract.LoginRequest;
import br.com.ovigia.usecase.user.login.contract.LoginResponse;
import br.com.ovigia.usecase.user.login.mapping.LoginMapper;
import br.com.ovigia.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginEndpoint {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final LoginMapper loginMapper;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            // Get user details
            var user = (br.com.ovigia.domain.model.User) authentication.getPrincipal();
            
            // Generate JWT token
            String token = jwtService.generateToken(user);
            
            // Map to response
            var response = loginMapper.toResponse(user, token);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during login"));
        }
    }
}
