package br.com.authentication.usecase.user.verify.contract;

import jakarta.validation.constraints.NotBlank;

public class VerifyEmailRequest {
    
    @NotBlank(message = "Token is required")
    public String token;
}
