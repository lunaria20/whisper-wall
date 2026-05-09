package com.sysintegg7.potane.whisperwall.features.users.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    private String profilePicture;
}
