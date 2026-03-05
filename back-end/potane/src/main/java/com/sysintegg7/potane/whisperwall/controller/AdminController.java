package com.sysintegg7.potane.whisperwall.controller;

import com.sysintegg7.potane.whisperwall.dto.ReportResponse;
import com.sysintegg7.potane.whisperwall.service.ReportService;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/reports/pending")
    public ResponseEntity<Page<ReportResponse>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> response = reportService.getPendingReports(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reports/{reportId}/resolve")
    public ResponseEntity<Void> resolveReport(@PathVariable Long reportId) {
        reportService.resolveReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{reportId}/dismiss")
    public ResponseEntity<Void> dismissReport(@PathVariable Long reportId) {
        reportService.dismissReport(reportId);
        return ResponseEntity.noContent().build();
    }
}
