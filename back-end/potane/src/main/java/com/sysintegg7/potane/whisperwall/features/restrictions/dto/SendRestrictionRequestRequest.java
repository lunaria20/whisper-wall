package com.sysintegg7.potane.whisperwall.features.restrictions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendRestrictionRequestRequest {

    // Frontend sends { confessionId, reason, requestedDurationDays }
    private Long confessionId;
    private String reason;
    private Integer requestedDurationDays;
}