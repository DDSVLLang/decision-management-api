package de.langen.decision_service.api.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing Report.
 * All fields are optional.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Pattern(
            regexp = "^(19|20)\\d{2}/(19|20)\\d{2}$",
            message = "Year must be in format YYYY/YYYY (e.g., 2024/2025)"
    )
    private String year;

    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @Pattern(
            regexp = "^(19|20)\\d{2}/Q[1-4]$",
            message = "Expected completion quarter must be in format YYYY/Q1, YYYY/Q2, YYYY/Q3 or YYYY/Q4 (e.g., 2026/Q1)"
    )
    private String expectedCompletionQuarter;

    @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|REJECTED", message = "Status must be DRAFT, SUBMITTED, APPROVED, or REJECTED")
    private String status;
}