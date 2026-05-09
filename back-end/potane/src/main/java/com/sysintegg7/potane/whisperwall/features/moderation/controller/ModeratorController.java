package com.sysintegg7.potane.whisperwall.features.moderation.controller;

import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionResponse;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.SendRestrictionRequestRequest;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictionRequestResponse;
import com.sysintegg7.potane.whisperwall.features.moderation.service.ModeratorService;
import com.sysintegg7.potane.whisperwall.shared.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/moderator")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('MODERATOR')")
public class ModeratorController {
    @Autowired
    private ModeratorService moderatorService;

    @Autowired
    private SecurityUtil securityUtil;

    // ===== POST VIEWING (Moderator can view but not modify directly) =====
    @GetMapping("/posts")
    public ResponseEntity<Page<ConfessionResponse>> viewAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConfessionResponse> posts = moderatorService.viewAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ConfessionResponse> viewPost(@PathVariable Long postId) {
        ConfessionResponse post = moderatorService.viewPost(postId);
        return ResponseEntity.ok(post);
    }

    // ===== POST & COMMENT DELETION (Moderator can delete) =====
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        moderatorService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        moderatorService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // ===== RESTRICTION REQUEST (Moderator sends request to Admin) =====
    @PostMapping("/restriction-requests")
    public ResponseEntity<RestrictionRequestResponse> sendRestrictionRequest(
            @RequestBody SendRestrictionRequestRequest request) {
        log.info("[MODERATOR] POST /restriction-requests called with request: {}", request);
        Long moderatorId = securityUtil.getCurrentUserId();
        log.info("[MODERATOR] moderatorId={}", moderatorId);
        RestrictionRequestResponse response = moderatorService.sendRestrictionRequest(request, moderatorId);
        log.info("[MODERATOR] Restriction request created: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/restriction-requests")
    public ResponseEntity<Page<RestrictionRequestResponse>> getMyRestrictionRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long moderatorId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<RestrictionRequestResponse> requests = moderatorService.getMyRestrictionRequests(moderatorId, pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/restriction-requests/pending")
    public ResponseEntity<Page<RestrictionRequestResponse>> getPendingRestrictionRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RestrictionRequestResponse> requests = moderatorService.getPendingRestrictionRequests(pageable);
        return ResponseEntity.ok(requests);
    }
}
