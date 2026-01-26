package de.langen.decision_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * User entity representing system users with authentication capabilities.
 * Implements UserDetails for Spring Security integration.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Entity
@Table(name = "user", schema = "dm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // BCrypt hashed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

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
    // Deprecated Field (for backward compatibility during migration)
    // =========================================================================

    @Column(name = "responsible_department", length = 255)
    @Deprecated
    private String responsibleDepartment;

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
    // UserDetails Implementation (Spring Security)
    // =========================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    // =========================================================================
    // Business Methods
    // =========================================================================

    /**
     * Get user's full name
     * @return formatted full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if user has admin role
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Check if user has user role
     * @return true if user is regular user
     */
    public boolean isRegularUser() {
        return role == UserRole.USER;
    }
}