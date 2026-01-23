package de.langen.decision_service.api.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing Decision.
 * All fields are optional.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDecisionRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Decision date must be in format YYYY-MM-DD")
    private String decisionDate;

    private String printMatter;

    private String decisionCommittee;

    private String decisionDepartment;

    private String topic;

    @Pattern(regexp = "pending|in-progress|completed", message = "Status must be pending, in-progress, or completed")
    private String status;

    // NEW: Priority field
    @Pattern(regexp = "low|medium|high|urgent", message = "Priority must be low, medium, high, or urgent")
    private String priority;

    private String content;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Due date must be in format YYYY-MM-DD")
    private String dueDate;

    private String implementationNotes;

    // NEW: Resource tracking fields
    private Integer estimatedHours;

    private Integer actualHours;

    private String assigneeId; // UUID as string
}