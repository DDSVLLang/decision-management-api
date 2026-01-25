package de.langen.decision_service.application.service;

import de.langen.decision_service.api.dto.request.CreateDepartmentRequest;
import de.langen.decision_service.api.dto.request.CreateTopicRequest;
import de.langen.decision_service.api.dto.request.UpdateDepartmentRequest;
import de.langen.decision_service.api.dto.request.UpdateTopicRequest;
import de.langen.decision_service.api.dto.response.*;
import de.langen.decision_service.api.exception.ResourceNotFoundException;
import de.langen.decision_service.domain.entity.*;
import de.langen.decision_service.domain.repository.CommitteeRepository;
import de.langen.decision_service.domain.repository.DepartmentRepository;
import de.langen.decision_service.domain.repository.TopicRepository;
import de.langen.decision_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for master data management (Topics, Departments, Committees, Users).
 *
 * @author Backend Team
 * @version 2.0 - Added Topic CRUD operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementService {

    private final TopicRepository topicRepository;
    private final DepartmentRepository departmentRepository;
    private final CommitteeRepository committeeRepository;
    private final UserRepository userRepository;

    // =========================================================================
    // Topic Operations
    // =========================================================================

    /**
     * Get all topics.
     *
     * @return list of topics
     */
    @Transactional(readOnly = true)
    public List<TopicResponse> getAllTopics() {
        log.debug("Fetching all topics");
        return topicRepository.findAll().stream()
                .map(this::mapTopicToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new topic.
     * ADMIN ONLY
     *
     * @param request topic data
     * @return created topic
     */
    @Transactional
    public TopicResponse createTopic(CreateTopicRequest request) {
        log.info("Creating new topic: {}", request.getName());

        // Check if topic with same name already exists
        if (topicRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Topic with name '" + request.getName() + "' already exists");
        }

        Topic topic = Topic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Topic savedTopic = topicRepository.save(topic);
        log.info("Topic created with id: {}", savedTopic.getId());

        return mapTopicToResponse(savedTopic);
    }

    /**
     * Update an existing topic.
     * ADMIN ONLY
     *
     * @param id topic UUID
     * @param request update data
     * @return updated topic
     */
    @Transactional
    public TopicResponse updateTopic(String id, UpdateTopicRequest request) {
        log.info("Updating topic with id: {}", id);

        Topic topic = topicRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            // Check if new name conflicts with existing topic
            topicRepository.findByName(request.getName())
                    .ifPresent(existingTopic -> {
                        if (!existingTopic.getId().equals(topic.getId())) {
                            throw new IllegalArgumentException(
                                    "Topic with name '" + request.getName() + "' already exists"
                            );
                        }
                    });
            topic.setName(request.getName());
        }

        if (request.getDescription() != null) {
            topic.setDescription(request.getDescription());
        }

        Topic updatedTopic = topicRepository.save(topic);
        log.info("Topic updated: {}", updatedTopic.getName());

        return mapTopicToResponse(updatedTopic);
    }

    /**
     * Get topic by ID.
     *
     * @param id topic UUID
     * @return topic
     */
    @Transactional(readOnly = true)
    public TopicResponse getTopicById(String id) {
        log.debug("Fetching topic with id: {}", id);

        Topic topic = topicRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        return mapTopicToResponse(topic);
    }

    /**
     * Delete a topic.
     * ADMIN ONLY
     * Note: This might fail if there are decisions referencing this topic.
     *
     * @param id topic UUID
     */
    @Transactional
    public void deleteTopic(String id) {
        log.info("Deleting topic with id: {}", id);

        Topic topic = topicRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        try {
            topicRepository.delete(topic);
            log.info("Topic deleted: {}", topic.getName());
        } catch (Exception e) {
            log.error("Failed to delete topic {}: {}", id, e.getMessage());
            throw new IllegalStateException(
                    "Cannot delete topic because it is referenced by existing decisions",
                    e
            );
        }
    }

    // =========================================================================
    // Department Operations (Read-Only for now)
    // =========================================================================

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Fetching all departments");
        return departmentRepository.findAll().stream()
                .map(this::mapDepartmentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new department.
     * ADMIN ONLY
     *
     * @param request department data
     * @return created department
     */
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new Department: {}", request.getName());

        // Check if department with same name already exists
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department savedTopic = departmentRepository.save(department);
        log.info("Department created with id: {}", savedTopic.getId());

        return mapDepartmentToResponse(savedTopic);
    }

    /**
     * Update an existing Department.
     * ADMIN ONLY
     *
     * @param id department UUID
     * @param request update data
     * @return updated department
     */
    @Transactional
    public DepartmentResponse updateDepartment(String id, UpdateDepartmentRequest request) {
        log.info("Updating department with id: {}", id);

        Department department = departmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            // Check if new name conflicts with existing topic
            departmentRepository.findByName(request.getName())
                    .ifPresent(existingTopic -> {
                        if (!existingTopic.getId().equals(department.getId())) {
                            throw new IllegalArgumentException(
                                    "Department with name '" + request.getName() + "' already exists"
                            );
                        }
                    });
            department.setName(request.getName());
        }

        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department updated: {}", updatedDepartment.getName());

        return mapDepartmentToResponse(updatedDepartment);
    }

    /**
     * Get department by ID.
     *
     * @param id department UUID
     * @return department
     */
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(String id) {
        log.debug("Fetching department with id: {}", id);

        Department department = departmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        return mapDepartmentToResponse(department);
    }

    /**
     * Delete a department.
     * ADMIN ONLY
     * Note: This might fail if there are decisions referencing this department.
     *
     * @param id topic UUID
     */
    @Transactional
    public void deleteDepartment(String id) {
        log.info("Deleting department with id: {}", id);

        Department department = departmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        try {
            departmentRepository.delete(department);
            log.info("Department deleted: {}", department.getName());
        } catch (Exception e) {
            log.error("Failed to delete department {}: {}", id, e.getMessage());
            throw new IllegalStateException(
                    "Cannot delete Department because it is referenced by existing decisions",
                    e
            );
        }
    }

    // =========================================================================
    // Committee Operations (Read-Only for now)
    // =========================================================================

    @Transactional(readOnly = true)
    public List<CommitteeResponse> getAllCommittees() {
        log.debug("Fetching all committees");
        return committeeRepository.findAll().stream()
                .map(this::mapCommitteeToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // User Operations (Read-Only for now)
    // =========================================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Mapping Methods
    // =========================================================================

    private TopicResponse mapTopicToResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId().toString())
                .name(topic.getName())
                .description(topic.getDescription())
                .createdAt(String.valueOf(topic.getCreatedAt()))
                .updatedAt(String.valueOf(topic.getUpdatedAt()))
                .build();
    }

    private DepartmentResponse mapDepartmentToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId().toString())
                .name(department.getName())
                .shortName(department.getShortName())
                .description(department.getDescription())
                .active(department.getActive())
                .headUserId(department.getHeadUserId() != null ? department.getHeadUserId().toString() : null)
                .createdAt(String.valueOf(department.getCreatedAt()))
                .updatedAt(String.valueOf(department.getUpdatedAt()))
                .build();
    }

    private CommitteeResponse mapCommitteeToResponse(Committee committee) {
        return CommitteeResponse.builder()
                .id(committee.getId().toString())
                .name(committee.getName())
                .shortName(committee.getShortName())
                .description(committee.getDescription())
                .createdAt(String.valueOf(committee.getCreatedAt()))
                .updatedAt(String.valueOf(committee.getUpdatedAt()))
                .build();
    }

    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().getValue())
                .responsibleDepartment(user.getResponsibleDepartment())
                .active(user.getActive())
                .createdAt(String.valueOf(user.getCreatedAt()))
                .build();
    }
}