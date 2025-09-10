package br.com.ovigia.usecase.user.profile.endpoint;

import br.com.ovigia.service.UserService;
import br.com.ovigia.usecase.user.profile.contract.ProfileResponse;
import br.com.ovigia.usecase.user.profile.contract.UpdateProfileRequest;
import br.com.ovigia.usecase.user.profile.mapping.ProfileMapper;
import br.com.ovigia.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ProfileEndpoint {
    
    private final UserService userService;
    private final ProfileMapper profileMapper;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(Authentication authentication) {
        try {
            var user = (br.com.ovigia.domain.model.User) authentication.getPrincipal();
            var response = profileMapper.toResponse(user);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while fetching profile"));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        try {
            var user = (br.com.ovigia.domain.model.User) authentication.getPrincipal();
            
            var updatedUser = userService.updateProfile(
                    user,
                    request.getFullName(),
                    request.getPhone(),
                    request.getProfilePicture()
            );
            
            var response = profileMapper.toResponse(updatedUser);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while updating profile"));
        }
    }
}
