package de.langen.decision_service.api.controller;

import de.langen.decision_service.api.dto.request.*;
import de.langen.decision_service.api.dto.response.*;
import de.langen.decision_service.application.service.ManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for master data management.
 * Provides CRUD operations for Topics, Departments, Committees, and Users.
 *
 * @version 2.0 - Added Topic CRUD operations
 */
@RestController
@RequestMapping("/api/v1/management")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Management API", description = "Master data management")
public class ManagementController {

    private final ManagementService managementService;

    // =========================================================================
    // Topic Endpoints
    // =========================================================================

    /**
     * Get all topics.
     * ADMIN ONLY
     *
     * GET /api/v1/management/topic
     */
    @GetMapping("/topic")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics() {
        log.debug("GET /api/v1/management/topic");

        List<TopicResponse> topics = managementService.getAllTopics();

        return ResponseEntity.ok(
                ApiResponse.<List<TopicResponse>>builder()
                        .success(true)
                        .data(topics)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Get topic by ID.
     * ADMIN ONLY
     *
     * GET /api/v1/management/topic/{id}
     */
    @GetMapping("/topic/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get topic by ID")
    public ResponseEntity<ApiResponse<TopicResponse>> getTopicById(@PathVariable String id) {
        log.debug("GET /api/v1/management/topic/{}", id);

        TopicResponse topic = managementService.getTopicById(id);

        return ResponseEntity.ok(
                ApiResponse.<TopicResponse>builder()
                        .success(true)
                        .data(topic)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Create a new topic.
     * ADMIN ONLY
     *
     * POST /api/v1/management/topic
     */
    @PostMapping("/topic")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new topic")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(
            @Valid @RequestBody CreateTopicRequest request
    ) {
        log.info("POST /api/v1/management/topic - name: {}", request.getName());

        TopicResponse topic = managementService.createTopic(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TopicResponse>builder()
                        .success(true)
                        .message("Topic created successfully")
                        .data(topic)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Update an existing topic.
     * ADMIN ONLY
     *
     * PUT /api/v1/management/topic/{id}
     */
    @PutMapping("/topic/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing topic")
    public ResponseEntity<ApiResponse<TopicResponse>> updateTopic(
            @PathVariable String id,
            @Valid @RequestBody UpdateTopicRequest request
    ) {
        log.info("PUT /api/v1/management/topic/{}", id);

        TopicResponse topic = managementService.updateTopic(id, request);

        return ResponseEntity.ok(
                ApiResponse.<TopicResponse>builder()
                        .success(true)
                        .message("Topic updated successfully")
                        .data(topic)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Delete a topic.
     * ADMIN ONLY
     *
     * DELETE /api/v1/management/topic/{id}
     */
    @DeleteMapping("/topic/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a topic")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable String id) {
        log.info("DELETE /api/v1/management/topic/{}", id);

        managementService.deleteTopic(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Topic deleted successfully")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // =========================================================================
    // Department Endpoints
    // =========================================================================

    /**
     * Get all departments.
     * ADMIN ONLY
     *
     * GET /api/v1/management/department
     */
    @GetMapping("/department")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        log.debug("GET /api/v1/management/department");

        List<DepartmentResponse> departments = managementService.getAllDepartments();

        return ResponseEntity.ok(
                ApiResponse.<List<DepartmentResponse>>builder()
                        .success(true)
                        .data(departments)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Get department by ID.
     * ADMIN ONLY
     *
     * GET /api/v1/management/department/{id}
     */
    @GetMapping("/department/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable String id) {
        log.debug("GET /api/v1/management/topic/{}", id);

        DepartmentResponse department = managementService.getDepartmentById(id);

        return ResponseEntity.ok(
                ApiResponse.<DepartmentResponse>builder()
                        .success(true)
                        .data(department)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Create a new department.
     * ADMIN ONLY
     *
     * POST /api/v1/management/department
     */
    @PostMapping("/department")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request
    ) {
        log.info("POST /api/v1/management/department - name: {}", request.getName());

        DepartmentResponse department = managementService.createDepartment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<DepartmentResponse>builder()
                        .success(true)
                        .message("Department created successfully")
                        .data(department)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Update an existing department.
     * ADMIN ONLY
     *
     * PUT /api/v1/department/topic/{id}
     */
    @PutMapping("/department/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable String id,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        log.info("PUT /api/v1/management/department/{}", id);

        DepartmentResponse department = managementService.updateDepartment(id, request);

        return ResponseEntity.ok(
                ApiResponse.<DepartmentResponse>builder()
                        .success(true)
                        .message("Topic updated successfully")
                        .data(department)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Delete a department.
     * ADMIN ONLY
     *
     * DELETE /api/v1/management/department/{id}
     */
    @DeleteMapping("/department/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a department")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable String id) {
        log.info("DELETE /api/v1/management/department/{}", id);

        managementService.deleteDepartment(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Department deleted successfully")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // =========================================================================
    // Committee Endpoints (Read-Only)
    // =========================================================================

    /**
     * Get all committees.
     * ADMIN ONLY
     *
     * GET /api/v1/management/committee
     */
    @GetMapping("/committee")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all committees")
    public ResponseEntity<ApiResponse<List<CommitteeResponse>>> getAllCommittees() {
        log.debug("GET /api/v1/management/committee");

        List<CommitteeResponse> committees = managementService.getAllCommittees();

        return ResponseEntity.ok(
                ApiResponse.<List<CommitteeResponse>>builder()
                        .success(true)
                        .data(committees)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    /**
     * Get committee by ID.
     * ADMIN ONLY
     *
     * GET /api/v1/management/committee/{id}
     */
    @GetMapping("/committee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get committee by ID")
    public ResponseEntity<ApiResponse<CommitteeResponse>> getCommitteeById(@PathVariable String id) {
        log.debug("GET /api/v1/management/committee/{}", id);

        CommitteeResponse committee = managementService.getCommitteeById(id);

        return ResponseEntity.ok(
                ApiResponse.<CommitteeResponse>builder()
                        .success(true)
                        .data(committee)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Create a new committee.
     * ADMIN ONLY
     *
     * POST /api/v1/management/committee
     */
    @PostMapping("/committee")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new committee")
    public ResponseEntity<ApiResponse<CommitteeResponse>> createCommittee(
            @Valid @RequestBody CreateCommitteeRequest request
    ) {
        log.info("POST /api/v1/management/committee - name: {}", request.getName());

        CommitteeResponse committee = managementService.createCommittee(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CommitteeResponse>builder()
                        .success(true)
                        .message("Committee created successfully")
                        .data(committee)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Update an existing committee.
     * ADMIN ONLY
     *
     * PUT /api/v1/management/committee/{id}
     */
    @PutMapping("/committee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing committee")
    public ResponseEntity<ApiResponse<CommitteeResponse>> updateCommittee(
            @PathVariable String id,
            @Valid @RequestBody UpdateCommitteeRequest request
    ) {
        log.info("PUT /api/v1/management/committee/{}", id);

        CommitteeResponse committee = managementService.updateCommittee(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CommitteeResponse>builder()
                        .success(true)
                        .message("Committee updated successfully")
                        .data(committee)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Delete a committee.
     * ADMIN ONLY
     *
     * DELETE /api/v1/management/committee/{id}
     */
    @DeleteMapping("/committee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a committee")
    public ResponseEntity<ApiResponse<Void>> deleteCommittee(@PathVariable String id) {
        log.info("DELETE /api/v1/management/committee/{}", id);

        managementService.deleteCommittee(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Committee deleted successfully")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // =========================================================================
    // User Endpoints (Read-Only)
    // =========================================================================

    /**
     * Get all users.
     * ADMIN ONLY
     *
     * GET /api/v1/management/user
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.debug("GET /api/v1/management/user");

        List<UserResponse> users = managementService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .success(true)
                        .data(users)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Get user by ID.
     * ADMIN ONLY
     *
     * GET /api/v1/management/user/{id}
     */
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        log.debug("GET /api/v1/management/user/{}", id);

        UserResponse user = managementService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .data(user)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Update an existing user.
     * ADMIN ONLY
     *
     * PUT /api/v1/management/user/{id}
     */
    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("PUT /api/v1/management/user/{}", id);

        UserResponse user = managementService.updateUser(id, request);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("User updated successfully")
                        .data(user)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Delete a user.
     * ADMIN ONLY
     *
     * DELETE /api/v1/management/user/{id}
     */
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        log.info("DELETE /api/v1/management/user/{}", id);

        managementService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User deleted successfully")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}