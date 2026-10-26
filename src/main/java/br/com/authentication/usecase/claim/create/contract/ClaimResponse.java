package br.com.authentication.usecase.claim.create.contract;

import br.com.authentication.domain.model.ClaimValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    public Long id;
    public String name;
    public String description;
    public ClaimValueType valueType;
}
