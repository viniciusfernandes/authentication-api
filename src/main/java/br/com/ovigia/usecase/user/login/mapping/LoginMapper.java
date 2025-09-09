package br.com.ovigia.usecase.user.login.mapping;

import br.com.ovigia.domain.model.User;
import br.com.ovigia.usecase.user.login.contract.LoginResponse;
import org.springframework.stereotype.Component;

@Component
public class LoginMapper {
    
    public LoginResponse toResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .build();
    }
}
