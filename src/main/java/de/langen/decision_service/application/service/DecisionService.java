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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

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
        log.debug("Fetching decision with id: {} by user: {}", id, currentUser.getEmail());

        Decision decision = decisionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

        // Check access
        checkDecisionAccess(decision, currentUser, "view");

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
                request.getStatus(),
                request.getTopic(),
                request.getResponsibleDepartment(),
                dateFrom,
                dateTo,
                request.getKeyword()
        );

        // USER: Add filter for assigned decisions only
        if (!currentUser.isAdmin()) {
            Specification<Decision> assigneeSpec = DecisionSpecification.hasAssignee(currentUser.getEmail());
            spec = spec.and(assigneeSpec);
            log.debug("Applied assignee filter for user: {}", currentUser.getEmail());
        }

        Page<Decision> decisions = decisionRepository.findAll(spec, pageable);
        return decisions.map(d -> decisionMapper.toResponse(d, null));
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

        // Check if user is admin
        if (!currentUser.isAdmin()) {
            log.warn("Access denied: User {} attempted to update decision", currentUser.getEmail());
            throw new AccessDeniedException("Only administrators can update decisions");
        }

        Decision decision = decisionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

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

        // Update Entity relationships
        if (request.getDecisionDepartment() != null) {
            Department department = departmentRepository.findByName(request.getDecisionDepartment())
                    .or(() -> departmentRepository.findByShortName(request.getDecisionDepartment()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + request.getDecisionDepartment()
                    ));
            decision.setResponsibleDepartment(department);
        }

        if (request.getDecisionCommittee() != null) {
            Committee committee = committeeRepository.findByName(request.getDecisionCommittee())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Committee not found: " + request.getDecisionCommittee()
                    ));
            decision.setCommittee(committee);
        }

        if (request.getTopic() != null) {
            Topic topic = topicRepository.findByName(request.getTopic())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Topic not found: " + request.getTopic()
                    ));
            decision.setTopic(topic);
        }

        // Assign to user (ADMIN can assign to anyone)
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

        Decision updatedDecision = decisionRepository.save(decision);

        User completedByUser = decision.getCompletedBy() != null
                ? userRepository.findById(decision.getCompletedBy()).orElse(null)
                : null;

        return decisionMapper.toResponse(updatedDecision, completedByUser);
    }

    /**
     * Delete a decision.
     * ADMIN ONLY
     *
     * @param id decision ID
     * @param currentUser authenticated user (must be ADMIN)
     * @throws AccessDeniedException if user is not admin
     */
    @Transactional
    public void deleteDecision(String id, User currentUser) {
        log.info("Deleting decision with id: {} by user: {}", id, currentUser.getEmail());

        // Check if user is admin
        if (!currentUser.isAdmin()) {
            log.warn("Access denied: User {} attempted to delete decision", currentUser.getEmail());
            throw new AccessDeniedException("Only administrators can delete decisions");
        }

        Decision decision = decisionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

        decisionRepository.delete(decision);
        log.info("Decision {} deleted by admin: {}", id, currentUser.getEmail());
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
}