package de.langen.decision_service.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for User entity.
 * Includes department details instead of just name.
 *
 * @author Backend Team
 * @version 2.0 - Enhanced with Department object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;

    // ⭐ DEPRECATED - for backward compatibility
    private String responsibleDepartment;

    // ⭐ NEW - Full department object
    private DepartmentInfo department;

    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Department information nested in User response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DepartmentInfo {
        private String id;
        private String name;
        private String shortName;
        private String description;
        private Boolean active;
    }
}