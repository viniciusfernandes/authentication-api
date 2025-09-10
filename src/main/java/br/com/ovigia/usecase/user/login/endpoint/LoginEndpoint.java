package br.com.ovigia.usecase.user.login.endpoint;

import br.com.ovigia.service.IJwtService;
import br.com.ovigia.service.IUserService;
import br.com.ovigia.usecase.user.login.contract.LoginRequest;
import br.com.ovigia.usecase.user.login.contract.LoginResponse;
import br.com.ovigia.usecase.user.login.mapping.LoginMapper;
import br.com.ovigia.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class LoginEndpoint {

    private final AuthenticationManager authenticationManager;
    private final IJwtService jwtService;
    private final IUserService userService;
    private final LoginMapper loginMapper;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email,
                            request.password
                    )
            );

            // Get user details
            var user = (br.com.ovigia.domain.model.User) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtService.generateToken(user);

            // Map to response
            var response = loginMapper.toResponse(user, token);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during login"));
        }
    }
}
