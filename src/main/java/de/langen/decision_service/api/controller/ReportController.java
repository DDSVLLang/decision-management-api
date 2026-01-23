package de.langen.decision_service.api.controller;

import de.langen.decision_service.api.dto.request.CreateReportRequest;
import de.langen.decision_service.api.dto.request.UpdateReportRequest;
import de.langen.decision_service.api.dto.response.ApiResponse;
import de.langen.decision_service.api.dto.response.ReportResponse;
import de.langen.decision_service.application.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import de.langen.decision_service.domain.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Report operations.
 * Handles creating, updating, retrieving and deleting reports for decisions.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * Create a new report for a decision.
     * ADMIN: Can create for any decision
     * USER: Can only create for decisions assigned to them
     *
     * POST /api/v1/decision/{decisionId}/report
     */
    @PostMapping("/decision/{decisionId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @PathVariable String decisionId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Creating report for decision: {} by user: {}", decisionId, currentUser.getEmail());

        ReportResponse report = reportService.createReport(decisionId, request, currentUser);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Report created successfully")
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing report.
     * ADMIN: Can update any report
     * USER: Can only update reports for decisions assigned to them
     *
     * PUT /api/v1/report/{reportId}
     */
    @PutMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReport(
            @PathVariable String reportId,
            @Valid @RequestBody UpdateReportRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Updating report: {} by user: {}", reportId, currentUser.getEmail());

        ReportResponse report = reportService.updateReport(reportId, request, currentUser);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Report updated successfully")
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific report by ID.
     * ADMIN: Can view any report
     * USER: Can only view reports for decisions assigned to them
     *
     * GET /api/v1/report/{reportId}
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal User currentUser
    ) {
        ReportResponse report = reportService.getReportById(reportId, currentUser);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get all reports for a specific decision.
     * ADMIN: Can view reports for any decision
     * USER: Can only view reports for decisions assigned to them
     *
     * GET /api/v1/decision/{decisionId}/reports
     */
    @GetMapping("/decision/{decisionId}/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReportsByDecision(
            @PathVariable String decisionId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<ReportResponse> reports = reportService.getReportsByDecisionId(decisionId, currentUser);

        ApiResponse<List<ReportResponse>> response = ApiResponse.<List<ReportResponse>>builder()
                .success(true)
                .data(reports)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a report.
     * ADMIN: Can delete any report
     * USER: Can only delete reports for decisions assigned to them
     *
     * DELETE /api/v1/report/{reportId}
     */
    @DeleteMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Deleting report: {} by user: {}", reportId, currentUser.getEmail());

        reportService.deleteReport(reportId, currentUser);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Report deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Submit a report (change status from DRAFT to SUBMITTED).
     * ADMIN: Can submit any report
     * USER: Can only submit reports for decisions assigned to them
     *
     * POST /api/v1/report/{reportId}/submit
     */
    @PostMapping("/report/{reportId}/submit")
    public ResponseEntity<ApiResponse<ReportResponse>> submitReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Submitting report: {} by user: {}", reportId, currentUser.getEmail());

        ReportResponse report = reportService.submitReport(reportId, currentUser);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Report submitted successfully")
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Approve a report (change status to APPROVED).
     * ADMIN ONLY
     *
     * POST /api/v1/report/{reportId}/approve
     */
    @PostMapping("/report/{reportId}/approve")
    @PreAuthorize("hasRole('ADMIN')")  // ⭐ Nur Admins
    public ResponseEntity<ApiResponse<ReportResponse>> approveReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Approving report: {} by admin: {}", reportId, currentUser.getEmail());

        ReportResponse report = reportService.approveReport(reportId, currentUser);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Report approved successfully")
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Reject a report (change status to REJECTED).
     * ADMIN ONLY
     *
     * POST /api/v1/report/{reportId}/reject
     */
    @PostMapping("/report/{reportId}/reject")
    @PreAuthorize("hasRole('ADMIN')")  // ⭐ Nur Admins
    public ResponseEntity<ApiResponse<ReportResponse>> rejectReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Rejecting report: {} by admin: {}", reportId, currentUser.getEmail());

        ReportResponse report = reportService.rejectReport(reportId, currentUser);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Report rejected successfully")
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}