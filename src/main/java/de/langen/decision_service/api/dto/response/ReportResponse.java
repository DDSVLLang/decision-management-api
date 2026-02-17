package de.langen.decision_service.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for Report entity.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    private UUID id;
    private UUID decisionId;
    private String title;
    private String year;
    private String content;
    private String expectedCompletionQuarter;
    private String status;
    private String createdAt;
    private String updatedAt;
    private CreatedByUserInfo createdByUser;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedByUserInfo {
        private String firstName;
        private String lastName;
        private String email;
    }
}