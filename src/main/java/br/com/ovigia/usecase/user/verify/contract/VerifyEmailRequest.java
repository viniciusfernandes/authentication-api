package br.com.ovigia.usecase.user.verify.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
}
