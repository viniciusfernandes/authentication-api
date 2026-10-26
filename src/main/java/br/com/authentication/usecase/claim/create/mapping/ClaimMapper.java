package br.com.authentication.usecase.claim.create.mapping;

import br.com.authentication.domain.model.Claim;
import br.com.authentication.usecase.claim.create.contract.ClaimResponse;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {

    public ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.id)
                .name(claim.name)
                .description(claim.description)
                .valueType(claim.valueType)
                .build();
    }
}
