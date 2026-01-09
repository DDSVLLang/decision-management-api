package de.langen.beschlussservice.application.mapper;

import de.langen.beschlussservice.api.dto.request.CreateDepartmentRequest;
import de.langen.beschlussservice.api.dto.request.UpdateDepartmentRequest;
import de.langen.beschlussservice.api.dto.response.DepartmentResponse;
import de.langen.beschlussservice.domain.entity.Department;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MapStruct mapper for Department entity.
 *
 * @author Backend Team
 * @version 2.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DepartmentMapper {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================================
    // Request to Entity Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "headUser", ignore = true) // Set separately via service
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Department toEntity(CreateDepartmentRequest request);

    // =========================================================================
    // Update Entity from Request
    // =========================================================================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "headUser", ignore = true) // Set separately via service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateDepartmentRequest request, @MappingTarget Department entity);

    // =========================================================================
    // Entity to Response Mapping
    // =========================================================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "headUserId", source = "headUser.id")
    @Mapping(target = "headUserName", expression = "java(entity.getHeadName())")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToString")
    DepartmentResponse toResponse(Department entity);

    List<DepartmentResponse> toResponseList(List<Department> entities);

    // =========================================================================
    // Helper Methods
    // =========================================================================

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }
}