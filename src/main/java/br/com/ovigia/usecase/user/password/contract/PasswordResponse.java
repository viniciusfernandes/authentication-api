package br.com.ovigia.usecase.user.password.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResponse {
    
    public String message;
}
