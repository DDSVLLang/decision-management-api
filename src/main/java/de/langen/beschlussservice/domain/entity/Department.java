package de.langen.beschlussservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Department entity representing organizational departments.
 * Examples: FD 10, FD 11, FD 13, FD 20
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Entity
@Table(name = "department", schema = "dm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_user_id")
    private User headUser;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

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
     * Get display name (short name if available, otherwise full name)
     * @return display name
     */
    public String getDisplayName() {
        return shortName != null && !shortName.isBlank() ? shortName : name;
    }

    /**
     * Check if department is currently active
     * @return true if active
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Check if department has a head user assigned
     * @return true if head user exists
     */
    public boolean hasHead() {
        return headUser != null;
    }

    /**
     * Get head user's full name
     * @return full name or "Not assigned"
     */
    public String getHeadName() {
        return hasHead() ? headUser.getFullName() : "Not assigned";
    }

    /**
     * Deactivate department (soft disable)
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate department
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Assign a head user to this department
     * @param user user to assign as head
     */
    public void assignHead(User user) {
        this.headUser = user;
    }

    /**
     * Remove head user assignment
     */
    public void removeHead() {
        this.headUser = null;
    }
}