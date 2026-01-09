package de.langen.beschlussservice.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionResponse {

    private UUID id;
    private String title;
    private String decisionBody;
    private String decisionDate;
    private String decisionCommittee;
    private String printMatter;
    private String responsibleDepartment;
    private String topic;
    private String status;
    private String description;
    private String content;
    private String dueDate;
    private String implementationNotes;
    private List<ReportResponse> reports;
    private Boolean deleted;
    private String createdBy;
    private String completedAt;
    private String completedBy;
    private CompletedByUserInfo completedByUser;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedByUserInfo {
        private String firstName;
        private String lastName;
    }
}

