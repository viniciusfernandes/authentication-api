package br.com.ovigia.usecase.user.create.mapping;

import br.com.ovigia.domain.model.User;
import br.com.ovigia.usecase.user.create.contract.RegisterUserRequest;
import br.com.ovigia.usecase.user.create.contract.RegisterUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public User toDomain(RegisterUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .build();
    }
    
    public RegisterUserResponse toResponse(User user) {
        return RegisterUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("User registered successfully. Please check your email for verification.")
                .build();
    }
}
