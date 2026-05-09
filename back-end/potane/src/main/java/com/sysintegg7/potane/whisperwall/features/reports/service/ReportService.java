package com.sysintegg7.potane.whisperwall.features.reports.service;

import com.sysintegg7.potane.whisperwall.features.reports.dto.ReportRequest;
import com.sysintegg7.potane.whisperwall.features.reports.dto.ReportResponse;
import com.sysintegg7.potane.whisperwall.shared.exception.ResourceNotFoundException;
import com.sysintegg7.potane.whisperwall.features.confessions.model.Confession;
import com.sysintegg7.potane.whisperwall.features.reports.model.Report;
import com.sysintegg7.potane.whisperwall.shared.model.User;
import com.sysintegg7.potane.whisperwall.features.confessions.repository.ConfessionRepository;
import com.sysintegg7.potane.whisperwall.features.reports.repository.ReportRepository;
import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ReportResponse reportConfession(Long confessionId, Long userId, ReportRequest request) {
        Confession confession = confessionRepository.findById(confessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Report report = new Report();
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setConfession(confession);
        report.setReportedByUser(user);
        report.setStatus(Report.ReportStatus.PENDING);

        confession.setReportCount(confession.getReportCount() + 1);
        confessionRepository.save(confession);

        Report savedReport = reportRepository.save(report);
        return convertToReportResponse(savedReport);
    }

    public Page<ReportResponse> getPendingReports(Pageable pageable) {
        return reportRepository.findByStatus(Report.ReportStatus.PENDING, pageable)
                .map(this::convertToReportResponse);
    }

    public Page<ReportResponse> getReportsByStatus(String status, Pageable pageable) {
        String normalizedStatus = status == null ? "PENDING" : status.trim().toUpperCase(Locale.ROOT);

        return switch (normalizedStatus) {
            case "PENDING" -> reportRepository.findByStatus(Report.ReportStatus.PENDING, pageable)
                .map(this::convertToReportResponse);
            case "DISMISSED" -> reportRepository.findByStatus(Report.ReportStatus.DISMISSED, pageable)
                .map(this::convertToReportResponse);
            case "REVIEWED" -> reportRepository.findByStatusIn(
                    List.of(Report.ReportStatus.REVIEWED, Report.ReportStatus.RESOLVED),
                    pageable)
                .map(this::convertToReportResponse);
            default -> reportRepository.findByStatus(Report.ReportStatus.PENDING, pageable)
                .map(this::convertToReportResponse);
        };
    }

    public Page<ReportResponse> getConfessionReports(Long confessionId, Pageable pageable) {
        return reportRepository.findByConfessionId(confessionId, pageable)
                .map(this::convertToReportResponse);
    }

    @Transactional
    public void resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.setStatus(Report.ReportStatus.RESOLVED);
        reportRepository.save(report);
    }

    @Transactional
    public void dismissReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.setStatus(Report.ReportStatus.DISMISSED);
        reportRepository.save(report);
    }

    @Transactional
    public void removeConfessionAndResolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        Confession confession = report.getConfession();
        confession.setIsVisible(false);
        confessionRepository.save(confession);

        report.setStatus(Report.ReportStatus.RESOLVED);
        reportRepository.save(report);
    }

    private ReportResponse convertToReportResponse(Report report) {
        String confessionOwnerUsername = report.getConfession() != null
                && report.getConfession().getUser() != null
                ? report.getConfession().getUser().getUsername()
                : null;

        return new ReportResponse(
                report.getId(),
                report.getReason(),
                report.getDescription(),
                report.getConfession().getId(),
                report.getConfession().getContent(),
                report.getConfession().getCategory(),
                confessionOwnerUsername, // ✅ NEW field
                report.getReportedByUser().getId(),
                report.getReportedByUser().getUsername(),
                report.getStatus().toString(),
                report.getCreatedAt()
        );
    }
}