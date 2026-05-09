package com.sysintegg7.potane.whisperwall.features.comments.repository;

import com.sysintegg7.potane.whisperwall.features.comments.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    Page<Comment> findByConfessionIdAndIsVisibleTrueOrderByCreatedAtDesc(
            Long confessionId, Pageable pageable);

    // kept for moderation use (admin/moderator viewing user's comments)
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Long countByConfessionId(Long confessionId);
}