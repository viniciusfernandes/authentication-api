package br.com.authentication.usecase.user.create.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Claim wanted for the user: the id of a registered claim and the value it must have for this user.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClaimRequest {

    @NotNull(message = "Claim id is required")
    public Long claimId;

    @NotBlank(message = "Claim value is required")
    public String value;
}
