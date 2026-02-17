package de.langen.decision_service.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used to create a Report associated with a Decision.
 * decisionId typically comes from the path variable in the controller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {

    @NotBlank(message = "Year is required")
    @Pattern(regexp = "\\d{4}", message = "Year must be a 4-digit string")
    private String year;

    @NotBlank(message = "Content is required")
    @Size(min = 1, message = "Content must not be empty")
    private String content;

    @Pattern(regexp = "Q1|Q2|Q3|Q4",
            message = "Expected completion quarter must be one of Q1, Q2, Q3, Q4")
    private String expectedCompletionQuarter;
}
