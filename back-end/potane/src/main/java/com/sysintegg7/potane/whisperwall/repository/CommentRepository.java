package com.sysintegg7.potane.whisperwall.repository;

import com.sysintegg7.potane.whisperwall.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByConfessionIdAndIsVisibleAndIsApprovedOrderByCreatedAtDesc(
            Long confessionId, Boolean isVisible, Boolean isApproved, Pageable pageable);
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Long countByConfessionId(Long confessionId);
}
