package br.com.authentication.usecase.user.create.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {
    
    public Long id;
    public String email;
    public String fullName;
    public List<UserClaimResponse> claims;
    public String message;
}
