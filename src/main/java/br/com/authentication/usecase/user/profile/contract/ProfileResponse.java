package br.com.authentication.usecase.user.profile.contract;

import br.com.authentication.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    
    public Long id;
    public String email;
    public String fullName;
    public String phone;
    public String profilePicture;
    public Set<Role> roles;
    public boolean emailVerified;
    public LocalDateTime createdAt;
}
