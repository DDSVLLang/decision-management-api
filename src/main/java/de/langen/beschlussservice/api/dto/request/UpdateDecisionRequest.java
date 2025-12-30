package de.langen.beschlussservice.api.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * All fields optional, used for PATCH/PUT updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDecisionRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    private String decisionBody;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Decision date must be in format YYYY-MM-DD")
    private String decisionDate;

    private String printMatter;

    private String responsibleDepartment;

    private String topic;

    @Pattern(regexp = "pending|in-progress|completed", message = "Status must be pending, in-progress, or completed")
    private String status;

    private String content;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Due date must be in format YYYY-MM-DD")
    private String dueDate;

    private String implementationNotes;
}

