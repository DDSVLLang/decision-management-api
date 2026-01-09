package de.langen.beschlussservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Committee entity representing decision-making bodies.
 * Examples: STVV, Haupt- und Finanzausschuss, Bauausschuss, etc.
 *
 * @author yacine sghairi
 * @version 2.0
 */
@Entity
@Table(name = "committee", schema = "dm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Committee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(columnDefinition = "TEXT")
    private String description;

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
     * Check if committee is currently active
     * @return true if active
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Deactivate committee (soft disable)
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate committee
     */
    public void activate() {
        this.active = true;
    }
}