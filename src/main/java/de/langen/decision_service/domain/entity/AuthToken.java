package de.langen.decision_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Token entity.
 * Stores UUID-based authentication tokens for users.
 *
 * @author Yacine Sghairi
 * @version 1.0
 */
@Entity
@Table(name = "auth_token", schema = "dm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Token value (UUID).
     * This is the actual token sent to client.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /**
     * User who owns this token.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Token expiration time.
     * After this time, token is no longer valid.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Token creation timestamp.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last time token was used.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Whether token has been revoked (logged out).
     */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Check if token is expired.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token is valid (not expired and not revoked).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    /**
     * Revoke this token (logout).
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Update last used timestamp.
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}