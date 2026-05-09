package com.sysintegg7.potane.whisperwall.features.restrictions.repository;

import com.sysintegg7.potane.whisperwall.features.restrictions.model.RestrictionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestrictionRequestRepository extends JpaRepository<RestrictionRequest, Long> {
    Page<RestrictionRequest> findByStatus(String status, Pageable pageable);
    Page<RestrictionRequest> findByReviewedByAdminId(Long adminId, Pageable pageable);
    Page<RestrictionRequest> findByRequestedByModeratorId(Long moderatorId, Pageable pageable);
    List<RestrictionRequest> findByStatusOrderByCreatedAtDesc(String status);
    Optional<RestrictionRequest> findFirstByUserToRestrictIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
