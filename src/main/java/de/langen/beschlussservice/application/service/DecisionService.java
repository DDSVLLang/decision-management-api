package de.langen.beschlussservice.application.service;

import de.langen.beschlussservice.api.dto.request.CreateDecisionRequest;
import de.langen.beschlussservice.api.dto.request.CreateReportRequest;
import de.langen.beschlussservice.api.dto.request.SearchDecisionRequest;
import de.langen.beschlussservice.api.dto.request.UpdateDecisionRequest;
import de.langen.beschlussservice.api.dto.response.DecisionResponse;
import de.langen.beschlussservice.api.dto.response.ReportResponse;
import de.langen.beschlussservice.api.exception.ResourceNotFoundException;
import de.langen.beschlussservice.application.mapper.DecisionMapper;
import de.langen.beschlussservice.application.mapper.ReportMapper;
import de.langen.beschlussservice.domain.entity.*;
import de.langen.beschlussservice.domain.repository.*;
import de.langen.beschlussservice.infrastructure.persistance.specification.DecisionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service for Decision entity operations.
 * Handles business logic and entity relationship management.
 *
 * @author Backend Team
 * @version 2.1 - Added User tracking
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
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    /**
     * Create a new decision with authenticated user.
     *
     * @param request decision data
     * @param currentUser authenticated user
     * @return created decision
     */
    @Transactional
    public DecisionResponse createDecision(CreateDecisionRequest request, User currentUser) {
        log.info("Creating new decision: {} by user: {}", request.getTitle(), currentUser.getEmail());

        // Mapper handles String → Entity conversion via repositories
        Decision decision = decisionMapper.toEntity(request);

        // Set created by from authenticated user
        decision.setCreatedBy(currentUser.getId());

        var savedDecision = decisionRepository.save(decision);

        log.info("Decision created with id: {}", savedDecision.getId());
        return decisionMapper.toResponse(savedDecision, null);
    }

    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(String id) {
        log.debug("Fetching decision with id: {}", id);

        Decision decision = decisionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

        User completedByUser = decision.getCompletedBy() != null
                ? userRepository.findById(decision.getCompletedBy()).orElse(null)
                : null;

        return decisionMapper.toResponse(decision, completedByUser);
    }

    @Transactional(readOnly = true)
    public Page<DecisionResponse> searchDecisions(SearchDecisionRequest request, Pageable pageable) {
        log.debug("Searching decisions with filters: {}", request);

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

        Page<Decision> decisions = decisionRepository.findAll(spec, pageable);
        return decisions.map(d -> decisionMapper.toResponse(d, null));
    }

    /**
     * Update an existing decision with authenticated user.
     *
     * @param id decision ID
     * @param request update data
     * @param currentUser authenticated user
     * @return updated decision
     */
    @Transactional
    public DecisionResponse updateDecision(String id, UpdateDecisionRequest request, User currentUser) {
        log.info("Updating decision with id: {} by user: {}", id, currentUser.getEmail());

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

            // If marking as completed, set completed by and completed at
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

        // Update Entity relationships (String → Entity conversion)
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

        if (request.getAssigneeId() != null) {
            UUID assigneeUuid = UUID.fromString(request.getAssigneeId());
            User assignee = userRepository.findById(assigneeUuid)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getAssigneeId()
                    ));
            decision.assignTo(assignee);
        }

        Decision updatedDecision = decisionRepository.save(decision);

        // Load completedBy user if exists
        User completedByUser = decision.getCompletedBy() != null
                ? userRepository.findById(decision.getCompletedBy()).orElse(null)
                : null;

        return decisionMapper.toResponse(updatedDecision, completedByUser);
    }

    @Transactional
    public void deleteDecision(String id) {
        log.info("Soft deleting decision with id: {}", id);

        Decision decision = decisionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

        decisionRepository.delete(decision); // Triggers soft delete
    }

    /**
     * Create a report for a decision with authenticated user.
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

        Decision decision = decisionRepository.findById(UUID.fromString(decisionId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found with id: " + decisionId));

        Report report = reportMapper.toEntity(request);
        report.setDecision(decision);
        report.setCreatedBy(currentUser.getId());

        Report saved = reportRepository.save(report);

        // keep bidirectional association in sync (optional but nice for aggregates)
        decision.addReport(saved);

        return reportMapper.toResponse(saved, currentUser);
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