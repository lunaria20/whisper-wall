package com.sysintegg7.potane.whisperwall.features.users.service;

import com.sysintegg7.potane.whisperwall.features.auth.dto.ChangePasswordRequest;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserProfileUpdateRequest;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;
import com.sysintegg7.potane.whisperwall.shared.exception.ResourceAlreadyExistsException;
import com.sysintegg7.potane.whisperwall.shared.exception.ResourceNotFoundException;
import com.sysintegg7.potane.whisperwall.shared.model.User;
import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToUserResponse(user);
    }

    public UserResponse getCurrentUserProfile(String principalName) {
        User user = userRepository.findByUsernameIgnoreCase(principalName)
                .or(() -> userRepository.findByEmailIgnoreCase(principalName))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return convertToUserResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUserProfile(String principalName, UserProfileUpdateRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(principalName)
                .or(() -> userRepository.findByEmailIgnoreCase(principalName))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        if (StringUtils.hasText(request.getUsername())) {
            String nextUsername = request.getUsername().trim();
            if (!nextUsername.equalsIgnoreCase(user.getUsername()) && Boolean.TRUE.equals(userRepository.existsByUsernameIgnoreCase(nextUsername))) {
                throw new ResourceAlreadyExistsException("Username is already taken");
            }
            user.setUsername(nextUsername);
        }

        if (StringUtils.hasText(request.getEmail())) {
            String nextEmail = request.getEmail().trim();
            if (!nextEmail.equalsIgnoreCase(user.getEmail()) && Boolean.TRUE.equals(userRepository.existsByEmailIgnoreCase(nextEmail))) {
                throw new ResourceAlreadyExistsException("Email is already in use");
            }
            user.setEmail(nextEmail);
        }

        user.setBio(StringUtils.hasText(request.getBio()) ? request.getBio().trim() : null);

        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture().trim());
        }

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    @Transactional
    public void changeCurrentUserPassword(String principalName, ChangePasswordRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(principalName)
                .or(() -> userRepository.findByEmailIgnoreCase(principalName))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(user);
    }

    @Transactional
    public void blockUser(Long userId, Long blockedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User to block not found"));

        user.getBlockedUsers().add(blockedUser);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long userId, Long blockedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.getBlockedUsers().remove(blockedUser);
        userRepository.save(user);
    }

    private UserResponse convertToUserResponse(User user) {
        String roleName = user.getRoles().stream()
                .map(role -> role.getName())
                .findFirst()
                .orElse("USER");
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getProfilePicture(),
                user.getBio(),
                user.getIsVerified(),
                user.getIsActive(),
                user.getReportCount(),
                roleName,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
