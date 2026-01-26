package de.langen.decision_service.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication (login/register).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String role;
        private DepartmentInfo department;
        private String responsibleDepartment;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DepartmentInfo {
            private String id;
            private String name;
            private String shortName;
        }
    }


}