package com.sysintegg7.potane.whisperwall.features.restrictions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictUserRequest {
    private String reason;
    private Integer durationDays; // Duration in days
}
