package com.sysintegg7.potane.whisperwall.repository;

import com.sysintegg7.potane.whisperwall.model.Confession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfessionRepository extends JpaRepository<Confession, Long> {
    Page<Confession> findByIsVisibleAndIsApprovedOrderByCreatedAtDesc(Boolean isVisible, Boolean isApproved, Pageable pageable);
    Page<Confession> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Long countByUserId(Long userId);
}
