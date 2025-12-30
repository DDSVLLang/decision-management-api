package de.langen.beschlussservice.application.mapper;

import de.langen.beschlussservice.api.dto.request.CreateReportRequest;
import de.langen.beschlussservice.api.dto.response.ReportResponse;
import de.langen.beschlussservice.domain.entity.Report;
import de.langen.beschlussservice.domain.entity.User;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReportMapper {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Report toEntity(CreateReportRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "reportLocalDateTimeToString")
    ReportResponse toResponse(Report entity, @Context User createdByUser);

    List<ReportResponse> toResponseList(List<Report> entities);

    @Named("reportLocalDateTimeToString")
    default String reportLocalDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    @AfterMapping
    default void mapCreatedByUser(
            Report entity,
            @MappingTarget ReportResponse response,
            @Context User createdByUser
    ) {
        if (createdByUser != null) {
            response.setCreatedByUser(
                    ReportResponse.CreatedByUserInfo.builder()
                            .firstName(createdByUser.getFirstName())
                            .lastName(createdByUser.getLastName())
                            .build()
            );
        }
    }
}

