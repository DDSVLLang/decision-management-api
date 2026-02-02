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
    private String topic;
    private String department;
    private String committee;
    private String decisionDateFrom;
    private String decisionDateTo;
    private String keyword;
}

