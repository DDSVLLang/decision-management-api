package de.langen.decision_service.api.controller;

import de.langen.decision_service.api.dto.request.LoginRequest;
import de.langen.decision_service.api.dto.request.RegisterRequest;
import de.langen.decision_service.api.dto.response.ApiResponse;
import de.langen.decision_service.api.dto.response.AuthResponse;
import de.langen.decision_service.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for Authentication.
 * Handles login, registration, and logout.
 *
 * @author Backend Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * User login.
     *
     * POST /api/v1/auth/login
     *
     * @param request login credentials
     * @return authentication response with token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request for email: {}", request.getEmail());

        AuthResponse authResponse = authService.login(request);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * User registration.
     *
     * POST /api/v1/auth/register
     *
     * @param request registration data
     * @return authentication response with token
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Registration request for email: {}", request.getEmail());

        AuthResponse authResponse = authService.register(request);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Registration successful")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User logout.
     * Revokes the provided token.
     *
     * POST /api/v1/auth/logout
     *
     * @param authorization Authorization header with Bearer token
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorization
    ) {
        String token = extractToken(authorization);

        log.info("Logout request");
        authService.logout(token);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user info.
     *
     * GET /api/v1/auth/me
     *
     * @param authorization Authorization header with Bearer token
     * @return current user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(
            @RequestHeader("Authorization") String authorization
    ) {
        String token = extractToken(authorization);

        AuthResponse.UserInfo userInfo = authService.getCurrentUser(token);

        ApiResponse<AuthResponse.UserInfo> response = ApiResponse.<AuthResponse.UserInfo>builder()
                .success(true)
                .data(userInfo)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Extract token from Authorization header.
     * Expects format: "Bearer <token>"
     *
     * @param authorization authorization header
     * @return token string
     */
    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        return authorization.substring(7);
    }
}