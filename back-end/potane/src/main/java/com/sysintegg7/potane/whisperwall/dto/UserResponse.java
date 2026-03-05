package com.sysintegg7.potane.whisperwall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String profilePicture;
    private String bio;

    @JsonProperty("isVerified")
    private Boolean isVerified;

    @JsonProperty("isActive")
    private Boolean isActive;

    private Integer reportCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
