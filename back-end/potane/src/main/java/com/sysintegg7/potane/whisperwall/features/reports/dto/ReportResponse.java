package com.sysintegg7.potane.whisperwall.features.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String reason;
    private String description;
    private Long confessionId;
    private String confessionContent;
    private String confessionCategory;
    
    private String confessionOwnerUsername;
    private Long reportedByUserId;
    private String reportedByUsername;
    private String status;
    private LocalDateTime createdAt;
}