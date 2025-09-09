package br.com.ovigia.usecase.user.profile.contract;

import br.com.ovigia.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String profilePicture;
    private Set<Role> roles;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
