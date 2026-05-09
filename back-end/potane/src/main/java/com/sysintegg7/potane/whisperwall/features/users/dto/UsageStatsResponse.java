package com.sysintegg7.potane.whisperwall.features.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageStatsResponse {
    private Long totalUsers;
    private Long totalPosts;
    private Long totalComments;
    private Long totalReports;
    private Long activeUsers; // Users who posted in last 30 days
    private Long restrictedUsers;
    private Long adminUsers;
    private Long moderatorUsers;
}
