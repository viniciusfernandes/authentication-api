package br.com.ovigia.usecase.user.create.mapping;

import br.com.ovigia.domain.model.User;
import br.com.ovigia.usecase.user.create.contract.RegisterUserRequest;
import br.com.ovigia.usecase.user.create.contract.RegisterUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public User toDomain(RegisterUserRequest request) {
        User user = new User();
        user.email = request.email;
        user.password = request.password;
        user.fullName = request.fullName;
        return user;
    }
    
    public RegisterUserResponse toResponse(User user) {
        RegisterUserResponse response = new RegisterUserResponse();
        response.id = user.id;
        response.email = user.email;
        response.fullName = user.fullName;
        response.message = "User registered successfully. Please check your email for verification.";
        return response;
    }
}
