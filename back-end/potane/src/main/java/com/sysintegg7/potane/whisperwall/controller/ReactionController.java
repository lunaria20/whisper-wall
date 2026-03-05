package com.sysintegg7.potane.whisperwall.controller;

import com.sysintegg7.potane.whisperwall.dto.ReactionRequest;
import com.sysintegg7.potane.whisperwall.dto.ReactionResponse;
import com.sysintegg7.potane.whisperwall.service.ReactionService;
import com.sysintegg7.potane.whisperwall.util.SecurityUtil;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReactionController {
    @Autowired
    private ReactionService reactionService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/confession/{confessionId}")
    public ResponseEntity<ReactionResponse> addReaction(
            @PathVariable Long confessionId,
            @Valid @RequestBody ReactionRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ReactionResponse response = reactionService.addReaction(confessionId, userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/confession/{confessionId}")
    public ResponseEntity<List<ReactionResponse>> getConfessionReactions(@PathVariable Long confessionId) {
        List<ReactionResponse> response = reactionService.getConfessionReactions(confessionId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/confession/{confessionId}")
    public ResponseEntity<Void> removeReaction(@PathVariable Long confessionId) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reactionService.removeReaction(confessionId, userId);
        return ResponseEntity.noContent().build();
    }
}
