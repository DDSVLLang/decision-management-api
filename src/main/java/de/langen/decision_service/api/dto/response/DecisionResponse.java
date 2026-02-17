package de.langen.decision_service.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Decision entity.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionResponse {

    private UUID id;
    private String title;
    private String decisionDate;
    private String decisionCommittee;
    private String printMatter;
    private String decisionDepartment;
    private List<String> decisionDepartments;
    private List<DepartmentInfo> departments;
    private String decisionTopic;
    private String status;

    // NEW: Priority field
    private String priority;

    private String content;
    private String dueDate;
    private String implementationNotes;

    // NEW: Resource tracking fields
    private Integer estimatedHours;
    private Integer actualHours;

    // NEW: Assignee fields
    private String assigneeId;
    private String assigneeName;

    private List<ReportResponse> reports;
    private Boolean deleted;
    private String createdBy;
    private String completedAt;
    private String completedBy;
    private CompletedByUserInfo completedByUser;

    /**
     * Department information embedded in Decision response.
     * Similar to ReportResponse structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentInfo {
        private String id;
        private String name;
        private String shortName;
        private String description;
        private Boolean active;
        private HeadUserInfo headUser;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HeadUserInfo {
            private String id;
            private String firstName;
            private String lastName;
            private String fullName;
            private String email;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedByUserInfo {
        private String firstName;
        private String lastName;
    }
}