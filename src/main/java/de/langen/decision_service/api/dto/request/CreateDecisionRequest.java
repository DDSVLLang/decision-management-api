package de.langen.decision_service.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new Decision.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDecisionRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @NotBlank(message = "Decision date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Decision date must be in format YYYY-MM-DD")
    private String decisionDate;

    private String printMatter;

    @NotBlank(message = "Decision committee is required")
    private String decisionCommittee;

    @NotBlank(message = "Responsible department is required")
    private String responsibleDepartment;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Status is required")
    @Pattern(regexp = "pending|in-progress|completed", message = "Status must be pending, in-progress, or completed")
    private String status;

    // NEW: Priority field
    @Pattern(regexp = "low|medium|high|urgent", message = "Priority must be low, medium, high, or urgent")
    private String priority; // Optional, defaults to MEDIUM in mapper

    @NotBlank(message = "Content is required")
    private String content;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Due date must be in format YYYY-MM-DD")
    private String dueDate;

    private String implementationNotes;

    // NEW: Optional fields for resource tracking
    private Integer estimatedHours;

    private Integer actualHours;

    private String assigneeId; // UUID as string
}