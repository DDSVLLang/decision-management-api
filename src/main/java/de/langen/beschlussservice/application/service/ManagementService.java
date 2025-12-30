package de.langen.beschlussservice.application.service;

import de.langen.beschlussservice.api.dto.response.CommitteeResponse;
import de.langen.beschlussservice.api.dto.response.DepartmentResponse;
import de.langen.beschlussservice.api.dto.response.TopicResponse;
import de.langen.beschlussservice.api.dto.response.UserResponse;
import de.langen.beschlussservice.domain.entity.Committee;
import de.langen.beschlussservice.domain.entity.Department;
import de.langen.beschlussservice.domain.entity.Topic;
import de.langen.beschlussservice.domain.entity.User;
import de.langen.beschlussservice.domain.repository.CommitteeRepository;
import de.langen.beschlussservice.domain.repository.DepartmentRepository;
import de.langen.beschlussservice.domain.repository.TopicRepository;
import de.langen.beschlussservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementService {

    private final TopicRepository topicRepository;
    private final CommitteeRepository committeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional(readOnly = true)
    public List<TopicResponse> getAllTopics() {
        log.debug("Fetching all topics");
        List<Topic> topics = topicRepository.findAll();
        return topics.stream()
                .map(this::toTopicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommitteeResponse> getAllCommittees() {
        log.debug("Fetching all committees");
        List<Committee> committees = committeeRepository.findAll();
        return committees.stream()
                .map(this::toCommitteeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Fetching all departments");
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(this::toDepartmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toUserResponse)
                .toList();
    }

    private TopicResponse toTopicResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId().toString())
                .name(topic.getName())
                .createdAt(topic.getCreatedAt() != null
                        ? topic.getCreatedAt().format(DATE_TIME_FORMATTER)
                        : null)
                .updatedAt(topic.getUpdatedAt() != null
                        ? topic.getUpdatedAt().format(DATE_TIME_FORMATTER)
                        : null)
                .build();
    }

    private CommitteeResponse toCommitteeResponse(Committee committee) {
        return CommitteeResponse.builder()
                .id(committee.getId().toString())
                .name(committee.getName())
                .createdAt(committee.getCreatedAt() != null
                        ? committee.getCreatedAt().format(DATE_TIME_FORMATTER)
                        : null)
                .updatedAt(committee.getUpdatedAt() != null
                        ? committee.getUpdatedAt().format(DATE_TIME_FORMATTER)
                        : null)
                .build();
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId().toString())
                .name(department.getName())
                .createdAt(department.getCreatedAt() != null
                        ? department.getCreatedAt().format(DATE_TIME_FORMATTER)
                        : null)
                .updatedAt(department.getUpdatedAt() != null
                        ? department.getUpdatedAt().format(DATE_TIME_FORMATTER)
                        : null)
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().getValue())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .responsibleDepartment(user.getResponsibleDepartment())
                .build();
    }
}
