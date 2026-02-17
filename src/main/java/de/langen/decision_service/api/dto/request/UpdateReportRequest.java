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

    @Pattern(regexp = "\\d{4}", message = "Year must be in format YYYY")
    private String year;

    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @Pattern(regexp = "Q[1-4]", message = "Expected completion quarter must be Q1, Q2, Q3, or Q4")
    private String expectedCompletionQuarter;

    @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|REJECTED", message = "Status must be DRAFT, SUBMITTED, APPROVED, or REJECTED")
    private String status;
}