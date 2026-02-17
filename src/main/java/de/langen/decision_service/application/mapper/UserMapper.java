package de.langen.decision_service.application.mapper;

import de.langen.decision_service.api.dto.request.CreateUserRequest;
import de.langen.decision_service.api.dto.request.UpdateUserRequest;
import de.langen.decision_service.api.dto.response.UserResponse;
import de.langen.decision_service.domain.entity.User;
import de.langen.decision_service.domain.entity.UserRole;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MapStruct mapper for User entity.
 * Note: Password handling is done separately for security.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================================
    // Request to Entity Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Set separately with BCrypt
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRole")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    // =========================================================================
    // Update Entity from Request
    // =========================================================================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Changed via separate endpoint
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRole")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User entity);

    // =========================================================================
    // Entity to Response Mapping
    // =========================================================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    @Mapping(target = "fullName", expression = "java(entity.getFullName())")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToString")
    UserResponse toResponse(User entity);

    List<UserResponse> toResponseList(List<User> entities);

    // =========================================================================
    // Helper Methods
    // =========================================================================

    @Named("stringToRole")
    default UserRole stringToRole(String role) {
        if (role == null || role.isBlank()) {
            return UserRole.USER; // Default
        }
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: " + role + ". Must be one of: ADMIN, USER"
            );
        }
    }

    @Named("roleToString")
    default String roleToString(UserRole role) {
        return role != null ? role.getValue() : null;
    }

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }
}