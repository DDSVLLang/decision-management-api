package de.langen.beschlussservice.application.mapper;


import de.langen.beschlussservice.api.dto.request.CreateDecisionRequest;
import de.langen.beschlussservice.api.dto.response.DecisionResponse;
import de.langen.beschlussservice.domain.entity.Decision;
import de.langen.beschlussservice.domain.entity.DecisionStatus;
import de.langen.beschlussservice.domain.entity.User;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {ReportMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DecisionMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decisionDate", source = "decisionDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "reports", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    Decision toEntity(CreateDecisionRequest request);

    @Mapping(target = "decisionDate", source = "decisionDate", qualifiedByName = "localDateToString")
    @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "localDateToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "completedAt", source = "completedAt", qualifiedByName = "decisionLocalDateTimeToString")
    DecisionResponse toResponse(Decision entity, @Context User completedByUser);

    List<DecisionResponse> toResponseList(List<Decision> entities);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return date != null && !date.isBlank()
                ? LocalDate.parse(date, DATE_FORMATTER)
                : null;
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    @Named("decisionLocalDateTimeToString")
    default String decisionLocalDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    @Named("stringToStatus")
    default DecisionStatus stringToStatus(String status) {
        return status != null
                ? DecisionStatus.valueOf(status.toUpperCase().replace("-", "_"))
                : null;
    }

    @Named("statusToString")
    default String statusToString(DecisionStatus status) {
        return status != null ? status.getValue() : null;
    }

    @AfterMapping
    default void mapCompletedByUser(
            Decision entity,
            @MappingTarget DecisionResponse response,
            @Context User completedByUser
    ) {
        if (completedByUser != null) {
            response.setCompletedByUser(
                    DecisionResponse.CompletedByUserInfo.builder()
                            .firstName(completedByUser.getFirstName())
                            .lastName(completedByUser.getLastName())
                            .build()
            );
        }
    }
}

