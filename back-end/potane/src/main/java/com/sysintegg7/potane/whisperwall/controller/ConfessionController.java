package com.sysintegg7.potane.whisperwall.controller;

import com.sysintegg7.potane.whisperwall.dto.ConfessionRequest;
import com.sysintegg7.potane.whisperwall.dto.ConfessionResponse;
import com.sysintegg7.potane.whisperwall.service.ConfessionService;
import com.sysintegg7.potane.whisperwall.util.SecurityUtil;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/confessions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConfessionController {
    @Autowired
    private ConfessionService confessionService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<ConfessionResponse> createConfession(@Valid @RequestBody ConfessionRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ConfessionResponse response = confessionService.createConfession(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfessionResponse> getConfession(@PathVariable Long id) {
        ConfessionResponse response = confessionService.getConfession(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<ConfessionResponse>> getPublicConfessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<ConfessionResponse> response = confessionService.getPublicConfessions(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ConfessionResponse>> getUserConfessions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ConfessionResponse> response = confessionService.getUserConfessions(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConfessionResponse> updateConfession(
            @PathVariable Long id,
            @Valid @RequestBody ConfessionRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ConfessionResponse response = confessionService.updateConfession(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfession(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        confessionService.deleteConfession(id, userId);
        return ResponseEntity.noContent().build();
    }
}
