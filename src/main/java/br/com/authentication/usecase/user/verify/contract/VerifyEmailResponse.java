package br.com.authentication.usecase.user.verify.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailResponse {
    
    public String message;
    public boolean verified;
}
