package de.langen.decision_service.application.mapper;

import de.langen.decision_service.api.dto.request.CreateDecisionRequest;
import de.langen.decision_service.api.dto.response.DecisionResponse;
import de.langen.decision_service.domain.entity.*;
import de.langen.decision_service.domain.repository.CommitteeRepository;
import de.langen.decision_service.domain.repository.DepartmentRepository;
import de.langen.decision_service.domain.repository.TopicRepository;
import de.langen.decision_service.domain.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Decision entity.
 * Uses @AfterMapping for complex entity-to-string conversions.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Mapper(
        componentModel = "spring",
        uses = {ReportMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public abstract class DecisionMapper {

    @Autowired
    protected TopicRepository topicRepository;

    @Autowired
    protected CommitteeRepository committeeRepository;

    @Autowired
    protected DepartmentRepository departmentRepository;

    @Autowired
    protected UserRepository userRepository;

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    protected static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================================
    // Request to Entity Mapping
    // =========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decisionDate", source = "decisionDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority")

    // Map String fields to Entity relationships
    @Mapping(target = "topic", source = "topic", qualifiedByName = "stringToTopic")
    @Mapping(target = "committee", source = "decisionCommittee", qualifiedByName = "stringToCommittee")
    @Mapping(target = "responsibleDepartments", ignore = true)
    @Mapping(target = "assignee", source = "assigneeId", qualifiedByName = "stringToUser")

    // Ignore entity-managed fields
    @Mapping(target = "reports", ignore = true)
    @Mapping(target = "completedByUser", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "completedBy", ignore = true)

    public abstract Decision toEntity(CreateDecisionRequest request);

    // =========================================================================
    // Entity to Response Mapping - IGNORE all complex entity fields
    // =========================================================================

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "decisionDate", source = "decisionDate", qualifiedByName = "localDateToString")
    @Mapping(target = "printMatter", source = "printMatter")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToString")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "localDateToString")
    @Mapping(target = "implementationNotes", source = "implementationNotes")
    @Mapping(target = "estimatedHours", source = "estimatedHours")
    @Mapping(target = "actualHours", source = "actualHours")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "completedAt", source = "completedAt")

    // IMPORTANT: Ignore all entity relationships - will be set in @AfterMapping
    @Mapping(target = "decisionTopic", ignore = true)
    @Mapping(target = "decisionCommittee", ignore = true)
    @Mapping(target = "assigneeId", ignore = true)
    @Mapping(target = "assigneeName", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedByUser", ignore = true)
    // Reports will be mapped automatically by ReportMapper
    public abstract DecisionResponse toResponse(Decision entity, @Context User completedByUser);

    public abstract List<DecisionResponse> toResponseList(List<Decision> entities);

    // =========================================================================
    // String to Entity Converters (for Foreign Keys)
    // =========================================================================

    @Named("stringToTopic")
    protected Topic stringToTopic(String topicName) {
        if (topicName == null || topicName.isBlank()) {
            return null;
        }
        return topicRepository.findByName(topicName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found: " + topicName +
                                ". Please create the topic first or use an existing one."
                ));
    }

    @Named("stringToCommittee")
    protected Committee stringToCommittee(String committeeName) {
        if (committeeName == null || committeeName.isBlank()) {
            return null;
        }
        return committeeRepository.findByName(committeeName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Committee not found: " + committeeName +
                                ". Please create the committee first or use an existing one."
                ));
    }

    @Named("stringToDepartment")
    protected Department stringToDepartment(String departmentName) {
        if (departmentName == null || departmentName.isBlank()) {
            return null;
        }
        // Try by name first, then by short name
        return departmentRepository.findByName(departmentName)
                .or(() -> departmentRepository.findByShortName(departmentName))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Department not found: " + departmentName +
                                ". Please create the department first or use an existing one."
                ));
    }

    @Named("stringToUser")
    protected User stringToUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(userId);
            return userRepository.findById(uuid)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found with id: " + userId
                    ));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid user ID format: " + userId + ". Must be a valid UUID."
            );
        }
    }

    // =========================================================================
    // Date/Time Converters
    // =========================================================================

    @Named("stringToLocalDate")
    protected LocalDate stringToLocalDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid date format: " + date + ". Expected format: YYYY-MM-DD"
            );
        }
    }

    @Named("localDateToString")
    protected String localDateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    @Named("localDateTimeToString")
    protected String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    // =========================================================================
    // Enum Converters
    // =========================================================================

    @Named("stringToStatus")
    protected DecisionStatus stringToStatus(String status) {
        if (status == null || status.isBlank()) {
            return DecisionStatus.PENDING; // Default
        }
        try {
            return DecisionStatus.valueOf(status.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: " + status +
                            ". Must be one of: pending, in-progress, completed"
            );
        }
    }

    @Named("statusToString")
    protected String statusToString(DecisionStatus status) {
        return status != null ? status.getValue() : null;
    }

    @Named("stringToPriority")
    protected DecisionPriority stringToPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return DecisionPriority.MEDIUM; // Default
        }
        try {
            return DecisionPriority.valueOf(priority.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid priority: " + priority +
                            ". Must be one of: low, medium, high, urgent"
            );
        }
    }

    @Named("priorityToString")
    protected String priorityToString(DecisionPriority priority) {
        return priority != null ? priority.getValue() : null;
    }

    // =========================================================================
    // After Mapping - Manual mapping of complex fields
    // =========================================================================

    @AfterMapping
    protected void mapEntityRelationshipsToResponse(
            Decision entity,
            @MappingTarget DecisionResponse response,
            @Context User completedByUser
    ) {
        // Map Topic entity to String
        if (entity.getTopic() != null) {
            response.setDecisionTopic(entity.getTopic().getName());
        }

        // Map Committee entity to String
        if (entity.getCommittee() != null) {
            response.setDecisionCommittee(entity.getCommittee().getName());
        }
        // ⭐ Map Departments - FULL OBJECTS
        if (entity.getResponsibleDepartments() != null && !entity.getResponsibleDepartments().isEmpty()) {
            // Full objects
            List<DecisionResponse.DepartmentInfo> departmentInfos = entity.getResponsibleDepartments().stream()
                    .map(this::mapDepartmentToDepartmentInfo)
                    .collect(Collectors.toList());
            response.setDepartments(departmentInfos);

            // Backward compatibility: first department name
            response.setDecisionDepartment(entity.getResponsibleDepartments().get(0).getName());
        }

        // Map Assignee entity to ID and Name
        if (entity.getAssignee() != null) {
            response.setAssigneeId(entity.getAssignee().getId().toString());
            response.setAssigneeName(entity.getAssignee().getFullName());
        }

        // Map createdBy UUID to String
        if (entity.getCreatedBy() != null) {
            response.setCreatedBy(entity.getCreatedBy().toString());
        }

        // Map completedBy UUID to String
        if (entity.getCompletedBy() != null) {
            response.setCompletedBy(entity.getCompletedBy().toString());
        }

        // Map CompletedBy User Info
        if (completedByUser != null) {
            response.setCompletedByUser(
                    DecisionResponse.CompletedByUserInfo.builder()
                            .firstName(completedByUser.getFirstName())
                            .lastName(completedByUser.getLastName())
                            .build()
            );
        }

        // Note: Reports are not mapped here as they would cause lazy loading
        // Use separate endpoint to fetch reports if needed
    }

    /**
     * Map Department entity to DepartmentInfo DTO.
     *
     * @param department department entity
     * @return department info
     */
    protected DecisionResponse.DepartmentInfo mapDepartmentToDepartmentInfo(Department department) {
        DecisionResponse.DepartmentInfo info = DecisionResponse.DepartmentInfo.builder()
                .id(department.getId().toString())
                .name(department.getName())
                .shortName(department.getShortName())
                .description(department.getDescription())
                .active(department.getActive())
                .build();

        // Map Head User if exists
        if (department.getHeadUserId() != null) {
            User headUser = userRepository.findById(department.getHeadUserId()).orElse(null);
            if (headUser != null) {
                info.setHeadUser(
                        DecisionResponse.DepartmentInfo.HeadUserInfo.builder()
                                .id(headUser.getId().toString())
                                .firstName(headUser.getFirstName())
                                .lastName(headUser.getLastName())
                                .fullName(headUser.getFullName())
                                .email(headUser.getEmail())
                                .build()
                );
            }
        }

        return info;
    }
}