package com.sysintegg7.potane.whisperwall.features.confessions.repository;

import com.sysintegg7.potane.whisperwall.features.confessions.model.Confession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ConfessionRepository extends JpaRepository<Confession, Long> {
    Page<Confession> findByIsVisibleTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Confession> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Long countByUserId(Long userId);
    Long countByCreatedAtAfter(LocalDateTime dateTime);
}
