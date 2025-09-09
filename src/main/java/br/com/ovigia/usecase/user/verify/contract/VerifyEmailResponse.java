package br.com.ovigia.usecase.user.verify.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailResponse {
    
    private String message;
    private boolean verified;
}
