package br.com.authentication.usecase.user.profile.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    
    public Long id;
    public String email;
    public String fullName;
    public String phone;
    public String profilePicture;
    public boolean emailVerified;
    public LocalDateTime createdAt;
}
