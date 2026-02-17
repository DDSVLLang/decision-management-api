package de.langen.decision_service.application.service;

import de.langen.decision_service.api.dto.request.*;
import de.langen.decision_service.api.dto.response.*;
import de.langen.decision_service.api.exception.ResourceNotFoundException;
import de.langen.decision_service.domain.entity.*;
import de.langen.decision_service.domain.repository.CommitteeRepository;
import de.langen.decision_service.domain.repository.DepartmentRepository;
import de.langen.decision_service.domain.repository.TopicRepository;
import de.langen.decision_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for master data management (Topics, Departments, Committees, Users).
 *
 * @author Yacine  Sghairi
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

    @Autowired
    private PasswordEncoder passwordEncoder;


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

        if (request.getShortName() != null) {
            department.setShortName(request.getShortName());
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

    /**
     * Get committee by ID.
     *
     * @param id committee UUID
     * @return committee
     */
    @Transactional(readOnly = true)
    public CommitteeResponse getCommitteeById(String id) {
        log.debug("Fetching committee with id: {}", id);

        Committee committee = committeeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Committee not found with id: " + id));

        return mapCommitteeToResponse(committee);
    }

    /**
     * Create a new committee.
     * ADMIN ONLY
     *
     * @param request committee data
     * @return created committee
     */
    @Transactional
    public CommitteeResponse createCommittee(CreateCommitteeRequest request) {
        log.info("Creating new committee: {}", request.getName());

        // Check if committee with same name already exists
        if (committeeRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Committee with name '" + request.getName() + "' already exists");
        }

        // Check if shortName is provided and unique
        if (request.getShortName() != null && !request.getShortName().isBlank()) {
            if (committeeRepository.findByShortName(request.getShortName()).isPresent()) {
                throw new IllegalArgumentException(
                        "Committee with short name '" + request.getShortName() + "' already exists"
                );
            }
        }

        Committee committee = Committee.builder()
                .name(request.getName())
                .shortName(request.getShortName())
                .description(request.getDescription())
                .build();

        Committee savedCommittee = committeeRepository.save(committee);
        log.info("Committee created with id: {}", savedCommittee.getId());

        return mapCommitteeToResponse(savedCommittee);
    }

    /**
     * Update an existing committee.
     * ADMIN ONLY
     *
     * @param id committee UUID
     * @param request update data
     * @return updated committee
     */
    @Transactional
    public CommitteeResponse updateCommittee(String id, UpdateCommitteeRequest request) {
        log.info("Updating committee with id: {}", id);

        Committee committee = committeeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Committee not found with id: " + id));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            // Check if new name conflicts with existing committee
            committeeRepository.findByName(request.getName())
                    .ifPresent(existingCommittee -> {
                        if (!existingCommittee.getId().equals(committee.getId())) {
                            throw new IllegalArgumentException(
                                    "Committee with name '" + request.getName() + "' already exists"
                            );
                        }
                    });
            committee.setName(request.getName());
        }

        // Update shortName if provided
        if (request.getShortName() != null && !request.getShortName().isBlank()) {
            // Check if new shortName conflicts with existing committee
            committeeRepository.findByShortName(request.getShortName())
                    .ifPresent(existingCommittee -> {
                        if (!existingCommittee.getId().equals(committee.getId())) {
                            throw new IllegalArgumentException(
                                    "Committee with short name '" + request.getShortName() + "' already exists"
                            );
                        }
                    });
            committee.setShortName(request.getShortName());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            committee.setDescription(request.getDescription());
        }

        Committee updatedCommittee = committeeRepository.save(committee);
        log.info("Committee updated: {}", updatedCommittee.getName());

        return mapCommitteeToResponse(updatedCommittee);
    }

    /**
     * Delete a committee.
     * ADMIN ONLY
     * Note: This might fail if there are decisions referencing this committee.
     *
     * @param id committee UUID
     */
    @Transactional
    public void deleteCommittee(String id) {
        log.info("Deleting committee with id: {}", id);

        Committee committee = committeeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Committee not found with id: " + id));

        try {
            committeeRepository.delete(committee);
            log.info("Committee deleted: {}", committee.getName());
        } catch (Exception e) {
            log.error("Failed to delete committee {}: {}", id, e.getMessage());
            throw new IllegalStateException(
                    "Cannot delete committee because it is referenced by existing decisions",
                    e
            );
        }
    }

    // =========================================================================
    // User Operations
    // =========================================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID.
     * ADMIN ONLY
     *
     * @param id user UUID
     * @return user
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        log.debug("Fetching user with id: {}", id);

        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapUserToResponse(user);
    }

    /**
     * Update an existing user.
     * ADMIN ONLY
     *
     * @param id user UUID
     * @param request update data
     * @return updated user
     */
    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if new email conflicts with existing user
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new IllegalArgumentException(
                                    "User with email '" + request.getEmail() + "' already exists"
                            );
                        }
                    });
            user.setEmail(request.getEmail());
        }

        // Update password if provided (will be BCrypt hashed)
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("Password updated for user: {}", user.getEmail());
        }

        // Update role if provided
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                UserRole newRole = UserRole.valueOf(request.getRole().toUpperCase());
                user.setRole(newRole);
                log.info("Role updated for user {}: {} -> {}",
                        user.getEmail(), user.getRole(), newRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
        }

        // Update first name if provided
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        // Update last name if provided
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        // Update department if provided
        if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
            try {
                UUID deptUuid = UUID.fromString(request.getDepartmentId());
                Department department = departmentRepository.findById(deptUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Department not found with id: " + request.getDepartmentId()
                        ));
                user.setDepartment(department);
                log.info("Department updated for user {}: {}",
                        user.getEmail(), department.getName());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid department ID format: " + request.getDepartmentId()
                );
            }
        }

        // Update description if provided
        if (request.getDescription() != null) {
            user.setDescription(request.getDescription());
        }

        // Update active status if provided
        if (request.getActive() != null) {
            user.setActive(request.getActive());
            log.info("Active status updated for user {}: {}",
                    user.getEmail(), request.getActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());

        return mapUserToResponse(updatedUser);
    }

    /**
     * Delete a user.
     * ADMIN ONLY
     *
     * @param id user UUID
     */
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user with id: {}", id);

        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        try {
            userRepository.delete(user);
            log.info("User deleted: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", id, e.getMessage());
            throw new IllegalStateException(
                    "Cannot delete user because they are referenced by existing decisions or reports",
                    e
            );
        }
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
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().getValue())
                .description(user.getDescription())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getDepartment() != null) {
            Department dept = user.getDepartment();

            builder.department(
                    UserResponse.DepartmentInfo.builder()
                            .id(dept.getId().toString())
                            .name(dept.getName())
                            .shortName(dept.getShortName())
                            .description(dept.getDescription())
                            .active(dept.getActive())
                            .build()
            );

            // Backward compatibility: set string field
            builder.responsibleDepartment(dept.getName());
        }

        return builder.build();
    }
}