package br.com.authentication.usecase.user.create.mapping;

import br.com.authentication.domain.model.User;
import br.com.authentication.service.ClaimAssignment;
import br.com.authentication.usecase.user.create.contract.RegisterUserRequest;
import br.com.authentication.usecase.user.create.contract.RegisterUserResponse;
import br.com.authentication.usecase.user.create.contract.UserClaimRequest;
import br.com.authentication.usecase.user.create.contract.UserClaimResponse;
import br.com.authentication.usecase.user.update.contract.UpdateUserResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class UserMapper {
    
    public User toDomain(RegisterUserRequest request) {
        User user = new User();
        user.email = request.email;
        user.password = request.password;
        user.fullName = request.fullName;
        return user;
    }

    /** Returns null when the request carries no claims list (null is meaningful on update). */
    public List<ClaimAssignment> toClaimAssignments(List<UserClaimRequest> claims) {
        if (claims == null) {
            return null;
        }
        return claims.stream()
                .map(claim -> new ClaimAssignment(claim.claimId, claim.value))
                .toList();
    }

    public List<UserClaimResponse> toClaimResponses(User user) {
        if (user.claims == null) {
            return List.of();
        }
        return user.claims.stream()
                .map(userClaim -> UserClaimResponse.builder()
                        .claimId(userClaim.claim.id)
                        .name(userClaim.claim.name)
                        .value(userClaim.value)
                        .build())
                .sorted(Comparator.comparing(response -> response.claimId))
                .toList();
    }
    
    public RegisterUserResponse toResponse(User user) {
        RegisterUserResponse response = new RegisterUserResponse();
        response.id = user.id;
        response.email = user.email;
        response.fullName = user.fullName;
        response.claims = toClaimResponses(user);
        response.message = "User registered successfully. Please check your email for verification.";
        return response;
    }

    public UpdateUserResponse toUpdateResponse(User user) {
        return UpdateUserResponse.builder()
                .id(user.id)
                .email(user.email)
                .fullName(user.fullName)
                .phone(user.phone)
                .profilePicture(user.profilePicture)
                .claims(toClaimResponses(user))
                .build();
    }
}
