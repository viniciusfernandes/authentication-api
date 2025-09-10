package br.com.ovigia.usecase.user.oauth.endpoint;

import br.com.ovigia.service.JwtService;
import br.com.ovigia.service.UserService;
import br.com.ovigia.usecase.user.login.contract.LoginResponse;
import br.com.ovigia.usecase.user.login.mapping.LoginMapper;
import br.com.ovigia.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Endpoint {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final LoginMapper loginMapper;
    
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<LoginResponse>> oauth2Success(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String providerId = oauth2User.getAttribute("id");
            
            // Determine provider
            br.com.ovigia.domain.model.ExternalProvider provider = 
                    determineProvider(oauth2User);
            
            // Check if user exists
            var existingUser = userService.findByExternalProvider(providerId, provider);
            
            br.com.ovigia.domain.model.User user;
            if (existingUser != null) {
                user = existingUser;
            } else {
                // Create new user from OAuth2 data
                user = userService.createUserFromExternalProvider(
                        email, name, providerId, provider);
            }
            
            // Generate JWT token
            String token = jwtService.generateToken(user);
            
            // Map to response
            var response = loginMapper.toResponse(user, token);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("OAuth2 authentication failed"));
        }
    }
    
    @GetMapping("/failure")
    public ResponseEntity<ApiResponse<String>> oauth2Failure() {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("OAuth2 authentication failed"));
    }
    
    private br.com.ovigia.domain.model.ExternalProvider determineProvider(OAuth2User oauth2User) {
        // This is a simplified approach. In a real application, you might need
        // to check the registration ID or other attributes to determine the provider
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        if (attributes.containsKey("sub")) {
            return br.com.ovigia.domain.model.ExternalProvider.GOOGLE;
        } else if (attributes.containsKey("id")) {
            return br.com.ovigia.domain.model.ExternalProvider.FACEBOOK;
        }
        
        throw new IllegalArgumentException("Unknown OAuth2 provider");
    }
}
