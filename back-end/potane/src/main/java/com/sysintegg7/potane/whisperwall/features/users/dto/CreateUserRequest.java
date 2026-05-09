package com.sysintegg7.potane.whisperwall.features.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String displayName;
    private String roleName; // ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR
}
