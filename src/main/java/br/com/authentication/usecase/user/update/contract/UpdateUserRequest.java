package br.com.authentication.usecase.user.update.contract;

import br.com.authentication.usecase.user.create.contract.UserClaimRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    public String fullName;

    public String phone;

    public String profilePicture;

    // null keeps the user's current claims; a list (even empty) replaces them.
    @Valid
    public List<UserClaimRequest> claims;
}
