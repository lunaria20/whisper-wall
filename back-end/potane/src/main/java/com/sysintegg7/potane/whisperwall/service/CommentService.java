package com.sysintegg7.potane.whisperwall.service;

import com.sysintegg7.potane.whisperwall.dto.CommentRequest;
import com.sysintegg7.potane.whisperwall.dto.CommentResponse;
import com.sysintegg7.potane.whisperwall.exception.ResourceNotFoundException;
import com.sysintegg7.potane.whisperwall.exception.UnauthorizedException;
import com.sysintegg7.potane.whisperwall.model.Comment;
import com.sysintegg7.potane.whisperwall.model.Confession;
import com.sysintegg7.potane.whisperwall.model.User;
import com.sysintegg7.potane.whisperwall.repository.CommentRepository;
import com.sysintegg7.potane.whisperwall.repository.ConfessionRepository;
import com.sysintegg7.potane.whisperwall.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long confessionId, Long userId, CommentRequest request) {
        Confession confession = confessionRepository.findById(confessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setConfession(confession);
        comment.setUser(user);
        comment.setIsApproved(false);
        comment.setIsVisible(true);

        Comment savedComment = commentRepository.save(comment);
        return convertToCommentResponse(savedComment);
    }

    public Page<CommentResponse> getConfessionComments(Long confessionId, Pageable pageable) {
        return commentRepository.findByConfessionIdAndIsVisibleAndIsApprovedOrderByCreatedAtDesc(
                confessionId, true, true, pageable)
                .map(this::convertToCommentResponse);
    }

    public Page<CommentResponse> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToCommentResponse);
    }

    @Transactional
    public CommentResponse updateComment(Long id, Long userId, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own comments");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return convertToCommentResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void approveComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        comment.setIsApproved(true);
        commentRepository.save(comment);
    }

    private CommentResponse convertToCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getConfession().getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getDisplayName(),
                comment.getUser().getProfilePicture(),
                comment.getIsApproved(),
                comment.getIsVisible(),
                comment.getReportCount(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
