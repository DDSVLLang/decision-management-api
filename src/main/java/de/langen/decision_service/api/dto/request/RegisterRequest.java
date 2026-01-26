package de.langen.decision_service.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required and cannot be empty")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required and cannot be empty")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    private String password;

    @NotBlank(message = "First name is required and cannot be empty")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required and cannot be empty")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    // Role is optional - defaults to USER if not provided
    @Pattern(regexp = "^(ADMIN|USER)?$", message = "Role must be either ADMIN or USER, or empty for default USER role")
    private String role;

    /**
     * Department ID (UUID).
     * References dm.department(id).
     */
    @Pattern(
            regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            message = "Department ID must be a valid UUID"
    )
    private String departmentId;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
