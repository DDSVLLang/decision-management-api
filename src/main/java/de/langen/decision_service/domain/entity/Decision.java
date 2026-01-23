package de.langen.decision_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Decision entity representing political/administrative decisions.
 * Central entity that connects to Topic, Committee, Department, and User.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Entity
@Table(name = "decision", schema = "dm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE dm.decision SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "decision_date", nullable = false)
    private LocalDate decisionDate;

    @Column(name = "print_matter", length = 255)
    private String printMatter;

    // =========================================================================
    // Foreign Key Relationships (OPTIMIZED - using entities instead of strings)
    // =========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id", nullable = false)
    private Committee committee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department responsibleDepartment;

    // =========================================================================
    // Core Decision Fields
    // =========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DecisionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private DecisionPriority priority = DecisionPriority.MEDIUM;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "implementation_notes", columnDefinition = "TEXT")
    private String implementationNotes;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "actual_hours")
    private Integer actualHours;

    // =========================================================================
    // User Relationships
    // =========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "created_by")
    private UUID createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by", insertable = false, updatable = false)
    private User completedByUser;

    @Column(name = "completed_by")
    private UUID completedBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // =========================================================================
    // Reports Collection
    // =========================================================================

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Report> reports = new ArrayList<>();

    // =========================================================================
    // Audit and Soft Delete
    // =========================================================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

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
    // Report Management Methods
    // =========================================================================

    /**
     * Add a report to this decision
     * @param report report to add
     */
    public void addReport(Report report) {
        reports.add(report);
        report.setDecision(this);
    }

    /**
     * Remove a report from this decision
     * @param report report to remove
     */
    public void removeReport(Report report) {
        reports.remove(report);
        report.setDecision(null);
    }

    /**
     * Get count of reports for this decision
     * @return number of reports
     */
    public int getReportCount() {
        return reports != null ? reports.size() : 0;
    }

    // =========================================================================
    // Business Logic Methods
    // =========================================================================

    /**
     * Check if decision is overdue
     * @return true if past due date and not completed
     */
    public boolean isOverdue() {
        return dueDate != null
                && LocalDate.now().isAfter(dueDate)
                && status != DecisionStatus.COMPLETED;
    }

    /**
     * Check if decision is completed
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == DecisionStatus.COMPLETED;
    }

    /**
     * Check if decision is pending
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status == DecisionStatus.PENDING;
    }

    /**
     * Check if decision is in progress
     * @return true if status is IN_PROGRESS
     */
    public boolean isInProgress() {
        return status == DecisionStatus.IN_PROGRESS;
    }

    /**
     * Mark decision as completed
     * @param completedByUserId ID of user completing the decision
     */
    public void markAsCompleted(UUID completedByUserId) {
        this.status = DecisionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.completedBy = completedByUserId;
    }

    /**
     * Assign decision to a user
     * @param user user to assign
     */
    public void assignTo(User user) {
        this.assignee = user;
    }

    /**
     * Unassign decision
     */
    public void unassign() {
        this.assignee = null;
    }

    /**
     * Check if decision has an assignee
     * @return true if assigned
     */
    public boolean isAssigned() {
        return assignee != null;
    }

    /**
     * Calculate completion percentage based on actual vs estimated hours
     * @return percentage (0-100), or null if not applicable
     */
    public Integer getCompletionPercentage() {
        if (estimatedHours == null || estimatedHours == 0) {
            return null;
        }
        if (actualHours == null) {
            return 0;
        }
        int percentage = (int) ((actualHours * 100.0) / estimatedHours);
        return Math.min(percentage, 100); // Cap at 100%
    }

    /**
     * Check if decision is high priority
     * @return true if priority is HIGH or URGENT
     */
    public boolean isHighPriority() {
        return priority == DecisionPriority.HIGH || priority == DecisionPriority.URGENT;
    }
}