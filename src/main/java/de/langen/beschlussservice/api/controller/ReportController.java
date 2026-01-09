package de.langen.beschlussservice.api.controller;

import de.langen.beschlussservice.api.dto.request.CreateReportRequest;
import de.langen.beschlussservice.api.dto.request.UpdateReportRequest;
import de.langen.beschlussservice.api.dto.response.ApiResponse;
import de.langen.beschlussservice.api.dto.response.ReportResponse;
import de.langen.beschlussservice.application.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Report operations.
 * Handles creating, updating, retrieving and deleting reports for decisions.
 *
 * @author Backend Team
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
     *
     * POST /api/v1/decision/{decisionId}/report
     *
     * @param decisionId decision UUID
     * @param request report creation data
     * @return created report
     */
    @PostMapping("/decision/{decisionId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @PathVariable String decisionId,
            @Valid @RequestBody CreateReportRequest request
    ) {
        log.info("Creating report for decision: {}", decisionId);

        // TODO: Get actual user email from Security Context
        String creatorEmail = "admin@stadt-langen.de"; // Placeholder

        ReportResponse report = reportService.createReport(decisionId, request, creatorEmail);

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
     *
     * PUT /api/v1/report/{reportId}
     *
     * @param reportId report UUID
     * @param request update data
     * @return updated report
     */
    @PutMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReport(
            @PathVariable String reportId,
            @Valid @RequestBody UpdateReportRequest request
    ) {
        log.info("Updating report: {}", reportId);

        ReportResponse report = reportService.updateReport(reportId, request);

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
     *
     * GET /api/v1/report/{reportId}
     *
     * @param reportId report UUID
     * @return report details
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(
            @PathVariable String reportId
    ) {
        log.debug("Fetching report: {}", reportId);

        ReportResponse report = reportService.getReportById(reportId);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get all reports for a specific decision.
     *
     * GET /api/v1/decision/{decisionId}/reports
     *
     * @param decisionId decision UUID
     * @return list of reports
     */
    @GetMapping("/decision/{decisionId}/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReportsByDecision(
            @PathVariable String decisionId
    ) {
        log.debug("Fetching reports for decision: {}", decisionId);

        List<ReportResponse> reports = reportService.getReportsByDecisionId(decisionId);

        ApiResponse<List<ReportResponse>> response = ApiResponse.<List<ReportResponse>>builder()
                .success(true)
                .data(reports)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a report.
     *
     * DELETE /api/v1/report/{reportId}
     *
     * @param reportId report UUID
     * @return success message
     */
    @DeleteMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable String reportId
    ) {
        log.info("Deleting report: {}", reportId);

        reportService.deleteReport(reportId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Report deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Submit a report (change status from DRAFT to SUBMITTED).
     *
     * POST /api/v1/report/{reportId}/submit
     *
     * @param reportId report UUID
     * @return updated report
     */
    @PostMapping("/report/{reportId}/submit")
    public ResponseEntity<ApiResponse<ReportResponse>> submitReport(
            @PathVariable String reportId
    ) {
        log.info("Submitting report: {}", reportId);

        ReportResponse report = reportService.submitReport(reportId);

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
     *
     * POST /api/v1/report/{reportId}/approve
     *
     * @param reportId report UUID
     * @return updated report
     */
    @PostMapping("/report/{reportId}/approve")
    public ResponseEntity<ApiResponse<ReportResponse>> approveReport(
            @PathVariable String reportId
    ) {
        log.info("Approving report: {}", reportId);

        ReportResponse report = reportService.approveReport(reportId);

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
     *
     * POST /api/v1/report/{reportId}/reject
     *
     * @param reportId report UUID
     * @return updated report
     */
    @PostMapping("/report/{reportId}/reject")
    public ResponseEntity<ApiResponse<ReportResponse>> rejectReport(
            @PathVariable String reportId
    ) {
        log.info("Rejecting report: {}", reportId);

        ReportResponse report = reportService.rejectReport(reportId);

        ApiResponse<ReportResponse> response = ApiResponse.<ReportResponse>builder()
                .success(true)
                .message("Report rejected successfully")
                .data(report)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}