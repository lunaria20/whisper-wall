package com.sysintegg7.potane.whisperwall.features.reports.repository;

import com.sysintegg7.potane.whisperwall.features.reports.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
    Page<Report> findByStatusIn(Collection<Report.ReportStatus> statuses, Pageable pageable);
    Page<Report> findByConfessionId(Long confessionId, Pageable pageable);
}
