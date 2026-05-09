package com.sysintegg7.potane.whisperwall.features.admin.controller;

import com.sysintegg7.potane.whisperwall.features.users.dto.CreateUserRequest;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictUserRequest;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictionRequestResponse;
import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionRequest;
import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionResponse;
import com.sysintegg7.potane.whisperwall.features.reports.dto.ReportResponse;
import com.sysintegg7.potane.whisperwall.features.users.dto.UsageStatsResponse;
import com.sysintegg7.potane.whisperwall.features.admin.service.AdminService;
import com.sysintegg7.potane.whisperwall.features.reports.service.ReportService;
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
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    @Autowired
    private ReportService reportService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private SecurityUtil securityUtil;

    // ===== REPORT MANAGEMENT (Admin & Moderator) =====
    @GetMapping("/reports/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<ReportResponse>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> response = reportService.getPendingReports(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<ReportResponse>> getReportsByStatus(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> response = reportService.getReportsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reports/{reportId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> resolveReport(@PathVariable Long reportId) {
        reportService.resolveReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{reportId}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> dismissReport(@PathVariable Long reportId) {
        reportService.dismissReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{reportId}/remove-confession")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> removeConfession(@PathVariable Long reportId) {
        reportService.removeConfessionAndResolveReport(reportId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ===== USER MANAGEMENT (Admin Only) =====
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserResponse user = adminService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/restrict")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> restrictUser(
            @PathVariable Long userId,
            @RequestBody RestrictUserRequest request) {
        Long adminId = securityUtil.getCurrentUserId();
        UserResponse user = adminService.restrictUser(userId, request, adminId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{userId}/restrict")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> unrestrictUser(@PathVariable Long userId) {
        UserResponse user = adminService.unrestrictUser(userId);
        return ResponseEntity.ok(user);
    }

    // ===== POST MANAGEMENT (Admin Only) =====
    @GetMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfessionResponse> getPost(@PathVariable Long postId) {
        ConfessionResponse post = adminService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ConfessionResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConfessionResponse> posts = adminService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfessionResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody ConfessionRequest request) {
        ConfessionResponse post = adminService.updatePost(postId, request);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    // ===== STATISTICS & REPORTING (Admin Only) =====
    @GetMapping("/stats/usage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsageStatsResponse> getUsageStats() {
        UsageStatsResponse stats = adminService.getUsageStats();
        return ResponseEntity.ok(stats);
    }

    // ===== RESTRICTION REQUEST MANAGEMENT (Admin Only) =====
    @GetMapping("/restriction-requests/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RestrictionRequestResponse>> getPendingRestrictionRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RestrictionRequestResponse> requests = adminService.getPendingRestrictionRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/restriction-requests/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestrictionRequestResponse> approveRestrictionRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) RestrictUserRequest request) {
        Long adminId = securityUtil.getCurrentUserId();
        RestrictionRequestResponse response = adminService.approveRestrictionRequest(
                requestId,
                request != null ? request : new RestrictUserRequest(),
                adminId
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/restriction-requests/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestrictionRequestResponse> rejectRestrictionRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason) {
        Long adminId = securityUtil.getCurrentUserId();
        RestrictionRequestResponse response = adminService.rejectRestrictionRequest(requestId, reason, adminId);
        return ResponseEntity.ok(response);
    }
}

