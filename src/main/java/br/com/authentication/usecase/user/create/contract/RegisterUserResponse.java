package br.com.authentication.usecase.user.create.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {
    
    public Long id;
    public String email;
    public String fullName;
    public String message;
}
