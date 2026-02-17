package de.langen.decision_service.application.service;

import de.langen.decision_service.api.dto.request.LoginRequest;
import de.langen.decision_service.api.dto.request.RegisterRequest;
import de.langen.decision_service.api.dto.response.AuthResponse;
import de.langen.decision_service.api.exception.ResourceNotFoundException;
import de.langen.decision_service.domain.entity.AuthToken;
import de.langen.decision_service.domain.entity.Department;
import de.langen.decision_service.domain.entity.User;
import de.langen.decision_service.domain.entity.UserRole;
import de.langen.decision_service.domain.repository.AuthTokenRepository;
import de.langen.decision_service.domain.repository.DepartmentRepository;
import de.langen.decision_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Authentication Service.
 * Handles user login, registration, and token management.
 *
 * @author Yacine Sghairi
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // Token expiration: 30 days
    private static final long TOKEN_EXPIRATION_DAYS = 30;

    /**
     * Authenticate user and generate token.
     *
     * @param request login credentials
     * @return authentication response with token
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if user is active
        if (!user.getActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // Generate token
        AuthToken authToken = createToken(user);

        log.info("User logged in successfully: {}", user.getEmail());

        return buildAuthResponse(authToken, user);
    }

    /**
     * Register new user.
     *
     * @param request registration data
     * @return authentication response with token
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        // Validate and parse role
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        Department department = null;
        if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
            try {
                UUID deptUuid = UUID.fromString(request.getDepartmentId());
                department = departmentRepository.findById(deptUuid)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Department not found with id: " + request.getDepartmentId()
                        ));
                log.debug("Mapped departmentId {} to department: {}",
                        request.getDepartmentId(), department.getName());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid department ID format: " + request.getDepartmentId());
            }
        }

        // Create user with BCrypt password
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .department(department)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with role: {}", savedUser.getEmail(), savedUser.getRole());

        // Generate authentication token
        AuthToken token = AuthToken.builder()
                .token(UUID.randomUUID().toString())
                .user(savedUser)
                .expiresAt(LocalDateTime.now().plusDays(TOKEN_EXPIRATION_DAYS))
                .revoked(false)
                .build();

        AuthToken savedToken = authTokenRepository.save(token);

        // Build response with UserInfo including department
        return AuthResponse.builder()
                .token(savedToken.getToken())
                .tokenType("Bearer")
                .expiresIn(Duration.ofDays(TOKEN_EXPIRATION_DAYS).toSeconds())
                .user(mapUserToUserInfo(savedUser))
                .build();
    }

    /**
     * Logout user by revoking token.
     *
     * @param token token string
     */
    @Transactional
    public void logout(String token) {
        log.info("Logout attempt with token");

        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        authToken.revoke();
        authTokenRepository.save(authToken);

        log.info("User logged out successfully: {}", authToken.getUser().getEmail());
    }

    /**
     * Validate token and return user.
     *
     * @param token token string
     * @return user if token is valid
     */
    @Transactional
    public User validateToken(String token) {
        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));

        if (!authToken.isValid()) {
            throw new BadCredentialsException("Token expired or revoked");
        }

        // Update last used timestamp
        authToken.updateLastUsed();
        authTokenRepository.save(authToken);

        return authToken.getUser();
    }

    /**
     * Get current authenticated user info.
     *
     * @param token token string
     * @return user info
     */
    @Transactional(readOnly = true)
    public AuthResponse.UserInfo getCurrentUser(String token) {
        User user = validateToken(token);
        return buildUserInfo(user);
    }

    /**
     * Revoke all tokens for a user (logout from all devices).
     *
     * @param userId user UUID
     */
    @Transactional
    public void logoutAllDevices(UUID userId) {
        log.info("Revoking all tokens for user: {}", userId);
        int count = authTokenRepository.revokeAllUserTokens(userId);
        log.info("Revoked {} tokens", count);
    }

    /**
     * Clean up expired tokens (scheduled task).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");
        int count = authTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Deleted {} expired tokens", count);
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Create new authentication token for user.
     *
     * @param user user
     * @return created token
     */
    private AuthToken createToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(TOKEN_EXPIRATION_DAYS);

        AuthToken token = AuthToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return authTokenRepository.save(token);
    }

    /**
     * Build authentication response.
     *
     * @param token auth token
     * @param user user
     * @return auth response
     */
    private AuthResponse buildAuthResponse(AuthToken token, User user) {
        long expiresIn = java.time.Duration.between(
                LocalDateTime.now(),
                token.getExpiresAt()
        ).getSeconds();

        return AuthResponse.builder()
                .token(token.getToken())
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(buildUserInfo(user))
                .build();
    }

    /**
     * Build user info DTO.
     *
     * @param user user
     * @return user info
     */
    private AuthResponse.UserInfo buildUserInfo(User user) {
        AuthResponse.UserInfo.UserInfoBuilder builder = AuthResponse.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().getValue());

        if (user.getDepartment() != null) {
            Department userDepartment = user.getDepartment();

            Department department = departmentRepository.findById(userDepartment.getId())
                    .orElse(null);

            if (department != null) {
                builder.department(AuthResponse.UserInfo.DepartmentInfo.builder()
                        .id(department.getId().toString())
                        .name(department.getName())
                        .shortName(department.getShortName())
                        .build());
            } else {
                builder.department(AuthResponse.UserInfo.DepartmentInfo.builder()
                        .id(userDepartment.getId().toString())
                        .name(userDepartment.getName())
                        .shortName(userDepartment.getShortName())
                        .build());

                log.warn("Department with ID {} not found in database, using cached data",
                        userDepartment.getId());
            }
        }

        return builder.build();
    }

    /**
     * Parse role string to enum.
     *
     * @param role role string
     * @return user role enum
     */
    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return UserRole.USER; // Default
        }
        return UserRole.valueOf(role.toUpperCase());
    }

    /**
     * Validate mandatory fields for user creation.
     *
     * @param request registration request
     * @throws IllegalArgumentException if mandatory fields are missing
     */
    private void validateMandatoryFields(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is mandatory");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is mandatory");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is mandatory");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is mandatory");
        }
    }

    /**
     * Parse role string to enum with USER as default.
     * Ensures role is always set for user creation.
     *
     * @param role role string (can be null or empty)
     * @return user role enum, defaults to USER
     */
    private UserRole parseRoleWithDefault(String role) {
        if (role == null || role.trim().isEmpty()) {
            log.debug("No role provided, defaulting to USER");
            return UserRole.USER; // Default role
        }

        try {
            UserRole parsedRole = UserRole.valueOf(role.trim().toUpperCase());
            log.debug("Role set to: {}", parsedRole.getValue());
            return parsedRole;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role '{}' provided, defaulting to USER", role);
            return UserRole.USER; // Fallback to default
        }
    }

    /**
     * Map User entity to UserInfo DTO with department details.
     */
    private AuthResponse.UserInfo mapUserToUserInfo(User user) {
        AuthResponse.UserInfo.UserInfoBuilder builder = AuthResponse.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().getValue());

        // ⭐ Map department to both old and new format
        if (user.getDepartment() != null) {
            Department dept = user.getDepartment();

            // New format: full object
            builder.department(
                    AuthResponse.UserInfo.DepartmentInfo.builder()
                            .id(dept.getId().toString())
                            .name(dept.getName())
                            .shortName(dept.getShortName())
                            .build()
            );

            // Old format: string (backward compatibility)
            builder.responsibleDepartment(dept.getName());
        }

        return builder.build();
    }

}