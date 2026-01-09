package de.langen.beschlussservice.api.controller;

import de.langen.beschlussservice.api.dto.request.CreateDecisionRequest;
import de.langen.beschlussservice.api.dto.request.SearchDecisionRequest;
import de.langen.beschlussservice.api.dto.request.UpdateDecisionRequest;
import de.langen.beschlussservice.api.dto.response.ApiResponse;
import de.langen.beschlussservice.api.dto.response.DecisionResponse;
import de.langen.beschlussservice.application.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import de.langen.beschlussservice.domain.entity.User;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/decision")
@RequiredArgsConstructor
@Tag(name = "Decision API", description = "Decision management endpoints")
@Slf4j
public class DecisionApiController {

    private final DecisionService decisionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Create a new decision")
    public ResponseEntity<ApiResponse<DecisionResponse>> createDecision(
            @Valid @RequestBody CreateDecisionRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        log.info("Creating decision: {} by user: {}", request.getTitle(), currentUser.getEmail());

        DecisionResponse decision  = decisionService.createDecision(request, currentUser);

        ApiResponse<DecisionResponse> response = ApiResponse.<DecisionResponse>builder()
                .success(true)
                .message("Decision created successfully")
                .data(decision)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get decision by ID")
    public ApiResponse<DecisionResponse> getDecision(@PathVariable String id) {
        DecisionResponse response = decisionService.getDecisionById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Update decision")
    public ResponseEntity<ApiResponse<DecisionResponse>> updateDecision(
            @PathVariable String id,
            @Valid @RequestBody UpdateDecisionRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        log.info("Updating decision: {} by user: {}", id, currentUser.getEmail());
        DecisionResponse decision  = decisionService.updateDecision(id, request, currentUser);
        ApiResponse<DecisionResponse> response = ApiResponse.<DecisionResponse>builder()
                .success(true)
                .message("Decision updated successfully")
                .data(decision)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete decision (soft delete)")
    public void deleteDecision(@PathVariable String id) {
        decisionService.deleteDecision(id);
    }

}

