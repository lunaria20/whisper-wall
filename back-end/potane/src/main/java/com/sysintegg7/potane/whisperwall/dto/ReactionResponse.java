package com.sysintegg7.potane.whisperwall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponse {
    private Long id;
    private String reactionType;
    private Long confessionId;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
}
