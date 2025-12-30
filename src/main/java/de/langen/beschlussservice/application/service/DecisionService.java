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
import de.langen.beschlussservice.domain.entity.Decision;
import de.langen.beschlussservice.domain.entity.DecisionStatus;
import de.langen.beschlussservice.domain.entity.Report;
import de.langen.beschlussservice.domain.entity.User;
import de.langen.beschlussservice.domain.repository.DecisionRepository;
import de.langen.beschlussservice.domain.repository.ReportRepository;
import de.langen.beschlussservice.domain.repository.UserRepository;
import de.langen.beschlussservice.infrastructure.persistance.specification.DecisionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final DecisionMapper decisionMapper;
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    @Transactional
    public DecisionResponse createDecision(CreateDecisionRequest request) {
        log.info("Creating new decision: {}", request.getTitle());

        Decision decision = decisionMapper.toEntity(request);
        var savedDecision = decisionRepository.save(decision);

        log.info("Decision created with id: {}", savedDecision.getId());
        return decisionMapper.toResponse(savedDecision, null);
    }

    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(Long id) {
        log.debug("Fetching decision with id: {}", id);

        Decision decision = decisionRepository.findById(id)
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

    @Transactional
    public DecisionResponse updateDecision(Long id, UpdateDecisionRequest request) {
        log.info("Updating decision with id: {}", id);

        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

        // Update fields manually or use mapper with @MappingTarget
        if (request.getTitle() != null) decision.setTitle(request.getTitle());
        if (request.getStatus() != null) decision.setStatus(
                DecisionStatus.valueOf(
                        request.getStatus().toUpperCase().replace("-", "_")
                )
        );
        if (request.getPrintMatter() != null) decision.setPrintMatter(request.getPrintMatter());
        if (request.getResponsibleDepartment() != null) decision.setResponsibleDepartment(request.getResponsibleDepartment());

        Decision updatedDecision = decisionRepository.save(decision);
        return decisionMapper.toResponse(updatedDecision, null);
    }

    @Transactional
    public void deleteDecision(Long id) {
        log.info("Soft deleting decision with id: {}", id);

        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));

        decisionRepository.delete(decision); // Triggers soft delete
    }

    @Transactional
    public ReportResponse createReport(Long decisionId,
                                       CreateReportRequest request,
                                       String creatorEmail) {

        log.info("Creating report for decisionId={} and year={}", decisionId, request.getYear());

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found with id: " + decisionId));

        User createdByUser = null;
        if (creatorEmail != null && !creatorEmail.isBlank()) {
            createdByUser = userRepository.findByEmail(creatorEmail).orElse(null);
        }

        Report report = reportMapper.toEntity(request);
        report.setDecision(decision);

        if (createdByUser != null) {
            report.setCreatedBy(createdByUser.getId());
        }

        Report saved = reportRepository.save(report);

        // keep bidirectional association in sync (optional but nice for aggregates)
        decision.addReport(saved);

        return reportMapper.toResponse(saved, createdByUser);
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

