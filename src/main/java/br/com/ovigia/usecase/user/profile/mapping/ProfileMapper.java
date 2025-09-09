package br.com.ovigia.usecase.user.profile.mapping;

import br.com.ovigia.domain.model.User;
import br.com.ovigia.usecase.user.profile.contract.ProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
    
    public ProfileResponse toResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .profilePicture(user.getProfilePicture())
                .roles(user.getRoles())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
