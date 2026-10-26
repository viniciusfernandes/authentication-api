package br.com.authentication.usecase.user.update.contract;

import br.com.authentication.usecase.user.create.contract.UserClaimResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserResponse {

    public Long id;
    public String email;
    public String fullName;
    public String phone;
    public String profilePicture;
    public List<UserClaimResponse> claims;
}
