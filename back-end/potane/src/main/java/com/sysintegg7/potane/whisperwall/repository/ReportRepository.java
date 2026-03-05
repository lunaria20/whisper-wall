package com.sysintegg7.potane.whisperwall.repository;

import com.sysintegg7.potane.whisperwall.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
    Page<Report> findByConfessionId(Long confessionId, Pageable pageable);
}
