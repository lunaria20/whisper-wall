package com.sysintegg7.potane.whisperwall.features.users.controller;

import com.sysintegg7.potane.whisperwall.features.auth.dto.ChangePasswordRequest;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserProfileUpdateRequest;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;
import com.sysintegg7.potane.whisperwall.features.users.service.UserService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Principal principal) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            Principal principal,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(principal.getName(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changeCurrentUserPassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeCurrentUserPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/block/{blockedUserId}")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId, @PathVariable Long blockedUserId) {
        userService.blockUser(userId, blockedUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/unblock/{blockedUserId}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId, @PathVariable Long blockedUserId) {
        userService.unblockUser(userId, blockedUserId);
        return ResponseEntity.noContent().build();
    }
}
