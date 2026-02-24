package de.langen.decision_service.api.controller;

import de.langen.decision_service.api.dto.request.CreateDecisionRequest;
import de.langen.decision_service.api.dto.request.SearchDecisionRequest;
import de.langen.decision_service.api.dto.request.UpdateDecisionRequest;
import de.langen.decision_service.api.dto.response.ApiResponse;
import de.langen.decision_service.api.dto.response.DecisionResponse;
import de.langen.decision_service.application.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import de.langen.decision_service.domain.entity.User;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/decision")
@RequiredArgsConstructor
@Tag(name = "Decision API", description = "Decision management endpoints")
@Slf4j
public class DecisionApiController {

    private final DecisionService decisionService;

    /**
     * Create a new decision.
     * ADMIN ONLY
     *
     * POST /api/v1/decision
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Create a new decision")
    public ResponseEntity<ApiResponse<DecisionResponse>> createDecision(
            @Valid @RequestBody CreateDecisionRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        if (Objects.isNull(currentUser)) {
            log.error("Unauthorized access attempt - no current user found");
            ApiResponse<DecisionResponse> errorResponse = ApiResponse.<DecisionResponse>builder()
                    .success(false)
                    .message("Unauthorized: No authenticated user found")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

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

    /**
     * Get decision by ID.
     * ADMIN: Can view any decision
     * USER: Can only view decisions assigned to them
     *
     * GET /api/v1/decision/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get decision by ID")
    public ResponseEntity<ApiResponse<DecisionResponse>> getDecision(@PathVariable String id, @AuthenticationPrincipal User currentUser) {
        if (Objects.isNull(currentUser)) {
            log.error("Unauthorized access attempt - no current user found for decision ID: {}", id);
            ApiResponse<DecisionResponse> errorResponse = ApiResponse.<DecisionResponse>builder()
                    .success(false)
                    .message("Unauthorized: No authenticated user found")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        DecisionResponse decision = decisionService.getDecisionById(id, currentUser);
        ApiResponse<DecisionResponse> response = ApiResponse.<DecisionResponse>builder()
                .success(true)
                .data(decision)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Search decisions with filters.
     * ADMIN: Returns all decisions
     * USER: Returns only decisions assigned to them
     *
     * GET /api/v1/decision/search
     */
    @GetMapping("/search")
    @Operation(summary = "Search decisions with filters and pagination")
    public ResponseEntity<ApiResponse<Page<DecisionResponse>>> searchDecisions(
            @ModelAttribute SearchDecisionRequest request,
            Pageable pageable,
            @AuthenticationPrincipal User currentUser

    ) {
        if (Objects.isNull(currentUser)) {
            log.error("Unauthorized access attempt - no current user found for search");
            ApiResponse<Page<DecisionResponse>> errorResponse = ApiResponse.<Page<DecisionResponse>>builder()
                    .success(false)
                    .message("Unauthorized: No authenticated user found")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        log.debug("Searching decisions by user: {} (role: {})",
                currentUser.getEmail(), currentUser.getRole());

        Page<DecisionResponse> decisions = decisionService.searchDecisions(
                request,
                pageable,
                currentUser
        );

        if (decisions.isEmpty()) {
            log.debug("No decisions found for user: {} with search criteria", currentUser.getEmail());
            return ResponseEntity.noContent().build();
        }

        ApiResponse<Page<DecisionResponse>> response = ApiResponse.<Page<DecisionResponse>>builder()
                .success(true)
                .data(decisions)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Update a decision.
     * ADMIN ONLY
     *
     * PUT /api/v1/decision/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update decision")
    public ResponseEntity<ApiResponse<DecisionResponse>> updateDecision(
            @PathVariable String id,
            @Valid @RequestBody UpdateDecisionRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        if (Objects.isNull(currentUser)) {
            log.error("Unauthorized access attempt - no current user found for update of decision ID: {}", id);
            ApiResponse<DecisionResponse> errorResponse = ApiResponse.<DecisionResponse>builder()
                    .success(false)
                    .message("Unauthorized: No authenticated user found")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        log.info("Updating decision: {} by admin: {}", id, currentUser.getEmail());

        DecisionResponse decision = decisionService.updateDecision(id, request, currentUser);

        ApiResponse<DecisionResponse> response = ApiResponse.<DecisionResponse>builder()
                .success(true)
                .message("Decision updated successfully")
                .data(decision)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a decision.
     * ADMIN ONLY
     *
     * DELETE /api/v1/decision/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete decision (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteDecision(@PathVariable String id, @AuthenticationPrincipal User currentUser) {
        if (Objects.isNull(currentUser)) {
            log.error("Unauthorized access attempt - no current user found for deletion of decision ID: {}", id);
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Unauthorized: No authenticated user found")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        log.info("Deleting decision: {} by admin: {}", id, currentUser.getEmail());

        decisionService.deleteDecision(id, currentUser);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Decision deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get all decisions assigned to the current user.
     *
     * GET /api/v1/decision/my-assignments
     */
    @GetMapping("/my-assignments")
    public ResponseEntity<ApiResponse<Page<DecisionResponse>>> getMyAssignments(
            Pageable pageable,
            @AuthenticationPrincipal User currentUser
    ) {
        if (Objects.isNull(currentUser)) {
            log.error("Unauthorized access attempt - no current user found for my-assignments");
            ApiResponse<Page<DecisionResponse>> errorResponse = ApiResponse.<Page<DecisionResponse>>builder()
                    .success(false)
                    .message("Unauthorized: No authenticated user found")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        log.info("Fetching assignments for user: {}", currentUser.getEmail());

        Page<DecisionResponse> decisions = decisionService.getAssignedDecisions(
                currentUser.getId().toString(),
                pageable,
                currentUser
        );

        if (decisions.isEmpty()) {
            log.debug("No decisions found for current user: {}.", currentUser.getEmail());
            return ResponseEntity.noContent().build();
        }

        ApiResponse<Page<DecisionResponse>> response = ApiResponse.<Page<DecisionResponse>>builder()
                .success(true)
                .data(decisions)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get decisions assigned to a specific user.
     * ADMIN: Can view for any user
     * USER: Can only view own assignments
     *
     * GET /api/v1/decision/assignments/{userId}
     */
    @GetMapping("/assignments/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<DecisionResponse>>> getUserAssignments(
            @PathVariable String userId,
            Pageable pageable,
            @AuthenticationPrincipal User currentUser
    ) {
        if (Objects.isNull(currentUser)) {
            log.error("Unauthorized access attempt - no current user found for user assignments of userId: {}", userId);
            ApiResponse<Page<DecisionResponse>> errorResponse = ApiResponse.<Page<DecisionResponse>>builder()
                    .success(false)
                    .message("Unauthorized: No authenticated user found")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        Page<DecisionResponse> decisions = decisionService.getAssignedDecisions(
                userId,
                pageable,
                currentUser
        );

        if (decisions.isEmpty()) {
            log.debug("No decisions found for user: {}.", currentUser.getEmail());
            return ResponseEntity.noContent().build();
        }

        ApiResponse<Page<DecisionResponse>> response = ApiResponse.<Page<DecisionResponse>>builder()
                .success(true)
                .data(decisions)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}