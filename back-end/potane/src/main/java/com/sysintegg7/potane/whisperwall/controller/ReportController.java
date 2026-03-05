package com.sysintegg7.potane.whisperwall.controller;

import com.sysintegg7.potane.whisperwall.dto.ReportRequest;
import com.sysintegg7.potane.whisperwall.dto.ReportResponse;
import com.sysintegg7.potane.whisperwall.service.ReportService;
import com.sysintegg7.potane.whisperwall.util.SecurityUtil;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {
    @Autowired
    private ReportService reportService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/confession/{confessionId}")
    public ResponseEntity<ReportResponse> reportConfession(
            @PathVariable Long confessionId,
            @Valid @RequestBody ReportRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ReportResponse response = reportService.reportConfession(confessionId, userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/confession/{confessionId}")
    public ResponseEntity<Page<ReportResponse>> getConfessionReports(
            @PathVariable Long confessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> response = reportService.getConfessionReports(confessionId, pageable);
        return ResponseEntity.ok(response);
    }
}
