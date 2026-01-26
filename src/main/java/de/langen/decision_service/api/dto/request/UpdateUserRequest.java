package de.langen.decision_service.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    /**
     * Department ID (UUID).
     * References dm.department table.
     */
    @Pattern(
            regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            message = "Department ID must be a valid UUID"
    )
    private String departmentId;


    @Pattern(regexp = "ADMIN|USER", message = "Role must be either ADMIN or USER")
    private String role;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Boolean active;
}