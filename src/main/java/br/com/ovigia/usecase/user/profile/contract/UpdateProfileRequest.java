package br.com.ovigia.usecase.user.profile.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    public String fullName;
    
    public String phone;
    
    public String profilePicture;
}
