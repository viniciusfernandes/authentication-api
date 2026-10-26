package br.com.authentication.usecase.user.create.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClaimResponse {

    public Long claimId;
    public String name;
    public String value;
}
