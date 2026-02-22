package de.langen.decision_service.domain.repository;

import de.langen.decision_service.domain.entity.Report;
import de.langen.decision_service.domain.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Report entity.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    /**
     * Find all reports for a decision, ordered by year descending.
     *
     * @param decisionId decision UUID
     * @return list of reports
     */
    List<Report> findByDecisionIdOrderByYearDesc(UUID decisionId);

    /**
     * Find all reports for a decision, ordered by year ascending.
     *
     * @param decisionId decision UUID
     * @return list of reports
     */
    List<Report> findByDecisionIdOrderByYearAsc(UUID decisionId);

    /**
     * Find report by decision and year.
     *
     * @param decisionId decision UUID
     * @param year year (YYYY)
     * @return report if exists
     */
    Optional<Report> findByDecisionIdAndYear(UUID decisionId, String year);

    /**
     * Check if report exists for decision and year.
     *
     * @param decisionId decision UUID
     * @param year year (YYYY)
     * @return true if exists
     */
    boolean existsByDecisionIdAndYear(UUID decisionId, String year);

    /**
     * Find all reports with specific status.
     *
     * @param status report status
     * @return list of reports
     */
    List<Report> findByStatus(ReportStatus status);

    /**
     * Find all reports created by user.
     *
     * @param userId user UUID
     * @return list of reports
     */
    List<Report> findByCreatedBy(UUID userId);

    /**
     * Find all reports for a specific year.
     *
     * @param year year (YYYY)
     * @return list of reports
     */
    List<Report> findByYear(String year);

    /**
     * Count reports for a decision.
     *
     * @param decisionId decision UUID
     * @return count
     */
    long countByDecisionId(UUID decisionId);

    /**
     * Count reports by status.
     *
     * @param status report status
     * @return count
     */
    long countByStatus(ReportStatus status);

    @Query("""
           select r
           from Report r
           join fetch r.decision d
           left join fetch d.assignee
           where r.id = :id
           """)
    Optional<Report> findByIdWithDecisionAndAssignee(@Param("id") UUID id);
}