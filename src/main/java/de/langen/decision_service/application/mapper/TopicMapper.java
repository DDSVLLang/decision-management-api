package de.langen.decision_service.application.mapper;

import de.langen.decision_service.api.dto.request.CreateTopicRequest;
import de.langen.decision_service.api.dto.request.UpdateTopicRequest;
import de.langen.decision_service.api.dto.response.TopicResponse;
import de.langen.decision_service.domain.entity.Topic;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MapStruct mapper for Topic entity.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TopicMapper {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================================
    // Request to Entity Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Topic toEntity(CreateTopicRequest request);

    // =========================================================================
    // Update Entity from Request
    // =========================================================================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateTopicRequest request, @MappingTarget Topic entity);

    // =========================================================================
    // Entity to Response Mapping
    // =========================================================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToString")
    TopicResponse toResponse(Topic entity);

    List<TopicResponse> toResponseList(List<Topic> entities);

    // =========================================================================
    // Helper Methods
    // =========================================================================

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }
}