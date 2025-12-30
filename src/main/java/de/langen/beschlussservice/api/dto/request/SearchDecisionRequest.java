package de.langen.beschlussservice.api.dto.request;

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
    private String responsibleDepartment;
    private String decisionDateFrom;
    private String decisionDateTo;
    private String keyword;
}

