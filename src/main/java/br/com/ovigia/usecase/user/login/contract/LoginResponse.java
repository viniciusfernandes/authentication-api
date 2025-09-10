package br.com.ovigia.usecase.user.login.contract;

import br.com.ovigia.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    public String token;
    @Builder.Default
    public String type = "Bearer";
    public Long id;
    public String email;
    public String fullName;
    public Set<Role> roles;
}
