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
    @Pattern(
            regexp = "^(19|20)\\d{2}/(19|20)\\d{2}$",
            message = "Year must be in format YYYY/YYYY (e.g., 2024/2025)"
    )
    private String year;

    @NotBlank(message = "Content is required")
    @Size(min = 1, message = "Content must not be empty")
    private String content;

    @Pattern(
            regexp = "^(19|20)\\d{2}/Q[1-4]$",
            message = "Expected completion quarter must be in format YYYY/Q1, YYYY/Q2, YYYY/Q3 or YYYY/Q4 (e.g., 2026/Q1)"
    )
    private String expectedCompletionQuarter;
}
