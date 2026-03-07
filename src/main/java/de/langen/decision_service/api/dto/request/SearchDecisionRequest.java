package de.langen.decision_service.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDecisionRequest {
    private String status;
    private String canBeCompleted;
    private String printMatter;
    private String topic;
    private String department;
    private String committee;
    private String decisionDateFrom;
    private String decisionDateTo;
    private String keyword;

    /**
     * Filter by deleted status.
     * ADMIN ONLY - ignored for USER role.
     *
     * null  → all decisions (deleted + active)   [default for admin]
     * true  → only deleted decisions
     * false → only active decisions
     */
    private String deleted;

    // PrintMatter component filters

    /**
     * Filter by printMatter year (last 2 digits).
     * Examples: "10", "22", "23", "24", "26"
     *
     * Matches: "287-10/XVI/10" when printMatterYear = "10"
     */
    private String printMatterYear;

    /**
     * Filter by printMatter election period (römische Zahl).
     * Examples: "XVI", "XIX", "XX", "X"
     *
     * Matches: "287-10/XVI/10" when printMatterElectionPeriod = "XVI"
     */
    private String printMatterElectionPeriod;
}

