package de.langen.beschlussservice.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String fullName;
    private String responsibleDepartment;
    private String description;
    private Boolean active;
    private String createdAt;
    private String updatedAt;

    // Password is NEVER included in response for security
}

