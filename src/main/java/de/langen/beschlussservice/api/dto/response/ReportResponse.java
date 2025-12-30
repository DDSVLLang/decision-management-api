package de.langen.beschlussservice.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    private String id;
    private String year;
    private String content;
    private String expectedCompletionQuarter;
    private String createdAt;
    private String createdBy;
    private CreatedByUserInfo createdByUser;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedByUserInfo {
        private String firstName;
        private String lastName;
    }
}

