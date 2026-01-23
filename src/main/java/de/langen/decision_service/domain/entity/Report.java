package de.langen.decision_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Report entity representing status reports for decisions.
 * Each decision can have multiple reports (typically one per year).
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Entity
@Table(name = "report", schema = "dm",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_decision_year",
                        columnNames = {"decision_id", "year"}
                )
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(length = 255)
    private String title;

    @Column(nullable = false, length = 4)
    private String year;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "expected_completion_quarter", length = 10)
    private String expectedCompletionQuarter;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================================================================
    // Lifecycle Callbacks
    // =========================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // Business Methods
    // =========================================================================

    /**
     * Check if report is in draft status
     * @return true if draft
     */
    public boolean isDraft() {
        return status == ReportStatus.DRAFT;
    }

    /**
     * Check if report is submitted
     * @return true if submitted
     */
    public boolean isSubmitted() {
        return status == ReportStatus.SUBMITTED;
    }

    /**
     * Check if report is approved
     * @return true if approved
     */
    public boolean isApproved() {
        return status == ReportStatus.APPROVED;
    }

    /**
     * Check if report is rejected
     * @return true if rejected
     */
    public boolean isRejected() {
        return status == ReportStatus.REJECTED;
    }

    /**
     * Submit report for approval
     */
    public void submit() {
        if (status != ReportStatus.DRAFT) {
            throw new IllegalStateException("Can only submit draft reports");
        }
        this.status = ReportStatus.SUBMITTED;
    }

    /**
     * Approve report
     */
    public void approve() {
        if (status != ReportStatus.SUBMITTED) {
            throw new IllegalStateException("Can only approve submitted reports");
        }
        this.status = ReportStatus.APPROVED;
    }

    /**
     * Reject report
     */
    public void reject() {
        if (status != ReportStatus.SUBMITTED) {
            throw new IllegalStateException("Can only reject submitted reports");
        }
        this.status = ReportStatus.REJECTED;
    }

    /**
     * Reset to draft status
     */
    public void resetToDraft() {
        this.status = ReportStatus.DRAFT;
    }

    /**
     * Get quarter as integer (1-4)
     * @return quarter number or null if not set
     */
    public Integer getQuarterNumber() {
        if (expectedCompletionQuarter == null || expectedCompletionQuarter.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(expectedCompletionQuarter.substring(1));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if report is for current year
     * @return true if year matches current year
     */
    public boolean isCurrentYear() {
        return String.valueOf(LocalDateTime.now().getYear()).equals(year);
    }
}