package br.com.authentication.usecase.claim.create.contract;

import br.com.authentication.domain.model.ClaimValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClaimRequest {

    @NotBlank(message = "Claim name is required")
    @Size(max = 100, message = "Claim name must have at most 100 characters")
    public String name;

    @Size(max = 255, message = "Description must have at most 255 characters")
    public String description;

    // STRING (default), INTEGER or BOOLEAN
    public ClaimValueType valueType;
}
