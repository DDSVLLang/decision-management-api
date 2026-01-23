package de.langen.decision_service.api.dto.response;

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
public class DepartmentResponse {

    private String id;
    private String name;
    private String shortName;
    private String description;
    private String headUserId;
    private String headUserName;
    private Boolean active;
    private String createdAt;
    private String updatedAt;
}