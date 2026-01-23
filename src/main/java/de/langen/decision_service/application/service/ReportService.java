package de.langen.decision_service.application.service;

import de.langen.decision_service.api.dto.request.CreateReportRequest;
import de.langen.decision_service.api.dto.request.UpdateReportRequest;
import de.langen.decision_service.api.dto.response.ReportResponse;
import de.langen.decision_service.api.exception.ResourceNotFoundException;
import de.langen.decision_service.application.mapper.ReportMapper;
import de.langen.decision_service.domain.entity.Decision;
import de.langen.decision_service.domain.entity.Report;
import de.langen.decision_service.domain.entity.ReportStatus;
import de.langen.decision_service.domain.entity.User;
import de.langen.decision_service.domain.repository.DecisionRepository;
import de.langen.decision_service.domain.repository.ReportRepository;
import de.langen.decision_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Report entity operations with Role-Based Access Control.
 *
 * Access Rules:
 * - ADMIN: Can view/create/update/delete/approve/reject all reports
 * - USER: Can only view/create/update/submit reports for decisions assigned to them
 *
 * @author Backend Team
 * @version 3.0 - Added RBAC
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
     * Create a new report for a decision.
     * ADMIN: Can create for any decision
     * USER: Can only create for decisions assigned to them
     *
     * @param decisionId decision UUID
     * @param request report data
     * @param currentUser authenticated user
     * @return created report
     * @throws AccessDeniedException if user doesn't have access to decision
     */
    @Transactional
    public ReportResponse createReport(String decisionId, CreateReportRequest request, User currentUser) {
        log.info("Creating report for decisionId={} and year={} by user: {}",
                decisionId, request.getYear(), currentUser.getEmail());

        Decision decision = decisionRepository.findById(UUID.fromString(decisionId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found with id: " + decisionId));

        // Check access to decision
        checkDecisionAccess(decision, currentUser, "create report for");

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

        Report report = reportMapper.toEntity(request);
        report.setDecision(decision);
        report.setCreatedBy(currentUser.getId());

        Report saved = reportRepository.save(report);
        decision.addReport(saved);

        log.info("Report created with id: {} by user: {}", saved.getId(), currentUser.getEmail());
        return reportMapper.toResponse(saved, currentUser);
    }

    /**
     * Update an existing report.
     * ADMIN: Can update any report
     * USER: Can only update reports for decisions assigned to them
     *
     * @param reportId report UUID
     * @param request update data
     * @param currentUser authenticated user
     * @return updated report
     * @throws AccessDeniedException if user doesn't have access
     */
    @Transactional
    public ReportResponse updateReport(String reportId, UpdateReportRequest request, User currentUser) {
        log.info("Updating report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Check access to decision
        checkDecisionAccess(report.getDecision(), currentUser, "update report for");

        // Update fields
        if (request.getTitle() != null) {
            report.setTitle(request.getTitle());
        }

        if (request.getYear() != null) {
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

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(updated, createdByUser);
    }

    /**
     * Get report by ID.
     * ADMIN: Can view any report
     * USER: Can only view reports for decisions assigned to them
     *
     * @param reportId report UUID
     * @param currentUser authenticated user
     * @return report
     * @throws AccessDeniedException if user doesn't have access
     */
    @Transactional(readOnly = true)
    public ReportResponse getReportById(String reportId, User currentUser) {
        log.debug("Fetching report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Check access to decision
        checkDecisionAccess(report.getDecision(), currentUser, "view report for");

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(report, createdByUser);
    }

    /**
     * Get all reports for a decision.
     * ADMIN: Can view reports for any decision
     * USER: Can only view reports for decisions assigned to them
     *
     * @param decisionId decision UUID
     * @param currentUser authenticated user
     * @return list of reports
     * @throws AccessDeniedException if user doesn't have access
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByDecisionId(String decisionId, User currentUser) {
        log.debug("Fetching reports for decision: {} by user: {}", decisionId, currentUser.getEmail());

        Decision decision = decisionRepository.findById(UUID.fromString(decisionId))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + decisionId));

        // Check access to decision
        checkDecisionAccess(decision, currentUser, "view reports for");

        List<Report> reports = reportRepository.findByDecisionIdOrderByYearDesc(
                UUID.fromString(decisionId)
        );

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
     * ADMIN: Can delete any report
     * USER: Can only delete reports for decisions assigned to them
     *
     * @param reportId report UUID
     * @param currentUser authenticated user
     * @throws AccessDeniedException if user doesn't have access
     */
    @Transactional
    public void deleteReport(String reportId, User currentUser) {
        log.info("Deleting report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Check access to decision
        checkDecisionAccess(report.getDecision(), currentUser, "delete report for");

        reportRepository.delete(report);
    }

    /**
     * Submit a report (DRAFT → SUBMITTED).
     * ADMIN: Can submit any report
     * USER: Can only submit reports for decisions assigned to them
     *
     * @param reportId report UUID
     * @param currentUser authenticated user
     * @return updated report
     * @throws AccessDeniedException if user doesn't have access
     */
    @Transactional
    public ReportResponse submitReport(String reportId, User currentUser) {
        log.info("Submitting report with id: {} by user: {}", reportId, currentUser.getEmail());

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        // Check access to decision
        checkDecisionAccess(report.getDecision(), currentUser, "submit report for");

        report.submit();
        Report updated = reportRepository.save(report);

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        return reportMapper.toResponse(updated, createdByUser);
    }

    /**
     * Approve a report (SUBMITTED → APPROVED).
     * ADMIN ONLY
     *
     * @param reportId report UUID
     * @param currentUser authenticated user (must be ADMIN)
     * @return updated report
     * @throws AccessDeniedException if user is not admin
     */
    @Transactional
    public ReportResponse approveReport(String reportId, User currentUser) {
        log.info("Approving report with id: {} by user: {}", reportId, currentUser.getEmail());

        if (!currentUser.isAdmin()) {
            log.warn("Access denied: User {} attempted to approve report", currentUser.getEmail());
            throw new AccessDeniedException("Only administrators can approve reports");
        }

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        report.approve();
        Report updated = reportRepository.save(report);

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        log.info("Report {} approved by admin: {}", reportId, currentUser.getEmail());
        return reportMapper.toResponse(updated, createdByUser);
    }

    /**
     * Reject a report (any status → REJECTED).
     * ADMIN ONLY
     *
     * @param reportId report UUID
     * @param currentUser authenticated user (must be ADMIN)
     * @return updated report
     * @throws AccessDeniedException if user is not admin
     */
    @Transactional
    public ReportResponse rejectReport(String reportId, User currentUser) {
        log.info("Rejecting report with id: {} by user: {}", reportId, currentUser.getEmail());

        if (!currentUser.isAdmin()) {
            log.warn("Access denied: User {} attempted to reject report", currentUser.getEmail());
            throw new AccessDeniedException("Only administrators can reject reports");
        }

        Report report = reportRepository.findById(UUID.fromString(reportId))
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        report.reject();
        Report updated = reportRepository.save(report);

        User createdByUser = report.getCreatedBy() != null
                ? userRepository.findById(report.getCreatedBy()).orElse(null)
                : null;

        log.info("Report {} rejected by admin: {}", reportId, currentUser.getEmail());
        return reportMapper.toResponse(updated, createdByUser);
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Check if user has access to a decision (for report operations).
     *
     * @param decision the decision
     * @param user the user
     * @param action action being performed (for logging)
     * @throws AccessDeniedException if user doesn't have access
     */
    private void checkDecisionAccess(Decision decision, User user, String action) {
        // Admin has access to everything
        if (user.isAdmin()) {
            return;
        }

        // User can only access decisions assigned to them
        if (decision.getAssignee() == null || !decision.getAssignee().getId().equals(user.getId())) {
            log.warn("Access denied: User {} attempted to {} decision {} (not assigned)",
                    user.getEmail(), action, decision.getId());
            throw new AccessDeniedException(
                    "You can only " + action + " decisions assigned to you"
            );
        }
    }
}