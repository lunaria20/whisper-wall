package com.sysintegg7.potane.whisperwall.features.restrictions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestrictionRequestResponse {
    private Long id;
    private UserResponse userToRestrict;
    private UserResponse requestedByModerator;
    private String reason;
    private Integer requestedDurationDays;
    private String status;
    private UserResponse reviewedByAdmin;
    private String adminResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
}
