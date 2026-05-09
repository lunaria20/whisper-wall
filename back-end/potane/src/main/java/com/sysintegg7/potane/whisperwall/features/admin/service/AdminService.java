package com.sysintegg7.potane.whisperwall.features.admin.service;

import com.sysintegg7.potane.whisperwall.features.users.dto.CreateUserRequest;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictUserRequest;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictionRequestResponse;
import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionRequest;
import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionResponse;
import com.sysintegg7.potane.whisperwall.features.users.dto.UsageStatsResponse;
import com.sysintegg7.potane.whisperwall.shared.exception.ResourceAlreadyExistsException;
import com.sysintegg7.potane.whisperwall.shared.exception.ResourceNotFoundException;
import com.sysintegg7.potane.whisperwall.shared.model.User;
import com.sysintegg7.potane.whisperwall.shared.model.Role;
import com.sysintegg7.potane.whisperwall.features.confessions.model.Confession;
import com.sysintegg7.potane.whisperwall.features.restrictions.model.RestrictionRequest;
import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;
import com.sysintegg7.potane.whisperwall.shared.repository.RoleRepository;
import com.sysintegg7.potane.whisperwall.features.confessions.repository.ConfessionRepository;
import com.sysintegg7.potane.whisperwall.features.comments.repository.CommentRepository;
import com.sysintegg7.potane.whisperwall.features.reactions.repository.ReactionRepository;
import com.sysintegg7.potane.whisperwall.features.restrictions.repository.RestrictionRequestRepository;
import com.sysintegg7.potane.whisperwall.features.reports.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReactionRepository reactionRepository; // ✅ FIX: inject to count safely

    @Autowired
    private RestrictionRequestRepository restrictionRequestRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername().trim())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim().toLowerCase())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setIsActive(true);
        user.setIsVerified(false);
        user.setIsRestricted(false);

        Set<Role> roles = new HashSet<>();
        String roleName = request.getRoleName() != null ? request.getRoleName() : "ROLE_USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        roles.add(role);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.getConfessions().forEach(confession -> {
            confessionRepository.delete(confession);
        });

        user.getComments().forEach(comment -> {
            commentRepository.delete(comment);
        });

        userRepository.delete(user);
        log.info("Deleted user with id: {}", userId);
    }

    @Transactional
    public UserResponse restrictUser(Long userId, RestrictUserRequest request, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Integer durationDays = request.getDurationDays() != null ? request.getDurationDays() : 7;
        user.setIsRestricted(true);
        user.setRestrictionEndDate(LocalDateTime.now().plusDays(durationDays));
        user.setRestrictionReason(request.getReason());
        user.setRestrictedByAdminId(adminId);

        User restrictedUser = userRepository.save(user);
        log.info("User {} restricted for {} days by admin {}", userId, durationDays, adminId);
        return convertToUserResponse(restrictedUser);
    }

    @Transactional
    public UserResponse unrestrictUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsRestricted(false);
        user.setRestrictionEndDate(null);
        user.setRestrictionReason(null);
        user.setRestrictedByAdminId(null);

        User unrestrictedUser = userRepository.save(user);
        log.info("User {} unrestricted", userId);
        return convertToUserResponse(unrestrictedUser);
    }

    @Transactional
    public void deletePost(Long postId) {
        Confession confession = confessionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        confessionRepository.delete(confession);
        log.info("Deleted confession with id: {}", postId);
    }

    @Transactional
    public ConfessionResponse updatePost(Long postId, ConfessionRequest request) {
        Confession confession = confessionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (request.getContent() != null) {
            confession.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            confession.setCategory(request.getCategory());
        }
        if (request.getMood() != null) {
            confession.setMood(request.getMood());
        }

        Confession updated = confessionRepository.save(confession);
        return convertToConfessionResponse(updated);
    }

    @Transactional
    public ConfessionResponse getPost(Long postId) {
        Confession confession = confessionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return convertToConfessionResponse(confession);
    }

    // ✅ FIX: use repository count queries instead of lazy collection .size()
    @Transactional(readOnly = true)
    public Page<ConfessionResponse> getAllPosts(Pageable pageable) {
        return confessionRepository.findAll(pageable).map(this::convertToConfessionResponse);
    }

    public UsageStatsResponse getUsageStats() {
        long totalUsers = userRepository.count();
        long totalPosts = confessionRepository.count();
        long totalComments = commentRepository.count();
        long totalReports = reportRepository.count();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = confessionRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long restrictedUsers = userRepository.countByIsRestrictedTrue();

        Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");
        Optional<Role> moderatorRole = roleRepository.findByName("ROLE_MODERATOR");

        long adminUsers = adminRole.isPresent() ? userRepository.countByRolesContains(adminRole.get()) : 0;
        long moderatorUsers = moderatorRole.isPresent() ? userRepository.countByRolesContains(moderatorRole.get()) : 0;

        return new UsageStatsResponse(
                totalUsers,
                totalPosts,
                totalComments,
                totalReports,
                activeUsers,
                restrictedUsers,
                adminUsers,
                moderatorUsers
        );
    }

    @Transactional
    public RestrictionRequestResponse approveRestrictionRequest(Long requestId, RestrictUserRequest request, Long adminId) {
        RestrictionRequest restrictionRequest = restrictionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction request not found"));

        User user = restrictionRequest.getUserToRestrict();
        Integer durationDays = restrictionRequest.getRequestedDurationDays();
        user.setIsRestricted(true);
        user.setRestrictionEndDate(LocalDateTime.now().plusDays(durationDays));
        user.setRestrictionReason(restrictionRequest.getReason());
        user.setRestrictedByAdminId(adminId);
        userRepository.save(user);

        restrictionRequest.setStatus("APPROVED");
        restrictionRequest.setReviewedByAdmin(userRepository.findById(adminId).orElseThrow());
        restrictionRequest.setReviewedAt(LocalDateTime.now());
        RestrictionRequest updated = restrictionRequestRepository.save(restrictionRequest);

        log.info("Approved restriction request {} for user {} till {}", requestId, user.getId(), user.getRestrictionEndDate());
        return convertToRestrictionRequestResponse(updated);
    }

    @Transactional
    public RestrictionRequestResponse rejectRestrictionRequest(Long requestId, String reason, Long adminId) {
        RestrictionRequest restrictionRequest = restrictionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction request not found"));

        restrictionRequest.setStatus("REJECTED");
        restrictionRequest.setReviewedByAdmin(userRepository.findById(adminId).orElseThrow());
        restrictionRequest.setAdminResponse(reason);
        restrictionRequest.setReviewedAt(LocalDateTime.now());

        RestrictionRequest updated = restrictionRequestRepository.save(restrictionRequest);
        log.info("Rejected restriction request {}", requestId);
        return convertToRestrictionRequestResponse(updated);
    }

    public Page<RestrictionRequestResponse> getPendingRestrictionRequests(Pageable pageable) {
        return restrictionRequestRepository.findByStatus("PENDING", pageable)
                .map(this::convertToRestrictionRequestResponse);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToUserResponse);
    }

    private UserResponse convertToUserResponse(User user) {
        String roleName = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().iterator().next().getName()
                : "ROLE_USER";

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

   
    private ConfessionResponse convertToConfessionResponse(Confession confession) {
        Long commentCount = commentRepository.countByConfessionId(confession.getId());
        Long reactionCount = reactionRepository.countByConfessionId(confession.getId());

        return new ConfessionResponse(
                confession.getId(),
                confession.getContent(),
                confession.getUser().getId(),
                confession.getUser().getUsername(),
                confession.getUser().getDisplayName(),
                confession.getUser().getProfilePicture(),
                confession.getIsApproved(),
                confession.getIsVisible(),
                confession.getCategory(),
                confession.getMood(),
                confession.getReportCount(),
                Math.toIntExact(commentCount),
                Math.toIntExact(reactionCount),
                confession.getCreatedAt(),
                confession.getUpdatedAt()
        );
    }

    private RestrictionRequestResponse convertToRestrictionRequestResponse(RestrictionRequest request) {
        return new RestrictionRequestResponse(
                request.getId(),
                request.getUserToRestrict() != null ? convertToUserResponse(request.getUserToRestrict()) : null,
                request.getRequestedByModerator() != null ? convertToUserResponse(request.getRequestedByModerator()) : null,
                request.getReason(),
                request.getRequestedDurationDays(),
                request.getStatus(),
                request.getReviewedByAdmin() != null ? convertToUserResponse(request.getReviewedByAdmin()) : null,
                request.getAdminResponse(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getReviewedAt()
        );
    }
}