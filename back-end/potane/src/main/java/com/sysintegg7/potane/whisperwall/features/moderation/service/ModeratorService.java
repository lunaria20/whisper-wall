package com.sysintegg7.potane.whisperwall.features.moderation.service;

import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionResponse;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.SendRestrictionRequestRequest;
import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictionRequestResponse;
import com.sysintegg7.potane.whisperwall.shared.exception.ResourceNotFoundException;
import com.sysintegg7.potane.whisperwall.shared.exception.UnauthorizedException;
import com.sysintegg7.potane.whisperwall.shared.model.User;
import com.sysintegg7.potane.whisperwall.shared.model.Role;
import com.sysintegg7.potane.whisperwall.features.confessions.model.Confession;
import com.sysintegg7.potane.whisperwall.features.comments.model.Comment;
import com.sysintegg7.potane.whisperwall.features.restrictions.model.RestrictionRequest;
import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;
import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;
import com.sysintegg7.potane.whisperwall.features.confessions.repository.ConfessionRepository;
import com.sysintegg7.potane.whisperwall.features.comments.repository.CommentRepository;
import com.sysintegg7.potane.whisperwall.features.reactions.repository.ReactionRepository;
import com.sysintegg7.potane.whisperwall.features.restrictions.repository.RestrictionRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ModeratorService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReactionRepository reactionRepository; 

    @Autowired
    private RestrictionRequestRepository restrictionRequestRepository;

    // View all posts
    @Transactional(readOnly = true)
    public Page<ConfessionResponse> viewAllPosts(Pageable pageable) {
        return confessionRepository.findAll(pageable).map(this::convertToConfessionResponse);
    }

    // View a specific post
    @Transactional(readOnly = true)
    public ConfessionResponse viewPost(Long postId) {
        Confession confession = confessionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return convertToConfessionResponse(confession);
    }

    // Delete a post
    @Transactional
    public void deletePost(Long postId) {
        Confession confession = confessionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        confessionRepository.delete(confession);
        log.info("Moderator deleted confession with id: {}", postId);
    }

    // Delete a comment
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
        log.info("Moderator deleted comment with id: {}", commentId);
    }

  
    @Transactional
    public RestrictionRequestResponse sendRestrictionRequest(SendRestrictionRequestRequest request, Long moderatorId) {
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderator not found"));

        if (request.getConfessionId() == null) {
            throw new ResourceNotFoundException("Confession ID is required");
        }

        Confession confession = confessionRepository.findById(request.getConfessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));
        User userToRestrict = confession.getUser();

        // Support both MODERATOR and ROLE_MODERATOR naming conventions.
        boolean isModerator = moderator.getRoles().stream()
            .map(Role::getName)
            .anyMatch(roleName -> "ROLE_MODERATOR".equalsIgnoreCase(roleName)
                || "MODERATOR".equalsIgnoreCase(roleName));

        if (!isModerator) {
            throw new UnauthorizedException("Only moderators can send restriction requests");
        }

        RestrictionRequest restrictionRequest = new RestrictionRequest();
        restrictionRequest.setUserToRestrict(userToRestrict);
        restrictionRequest.setRequestedByModerator(moderator);
        restrictionRequest.setReason(request.getReason());
        restrictionRequest.setRequestedDurationDays(
                request.getRequestedDurationDays() != null ? request.getRequestedDurationDays() : 7);
        restrictionRequest.setStatus("PENDING");

        RestrictionRequest saved = restrictionRequestRepository.save(restrictionRequest);
        log.info("Moderator {} sent restriction request for confession '{}'", moderatorId, request.getConfessionId());
        return convertToRestrictionRequestResponse(saved);
    }

    // Get restriction request history for this moderator
    public Page<RestrictionRequestResponse> getMyRestrictionRequests(Long moderatorId, Pageable pageable) {
        return restrictionRequestRepository.findByRequestedByModeratorId(moderatorId, pageable)
                .map(this::convertToRestrictionRequestResponse);
    }

    // Get all pending restriction requests
    public Page<RestrictionRequestResponse> getPendingRestrictionRequests(Pageable pageable) {
        return restrictionRequestRepository.findByStatus("PENDING", pageable)
                .map(this::convertToRestrictionRequestResponse);
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

    private UserResponse convertToUserResponse(User user) {
        String roleName = user.getRoles().stream()
                .map(Role::getName)
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