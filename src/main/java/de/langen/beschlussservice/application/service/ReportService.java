package de.langen.beschlussservice.application.service;

import de.langen.beschlussservice.api.dto.request.CreateReportRequest;
import de.langen.beschlussservice.api.dto.request.UpdateReportRequest;
import de.langen.beschlussservice.api.dto.response.ReportResponse;
import de.langen.beschlussservice.api.exception.ResourceNotFoundException;
import de.langen.beschlussservice.application.mapper.ReportMapper;
import de.langen.beschlussservice.domain.entity.Decision;
import de.langen.beschlussservice.domain.entity.Report;
import de.langen.beschlussservice.domain.entity.ReportStatus;
import de.langen.beschlussservice.domain.entity.User;
import de.langen.beschlussservice.domain.repository.DecisionRepository;
import de.langen.beschlussservice.domain.repository.ReportRepository;
import de.langen.beschlussservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Report entity operations.
 * Handles CRUD operations and status transitions for reports.
 *
 * @author Backend Team
 * @version 2.1 - Added User tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;

    /**
     * Create a new report for a decision with authenticated user.
     *
     * @param decisionId decision UUID
     * @param request report data
     * @param currentUser authenticated user
     * @return created report
     */
    @Transactional
    public ReportResponse createReport(String decisionId, CreateReportRequest request, User currentUser) {
        log.info("Creating report for decisionId={} and year={} by user: {}",
                decisionId, request.getYear(), currentUser.getEmail());

        // Find decision
        Decision decision = decisionRepository.findById(UUID.fromString(decisionId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found with id: " + decisionId));

        // Check if report for this year already exists
        boolean exists = reportRepository.existsByDecisionIdAndYear(
                UUID.fromString(decisionId),
                request.getYear()
        );

        if (exists) {
            throw new IllegalArgumentException(
                    "Report for year " + request.getYear() + " already exists for this decision"
            );
        }

        // Map to entity
        Report report = reportMapper.toEntity(request);
        report.setDecision(decision);
        report.setCreatedBy(currentUser.getId());

        // Save
        Report saved = reportRepository.save(report);

        // Keep bidirectional association in sync
        decision.addReport(saved);

        log.info("Report created with id: {} by user: {}", saved.getId(), currentUser.getEmail());
        return reportMapper.toResponse(saved, currentUser);
    }

    /**
     * Update an existing report with authenticated user.
     *
     * @param reportId report UUID
     * @param request update data
     * @param currentUser authenticated user (for audit)
     * @return updated report
     */
    @Transactional
    public ReportResponse updateReport(String reportId, UpdateReportRequest request, User currentUser) {
        log.info("Updating report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Update fields
        if (request.getTitle() != null) {
            report.setTitle(request.getTitle());
        }

        if (request.getYear() != null) {
            // Check if year change would create duplicate
            if (!request.getYear().equals(report.getYear())) {
                boolean exists = reportRepository.existsByDecisionIdAndYear(
                        report.getDecision().getId(),
                        request.getYear()
                );
                if (exists) {
                    throw new IllegalArgumentException(
                            "Report for year " + request.getYear() + " already exists for this decision"
                    );
                }
                report.setYear(request.getYear());
            }
        }

        if (request.getContent() != null) {
            report.setContent(request.getContent());
        }

        if (request.getExpectedCompletionQuarter() != null) {
            report.setExpectedCompletionQuarter(request.getExpectedCompletionQuarter());
        }

        if (request.getStatus() != null) {
            ReportStatus newStatus = ReportStatus.valueOf(request.getStatus().toUpperCase());
            report.setStatus(newStatus);
        }

        Report updated = reportRepository.save(report);

        // Load creator user for response
        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(updated, createdByUser);
    }

    /**
     * Get report by ID.
     *
     * @param reportId report UUID
     * @return report
     */
    @Transactional(readOnly = true)
    public ReportResponse getReportById(String reportId) {
        log.debug("Fetching report with id: {}", reportId);

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(report, createdByUser);
    }

    /**
     * Get all reports for a decision.
     *
     * @param decisionId decision UUID
     * @return list of reports
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByDecisionId(String decisionId) {
        log.debug("Fetching reports for decision: {}", decisionId);

        // Verify decision exists
        if (!decisionRepository.existsById(UUID.fromString(decisionId))) {
            throw new ResourceNotFoundException("Decision not found with id: " + decisionId);
        }

        List<Report> reports = reportRepository.findByDecisionIdOrderByYearDesc(
                UUID.fromString(decisionId)
        );

        // Map to responses
        return reports.stream()
                .map(report -> {
                    User createdByUser = report.getCreatedBy() != null
                            ? userRepository.findById(report.getCreatedBy()).orElse(null)
                            : null;
                    return reportMapper.toResponse(report, createdByUser);
                })
                .collect(Collectors.toList());
    }

    /**
     * Delete a report.
     *
     * @param reportId report UUID
     * @param currentUser authenticated user (for audit)
     */
    @Transactional
    public void deleteReport(String reportId, User currentUser) {
        log.info("Deleting report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        reportRepository.delete(report);
    }

    /**
     * Submit a report (DRAFT → SUBMITTED).
     *
     * @param reportId report UUID
     * @param currentUser authenticated user (for audit)
     * @return updated report
     */
    @Transactional
    public ReportResponse submitReport(String reportId, User currentUser) {
        log.info("Submitting report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Use business logic from entity
        report.submit();

        Report updated = reportRepository.save(report);

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(updated, createdByUser);
    }

    /**
     * Approve a report (SUBMITTED → APPROVED).
     *
     * @param reportId report UUID
     * @param currentUser authenticated user (must be admin)
     * @return updated report
     */
    @Transactional
    public ReportResponse approveReport(String reportId, User currentUser) {
        log.info("Approving report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Use business logic from entity
        report.approve();

        Report updated = reportRepository.save(report);

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(updated, createdByUser);
    }

    /**
     * Reject a report (any status → REJECTED).
     *
     * @param reportId report UUID
     * @param currentUser authenticated user (must be admin)
     * @return updated report
     */
    @Transactional
    public ReportResponse rejectReport(String reportId, User currentUser) {
        log.info("Rejecting report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Use business logic from entity
        report.reject();

        Report updated = reportRepository.save(report);

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(updated, createdByUser);
    }
}