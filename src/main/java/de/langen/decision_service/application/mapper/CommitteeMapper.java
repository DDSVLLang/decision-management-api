package de.langen.decision_service.application.mapper;

import de.langen.decision_service.api.dto.request.CreateCommitteeRequest;
import de.langen.decision_service.api.dto.request.UpdateCommitteeRequest;
import de.langen.decision_service.api.dto.response.CommitteeResponse;
import de.langen.decision_service.domain.entity.Committee;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MapStruct mapper for Committee entity.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommitteeMapper {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================================
    // Request to Entity Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Committee toEntity(CreateCommitteeRequest request);

    // =========================================================================
    // Update Entity from Request
    // =========================================================================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateCommitteeRequest request, @MappingTarget Committee entity);

    // =========================================================================
    // Entity to Response Mapping
    // =========================================================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToString")
    CommitteeResponse toResponse(Committee entity);

    List<CommitteeResponse> toResponseList(List<Committee> entities);

    // =========================================================================
    // Helper Methods
    // =========================================================================

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }
}