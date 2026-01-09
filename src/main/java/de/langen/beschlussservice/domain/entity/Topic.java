package de.langen.beschlussservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Topic entity representing decision categories/themes.
 * Examples: Verwaltung, Straßenbahn, Stadtentwicklung, etc.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Entity
@Table(name = "topic", schema = "dm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

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
     * Check if topic is currently active
     * @return true if active
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Deactivate topic (soft disable)
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate topic
     */
    public void activate() {
        this.active = true;
    }
}