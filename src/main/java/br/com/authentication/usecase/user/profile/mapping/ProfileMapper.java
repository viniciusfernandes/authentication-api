package br.com.authentication.usecase.user.profile.mapping;

import br.com.authentication.domain.model.User;
import br.com.authentication.usecase.user.profile.contract.ProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
    
    public ProfileResponse toResponse(User user) {
        return ProfileResponse.builder()
                .id(user.id)
                .email(user.email)
                .fullName(user.fullName)
                .phone(user.phone)
                .profilePicture(user.profilePicture)
                .roles(user.roles)
                .emailVerified(user.emailVerified)
                .createdAt(user.createdAt)
                .build();
    }
}
