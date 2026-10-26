package br.com.authentication.usecase.user.update.endpoint;

import br.com.authentication.service.IUserService;
import br.com.authentication.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import br.com.authentication.usecase.user.create.mapping.UserMapper;
import br.com.authentication.usecase.user.update.contract.UpdateUserRequest;
import br.com.authentication.usecase.user.update.contract.UpdateUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UpdateUserEndpoint {

    private final IUserService userService;
    private final UserMapper userMapper;

    @PutMapping("/register/{userId}")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {

        var user = userService.updateUser(
                userId,
                request.fullName,
                request.phone,
                request.profilePicture,
                userMapper.toClaimAssignments(request.claims)
        );

        return ResponseEntity.ok(ApiResponse.success(userMapper.toUpdateResponse(user)));
    }
}
