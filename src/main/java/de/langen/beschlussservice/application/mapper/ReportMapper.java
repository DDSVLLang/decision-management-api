package de.langen.beschlussservice.application.mapper;

import de.langen.beschlussservice.api.dto.request.CreateReportRequest;
import de.langen.beschlussservice.api.dto.response.ReportResponse;
import de.langen.beschlussservice.domain.entity.Report;
import de.langen.beschlussservice.domain.entity.ReportStatus;
import de.langen.beschlussservice.domain.entity.User;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MapStruct mapper for Report entity.
 *
 * @author Backend Team
 * @version 2.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReportMapper {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================================
    // Request to Entity Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decision", ignore = true) // Set by service
    @Mapping(target = "status", constant = "DRAFT") // Default status
    @Mapping(target = "createdBy", ignore = true) // Set by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Report toEntity(CreateReportRequest request);

    // =========================================================================
    // Entity to Response Mapping
    // =========================================================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "decisionId", source = "decision.id")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "createdByUser", ignore = true) // Will be set in @AfterMapping
    ReportResponse toResponse(Report report, @Context User createdByUser);

    List<ReportResponse> toResponseList(List<Report> reports);

    // =========================================================================
    // Helper Methods
    // =========================================================================

    @Named("statusToString")
    default String statusToString(ReportStatus status) {
        return status != null ? status.getValue() : null;
    }

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    // =========================================================================
    // After Mapping
    // =========================================================================

    @AfterMapping
    default void mapCreatedByUser(
            Report report,
            @MappingTarget ReportResponse response,
            @Context User createdByUser
    ) {
        if (createdByUser != null) {
            response.setCreatedByUser(
                    ReportResponse.CreatedByUserInfo.builder()
                            .firstName(createdByUser.getFirstName())
                            .lastName(createdByUser.getLastName())
                            .email(createdByUser.getEmail())
                            .build()
            );
        }
    }
}