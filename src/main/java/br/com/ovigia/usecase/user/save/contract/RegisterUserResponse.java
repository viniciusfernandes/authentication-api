package br.com.ovigia.usecase.user.save.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {
    
    private Long id;
    private String email;
    private String fullName;
    private String message;
}
