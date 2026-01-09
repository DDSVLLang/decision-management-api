package de.langen.beschlussservice.api.controller;

import de.langen.beschlussservice.api.dto.request.CreateDecisionRequest;
import de.langen.beschlussservice.api.dto.request.CreateReportRequest;
import de.langen.beschlussservice.api.dto.request.SearchDecisionRequest;
import de.langen.beschlussservice.api.dto.request.UpdateDecisionRequest;
import de.langen.beschlussservice.api.dto.response.ApiResponse;
import de.langen.beschlussservice.api.dto.response.DecisionResponse;
import de.langen.beschlussservice.api.dto.response.ReportResponse;
import de.langen.beschlussservice.application.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/decision")
@RequiredArgsConstructor
@Tag(name = "Decision API", description = "Decision management endpoints")
public class DecisionApiController {

    private final DecisionService decisionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new decision")
    public ApiResponse<DecisionResponse> createDecision(
            @Valid @RequestBody CreateDecisionRequest request
    ) {
        DecisionResponse response = decisionService.createDecision(request);
        return ApiResponse.success(response, "Decision created successfully");
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get decision by ID")
    public ApiResponse<DecisionResponse> getDecision(@PathVariable String id) {
        DecisionResponse response = decisionService.getDecisionById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    // @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Search decisions with filters and pagination")
    public ApiResponse<Page<DecisionResponse>> searchDecisions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String responsibleDepartment,
            @RequestParam(required = false) String decisionDateFrom,
            @RequestParam(required = false) String decisionDateTo,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "decisionDate,desc") String[] sort
    ) {
        SearchDecisionRequest searchRequest = SearchDecisionRequest.builder()
                .status(status)
                .topic(topic)
                .responsibleDepartment(responsibleDepartment)
                .decisionDateFrom(decisionDateFrom)
                .decisionDateTo(decisionDateTo)
                .keyword(keyword)
                .build();

        Sort sorting = Sort.by(
                sort[1].equalsIgnoreCase("asc")
                        ? Sort.Order.asc(sort[0])
                        : Sort.Order.desc(sort[0])
        );

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<DecisionResponse> decisions = decisionService.searchDecisions(searchRequest, pageable);

        return ApiResponse.<Page<DecisionResponse>>builder()
                .success(true)
                .data(decisions)
                .metadata(Map.of(
                        "totalElements", decisions.getTotalElements(),
                        "totalPages", decisions.getTotalPages(),
                        "currentPage", decisions.getNumber(),
                        "pageSize", decisions.getSize()
                ))
                .build();
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update decision")
    public ApiResponse<DecisionResponse> updateDecision(
            @PathVariable String id,
            @Valid @RequestBody UpdateDecisionRequest request
    ) {
        DecisionResponse response = decisionService.updateDecision(id, request);
        return ApiResponse.success(response, "Decision updated successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete decision (soft delete)")
    public void deleteDecision(@PathVariable Long id) {
        decisionService.deleteDecision(id);
    }

    @PostMapping("/{decisionId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    // @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a report for a decision")
    public ApiResponse<ReportResponse> createReportForDecision(
            @PathVariable String decisionId,
            @Valid @RequestBody CreateReportRequest request
    ) {

        ReportResponse response = decisionService.createReport(decisionId, request, "");
        return ApiResponse.success(response, "Report created successfully");
    }

}

