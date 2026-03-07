package de.langen.decision_service.application.service;

import de.langen.decision_service.api.dto.request.CreateDecisionRequest;
import de.langen.decision_service.api.dto.request.SearchDecisionRequest;
import de.langen.decision_service.api.dto.request.UpdateDecisionRequest;
import de.langen.decision_service.api.dto.response.DecisionResponse;
import de.langen.decision_service.api.exception.ResourceNotFoundException;
import de.langen.decision_service.application.mapper.DecisionMapper;
import de.langen.decision_service.domain.entity.*;
import de.langen.decision_service.domain.repository.*;
import de.langen.decision_service.infrastructure.persistance.specification.DecisionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Decision entity operations with Role-Based Access Control.
 *
 * Access Rules:
 * - ADMIN: Can create, update, view all, assign decisions
 * - USER: Can only view decisions assigned to them
 *
 * @author Yacine Sghairi
 * @version 3.0 - Added RBAC
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final CommitteeRepository committeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DecisionMapper decisionMapper;

    /**
     * Create a new decision.
     * ADMIN ONLY
     *
     * @param request decision data
     * @param currentUser authenticated user (must be ADMIN)
     * @return created decision
     * @throws AccessDeniedException if user is not admin
     */
    @Transactional
    public DecisionResponse createDecision(CreateDecisionRequest request, User currentUser) {
        log.info("Creating new decision: {} by user: {}", request.getTitle(), currentUser.getEmail());

        // Check if user is admin
        if (!currentUser.isAdmin()) {
            log.warn("Access denied: User {} attempted to create decision", currentUser.getEmail());
            throw new AccessDeniedException("Only administrators can create decisions");
        }

        Decision decision = decisionMapper.toEntity(request);
        decision.setCreatedBy(currentUser.getId());

        if (request.getResponsibleDepartments() != null && !request.getResponsibleDepartments().isEmpty()) {
            List<Department> departments = request.getResponsibleDepartments().stream()
                    .map(deptName -> departmentRepository.findByName(deptName)
                            .or(() -> departmentRepository.findByShortName(deptName))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Department not found: " + deptName
                            )))
                    .collect(Collectors.toList());

            decision.setDepartments(departments);  // ⭐ VOR dem save()!
        }

        var savedDecision = decisionRepository.save(decision);

        log.info("Decision created with id: {} by admin: {}", savedDecision.getId(), currentUser.getEmail());
        return decisionMapper.toResponse(savedDecision, null);
    }

    /**
     * Get decision by ID.
     * ADMIN: Can view any decision
     * USER: Can only view decisions assigned to them
     *
     * @param id decision UUID
     * @param currentUser authenticated user
     * @return decision
     * @throws AccessDeniedException if user doesn't have access
     */
    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(String id, User currentUser) {
        Decision decision;

        if (currentUser.isAdmin()) {
            decision = decisionRepository.findFirstById(UUID.fromString(id))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Decision not found with id: " + id
                    ));
        } else {
            decision = decisionRepository.findByIdAndDeletedFalse(UUID.fromString(id))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Decision not found with id: " + id
                    ));
            checkDecisionAccess(decision, currentUser, "view");
        }

        User completedByUser = decision.getCompletedBy() != null
                ? userRepository.findById(decision.getCompletedBy()).orElse(null)
                : null;

        return decisionMapper.toResponse(decision, completedByUser);
    }

    /**
     * Search decisions with access control.
     * ADMIN: Returns all decisions
     * USER: Returns only decisions assigned to them
     *
     * @param request search filters
     * @param pageable pagination
     * @param currentUser authenticated user
     * @return page of decisions
     */
    @Transactional(readOnly = true)
    public Page<DecisionResponse> searchDecisions(
            SearchDecisionRequest request,
            Pageable pageable,
            User currentUser
    ) {
        log.debug("Searching decisions by user: {} (role: {})",
                currentUser.getEmail(), currentUser.getRole());

        LocalDate dateFrom = parseDate(request.getDecisionDateFrom());
        LocalDate dateTo = parseDate(request.getDecisionDateTo());

        Specification<Decision> spec = DecisionSpecification.withFilters(
                currentUser.isAdmin(),
                currentUser.getId().toString(),
                request.getStatus(),
                request.getCanBeCompleted(),
                request.getPrintMatter(),
                request.getTopic(),
                request.getDepartment(),
                request.getCommittee(),
                dateFrom,
                dateTo,
                request.getKeyword(),
                Boolean.parseBoolean(request.getDeleted()),
                request.getPrintMatterYear(),
                request.getPrintMatterElectionPeriod()
        );

        // USER: Add filter for assigned decisions only
        if (!currentUser.isAdmin()) {
            Department userDepartment = userRepository.findById(currentUser.getId()).map(User::getDepartment).orElse(null);
            if (userDepartment == null) return Page.empty();
            Specification<Decision> assigneeSpec = DecisionSpecification.hasDepartments(List.of(userDepartment.getName()));
            spec = spec.and(assigneeSpec);
            log.debug("Applied assignee filter for user: {}", currentUser.getEmail());
        }

        // ⭐ Fetch ALL results (no pagination yet) for custom sorting
        List<Decision> allDecisions = decisionRepository.findAll(spec);

        //  Apply custom sort
        List<Decision> sorted = allDecisions.stream()
                .sorted(createPrintMatterComparator())
                .collect(Collectors.toList());

        //  Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        List<Decision> pageContent = sorted.subList(start, end);

        Page<Decision> page = new PageImpl<>(pageContent, pageable, sorted.size());

        return page.map(d -> decisionMapper.toResponse(d, null));
    }

    /**
     * Update an existing decision.
     * ADMIN ONLY
     *
     * @param id decision ID
     * @param request update data
     * @param currentUser authenticated user (must be ADMIN)
     * @return updated decision
     * @throws AccessDeniedException if user is not admin
     */
    @Transactional
    public DecisionResponse updateDecision(String id, UpdateDecisionRequest request, User currentUser) {
        log.info("Updating decision with id: {} by user: {}", id, currentUser.getEmail());

        Decision decision = decisionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));


        // Check if user is admin
        if (!currentUser.isAdmin()) {
            if (Objects.nonNull(request.isCanBeCompleted())) {
                decision.setCanBeCompleted(request.isCanBeCompleted());
            }
            //  log.warn("Access denied: User {} attempted to update decision", currentUser.getEmail());
            //  throw new AccessDeniedException("Only administrators can update decisions");
        } else {
            // Update simple fields
            if (request.getTitle() != null) {
                decision.setTitle(request.getTitle());
            }

            if (request.getStatus() != null) {
                DecisionStatus newStatus = DecisionStatus.valueOf(
                        request.getStatus().toUpperCase().replace("-", "_")
                );

                if (newStatus == DecisionStatus.COMPLETED) {
                    decision.markAsCompleted(currentUser.getId());
                } else {
                    decision.setStatus(newStatus);
                }
            }

            if (request.getPriority() != null) {
                decision.setPriority(
                        DecisionPriority.valueOf(
                                request.getPriority().toUpperCase().replace("-", "_")
                        )
                );
            }

            if (request.getPrintMatter() != null) {
                decision.setPrintMatter(request.getPrintMatter());
            }

            if (request.getContent() != null) {
                decision.setContent(request.getContent());
            }

            if (request.getDueDate() != null) {
                decision.setDueDate(parseDate(request.getDueDate()));
            }

            if (request.getImplementationNotes() != null) {
                decision.setImplementationNotes(request.getImplementationNotes());
            }

            if (request.getEstimatedHours() != null) {
                decision.setEstimatedHours(request.getEstimatedHours());
            }

            if (request.getActualHours() != null) {
                decision.setActualHours(request.getActualHours());
            }

            // Update Topic
            if (request.getTopic() != null) {
                Topic topic = topicRepository.findByName(request.getTopic())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Topic not found: " + request.getTopic()
                        ));
                decision.setTopic(topic);
            }

            // Update Committee
            if (request.getDecisionCommittee() != null) {
                Committee committee = committeeRepository.findByName(request.getDecisionCommittee())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Committee not found: " + request.getDecisionCommittee()
                        ));
                decision.setCommittee(committee);
            }

            // ⭐ Update Departments - WICHTIG: REPLACE ALL
            if (request.getResponsibleDepartments() != null && !request.getResponsibleDepartments().isEmpty()) {
                List<Department> departments = request.getResponsibleDepartments().stream()
                        .map(deptName -> departmentRepository.findByName(deptName)
                                .or(() -> departmentRepository.findByShortName(deptName))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Department not found: " + deptName
                                )))
                        .collect(Collectors.toList());

                // ⭐ CRITICAL: Clear existing + set new
                decision.clearDepartments();
                decision.setDepartments(departments);

                log.info("Updated departments for decision {}: {}", id,
                        departments.stream().map(Department::getShortName).collect(Collectors.toList()));
            }
            // Backward compatibility: single department
            else if (request.getResponsibleDepartment() != null && !request.getResponsibleDepartment().isBlank()) {
                Department department = departmentRepository.findByName(request.getResponsibleDepartment())
                        .or(() -> departmentRepository.findByShortName(request.getResponsibleDepartment()))
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Department not found: " + request.getResponsibleDepartment()
                        ));

                decision.clearDepartments();
                decision.addDepartment(department);

                log.info("Updated department for decision {}: {}", id, department.getShortName());
            }

            // Update Assignee
            if (request.getAssigneeId() != null) {
                UUID assigneeUuid = UUID.fromString(request.getAssigneeId());
                User assignee = userRepository.findById(assigneeUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "User not found with id: " + request.getAssigneeId()
                        ));
                decision.assignTo(assignee);
                log.info("Decision {} assigned to user: {} by admin: {}",
                        id, assignee.getEmail(), currentUser.getEmail());
            }
        }



        Decision updatedDecision = decisionRepository.save(decision);

        User completedByUser = decision.getCompletedBy() != null
                ? userRepository.findById(decision.getCompletedBy()).orElse(null)
                : null;

        return decisionMapper.toResponse(updatedDecision, completedByUser);
    }

    /**
     * Soft-delete a decision by setting deleted = true.
     * ADMIN ONLY
     *
     * @param id          decision ID
     * @param currentUser authenticated user (must be ADMIN)
     * @throws AccessDeniedException   if user is not admin
     * @throws ResourceNotFoundException if decision not found
     */
    @Transactional
    public void deleteDecision(String id, User currentUser) {
        log.info("Soft-deleting decision with id: {} by user: {}", id, currentUser.getEmail());

        if (!currentUser.isAdmin()) {
            log.warn("Access denied: User {} attempted to delete decision", currentUser.getEmail());
            throw new AccessDeniedException("Only administrators can delete decisions");
        }

        Decision decision = decisionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

        if (decision.getDeleted()) {
            throw new IllegalStateException("Decision with id " + id + " is already deleted");
        }

        decision.setDeleted(true);
        decisionRepository.save(decision);

        log.info("Decision {} soft-deleted by admin: {}", id, currentUser.getEmail());
    }


    /**
     * Get all decisions assigned to a specific user.
     * ADMIN: Can view for any user
     * USER: Can only view their own assignments
     *
     * @param userId user UUID
     * @param currentUser authenticated user
     * @return list of assigned decisions
     * @throws AccessDeniedException if user tries to view other user's decisions
     */
    @Transactional(readOnly = true)
    public Page<DecisionResponse> getAssignedDecisions(
            String userId,
            Pageable pageable,
            User currentUser
    ) {
        log.debug("Fetching assigned decisions for user: {} by: {}", userId, currentUser.getEmail());

        UUID targetUserId = UUID.fromString(userId);

        // Check access: User can only view own assignments, Admin can view any
        if (!currentUser.isAdmin() && !currentUser.getId().equals(targetUserId)) {
            log.warn("Access denied: User {} attempted to view assignments for user {}",
                    currentUser.getEmail(), userId);
            throw new AccessDeniedException("You can only view your own assigned decisions");
        }

        // Find user
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Specification<Decision> spec = DecisionSpecification.hasAssignee(targetUser.getEmail());
        Page<Decision> decisions = decisionRepository.findAll(spec, pageable);

        return decisions.map(d -> decisionMapper.toResponse(d, null));
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Check if user has access to a decision.
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

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.warn("Invalid date format: {}", dateStr);
            return null;
        }
    }

    /**
     * Creates custom sort that pushes UNKNOWN topics to the end.
     * Preserves user-requested sorting as primary sort.
     */
    private Pageable createCustomSort(Pageable pageable) {
        Sort userSort = pageable.getSort();

        // Custom sort: topic.name != 'UNKNOWN' DESC (true first, false last)
        // This means: normal topics (true) come first, UNKNOWN (false) comes last
        Sort unknownLast = Sort.by(
                Sort.Order.asc("topicName")  // Computed in Specification
        );

        // Combine: user sort takes precedence, then UNKNOWN-last
        Sort combinedSort = userSort.isSorted()
                ? userSort.and(unknownLast)
                : unknownLast;

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                combinedSort
        );
    }

    /**
     * Create comparator for natural PrintMatter sorting.
     *
     * Sort order:
     * 1. UNKNOWN topics last
     * 2. PrintMatter number1 ASC
     * 3. PrintMatter number2 ASC
     * 4. PrintMatter year ASC
     * 5. DecisionDate DESC
     */
    private Comparator<Decision> createPrintMatterComparator() {
        return Comparator
                // 1. UNKNOWN topics last
                .comparing((Decision d) ->
                        "UNKNOWN".equals(d.getTopic() != null ? d.getTopic().getName() : "")
                )
                // 2. PrintMatter year ASC
                .thenComparing(d -> extractPrintMatterYear(d.getPrintMatter()))
                // 3. PrintMatter number1 ASC
                .thenComparing(d -> extractPrintMatterNumber1(d.getPrintMatter()))
                // 4. PrintMatter number2 ASC
                .thenComparing(d -> extractPrintMatterNumber2(d.getPrintMatter()));
    }

    /**
     * Extract first number from printMatter.
     * Examples:
     *   "287-10/XVI/10" → 287
     *   "234/X/26"      → 234
     */
    private Integer extractPrintMatterNumber1(String printMatter) {
        if (printMatter == null || printMatter.isBlank()) {
            return Integer.MAX_VALUE;  // Sort nulls/empty last
        }

        try {
            // Split by '/' first, then by '-'
            String firstPart = printMatter.split("/")[0];
            String number1Str = firstPart.split("-")[0];
            return Integer.parseInt(number1Str.trim());
        } catch (Exception e) {
            log.warn("Failed to extract number1 from printMatter: {}", printMatter);
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Extract second number from printMatter (after '-').
     * Examples:
     *   "287-10/XVI/10" → 10
     *   "234/X/26"      → 0 (no second number)
     */
    private Integer extractPrintMatterNumber2(String printMatter) {
        if (printMatter == null || printMatter.isBlank()) {
            return 0;
        }

        try {
            String firstPart = printMatter.split("/")[0];
            String[] parts = firstPart.split("-");

            if (parts.length > 1) {
                return Integer.parseInt(parts[1].trim());
            }
            return 0;  // No second number
        } catch (Exception e) {
            log.warn("Failed to extract number2 from printMatter: {}", printMatter);
            return 0;
        }
    }

    /**
     * Extract year from printMatter (last 2 digits after last '/').
     * Examples:
     *   "287-10/XVI/10" → 10
     *   "234/X/26"      → 26
     */
    private Integer extractPrintMatterYear(String printMatter) {
        if (printMatter == null || printMatter.isBlank()) {
            return 0;
        }

        try {
            String[] parts = printMatter.split("/");
            String yearStr = parts[parts.length - 1].trim();
            return Integer.parseInt(yearStr);
        } catch (Exception e) {
            log.warn("Failed to extract year from printMatter: {}", printMatter);
            return 0;
        }
    }

}