package com.sysintegg7.potane.whisperwall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfessionResponse {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String displayName;
    private String profilePicture;

    @JsonProperty("isApproved")
    private Boolean isApproved;

    @JsonProperty("isVisible")
    private Boolean isVisible;

    private String category;
    private String mood;
    private Integer reportCount;
    private Integer commentCount;
    private Integer reactionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
