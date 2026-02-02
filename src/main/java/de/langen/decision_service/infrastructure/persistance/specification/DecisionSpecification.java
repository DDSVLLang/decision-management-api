package de.langen.decision_service.infrastructure.persistance.specification;

import de.langen.decision_service.domain.entity.Decision;
import de.langen.decision_service.domain.entity.DecisionStatus;
import de.langen.decision_service.domain.entity.DecisionPriority;
import de.langen.decision_service.domain.entity.Department;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification for dynamic Decision queries with filters.
 * Works with Entity relationships (FK joins).
 *
 * @author Yacine Sghairi
 * @version 2.1
 */
public class DecisionSpecification {

    /**
     * Create specification with multiple filters.
     * All filters are combined with AND logic.
     *
     * @param status decision status (PENDING, IN_PROGRESS, COMPLETED)
     * @param topic topic name (searches by topic.name via JOIN)
     * @param department department name (searches by responsibleDepartment.name via JOIN)
     * @param dateFrom decision date from (inclusive)
     * @param dateTo decision date to (inclusive)
     * @param keyword keyword search in title and content (case-insensitive)
     * @return specification for query
     */
    public static Specification<Decision> withFilters(
            String status,
            String topic,
            String department,
            String committee,
            LocalDate dateFrom,
            LocalDate dateTo,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted decisions
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            // ================================================================
            // Status Filter
            // ================================================================
            if (status != null && !status.isBlank()) {
                try {
                    // Normalize status: "done" → "COMPLETED"
                    String normalizedStatus = status.equalsIgnoreCase("done")
                            ? "COMPLETED"
                            : status.toUpperCase().replace("-", "_");

                    DecisionStatus decisionStatus = DecisionStatus.valueOf(normalizedStatus);
                    predicates.add(criteriaBuilder.equal(root.get("status"), decisionStatus));
                } catch (IllegalArgumentException e) {
                    // Invalid status - skip filter
                }
            }

            // ================================================================
            // Topic Filter (FK Join to Topic entity)
            // ================================================================
            if (topic != null && !topic.isBlank()) {
                // Create LEFT JOIN to topic entity
                Join<Object, Object> topicJoin = root.join("topic", JoinType.LEFT);

                // Filter by topic name (case-insensitive)
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(topicJoin.get("name")),
                        topic.toLowerCase()
                ));
            }

            // ================================================================
            // Committee Filter (FK Join to Topic entity)
            // ================================================================
            if (committee != null && !committee.isBlank()) {
                // Create LEFT JOIN to committee entity
                Join<Object, Object> committeeJoin = root.join("committee", JoinType.LEFT);

                // Filter by committee name (case-insensitive)
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(committeeJoin.get("name")),
                        committee.toLowerCase()
                ));
            }

            // ================================================================
            // Department Filter (FK Join to Department entity)
            // ================================================================
            if (department != null && !department.isBlank()) {
                // Create LEFT JOIN to responsibleDepartment entity
                Join<Object, Object> deptJoin = root.join("responsibleDepartments", JoinType.LEFT);

                // Try to match by name OR shortName (case-insensitive)
                Predicate namePredicate = criteriaBuilder.equal(
                        criteriaBuilder.lower(deptJoin.get("name")),
                        department.toLowerCase()
                );

                Predicate shortNamePredicate = criteriaBuilder.equal(
                        criteriaBuilder.lower(deptJoin.get("shortName")),
                        department.toLowerCase()
                );

                predicates.add(criteriaBuilder.or(namePredicate, shortNamePredicate));
            }

            // ================================================================
            // Department Filter (Many-to-Many Join)
            // ================================================================
            if (department != null && !department.isBlank()) {
                // Create JOIN to responsibleDepartments collection
                Join<Decision, Department> deptJoin = root.join("responsibleDepartments", JoinType.LEFT);

                // Filter by department name OR shortName (case-insensitive)
                Predicate namePredicate = criteriaBuilder.equal(
                        criteriaBuilder.lower(deptJoin.get("name")),
                        department.toLowerCase()
                );

                Predicate shortNamePredicate = criteriaBuilder.equal(
                        criteriaBuilder.lower(deptJoin.get("shortName")),
                        department.toLowerCase()
                );

                predicates.add(criteriaBuilder.or(namePredicate, shortNamePredicate));
            }


            // ================================================================
            // Date Range Filters
            // ================================================================
            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("decisionDate"),
                        dateFrom
                ));
            }

            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("decisionDate"),
                        dateTo
                ));
            }

            // ================================================================
            // Keyword Search (Title + Content + Print Matter)
            // ================================================================
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";

                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        likePattern
                );

                Predicate contentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")),
                        likePattern
                );

                Predicate printMatterPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("printMatter")),
                        likePattern
                );

                // Combine with OR (keyword in title OR content OR printMatter)
                predicates.add(criteriaBuilder.or(
                        titlePredicate,
                        contentPredicate,
                        printMatterPredicate
                ));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter by priority.
     *
     * @param priority priority level (LOW, MEDIUM, HIGH, URGENT)
     * @return specification
     */
    public static Specification<Decision> hasPriority(String priority) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null || priority.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            try {
                DecisionPriority decisionPriority = DecisionPriority.valueOf(
                        priority.toUpperCase().replace("-", "_")
                );
                return criteriaBuilder.equal(root.get("priority"), decisionPriority);
            } catch (IllegalArgumentException e) {
                return criteriaBuilder.conjunction(); // Invalid priority - no filter
            }
        };
    }

    /**
     * Filter by committee (via JOIN).
     *
     * @param committeeName committee name
     * @return specification
     */
    public static Specification<Decision> hasCommittee(String committeeName) {
        return (root, query, criteriaBuilder) -> {
            if (committeeName == null || committeeName.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<Object, Object> committeeJoin = root.join("committee", JoinType.LEFT);

            // Try to match by name OR shortName
            Predicate namePredicate = criteriaBuilder.equal(
                    criteriaBuilder.lower(committeeJoin.get("name")),
                    committeeName.toLowerCase()
            );

            Predicate shortNamePredicate = criteriaBuilder.equal(
                    criteriaBuilder.lower(committeeJoin.get("shortName")),
                    committeeName.toLowerCase()
            );

            return criteriaBuilder.or(namePredicate, shortNamePredicate);
        };
    }

    /**
     * Filter by assignee (via JOIN).
     *
     * @param assigneeEmail assignee email
     * @return specification
     */
    public static Specification<Decision> hasAssignee(String assigneeEmail) {
        return (root, query, criteriaBuilder) -> {
            if (assigneeEmail == null || assigneeEmail.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<Object, Object> assigneeJoin = root.join("assignee", JoinType.LEFT);

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(assigneeJoin.get("email")),
                    assigneeEmail.toLowerCase()
            );
        };
    }

    /**
     * Filter overdue decisions.
     * Decisions with due_date < today AND status != COMPLETED
     *
     * @return specification
     */
    public static Specification<Decision> isOverdue() {
        return (root, query, criteriaBuilder) -> {
            LocalDate today = LocalDate.now();

            Predicate dueDatePast = criteriaBuilder.lessThan(
                    root.get("dueDate"),
                    today
            );

            Predicate notCompleted = criteriaBuilder.notEqual(
                    root.get("status"),
                    DecisionStatus.COMPLETED
            );

            Predicate hasDueDate = criteriaBuilder.isNotNull(root.get("dueDate"));

            return criteriaBuilder.and(hasDueDate, dueDatePast, notCompleted);
        };
    }

    /**
     * Filter decisions created by specific user.
     *
     * @param userId user UUID as string
     * @return specification
     */
    public static Specification<Decision> createdBy(String userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null || userId.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("createdBy").as(String.class),
                    userId
            );
        };
    }

    /**
     * Filter decisions with assignee (any assignee).
     *
     * @return specification
     */
    public static Specification<Decision> hasAnyAssignee() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("assignee"));
    }

    /**
     * Filter decisions without assignee.
     *
     * @return specification
     */
    public static Specification<Decision> hasNoAssignee() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("assignee"));
    }

    /**
     * Filter by completion status.
     *
     * @param completed true for completed, false for not completed
     * @return specification
     */
    public static Specification<Decision> isCompleted(boolean completed) {
        return (root, query, criteriaBuilder) -> {
            if (completed) {
                return criteriaBuilder.equal(root.get("status"), DecisionStatus.COMPLETED);
            } else {
                return criteriaBuilder.notEqual(root.get("status"), DecisionStatus.COMPLETED);
            }
        };
    }

    /**
     * Combine multiple specifications with AND logic.
     *
     * @param specs specifications to combine
     * @return combined specification
     */
    @SafeVarargs
    public static Specification<Decision> combineWithAnd(Specification<Decision>... specs) {
        Specification<Decision> result = Specification.where(null);
        for (Specification<Decision> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }

    /**
     * Filter decisions that have ANY of the specified departments.
     *
     * @param departmentNames list of department names
     * @return specification
     */
    public static Specification<Decision> hasDepartments(List<String> departmentNames) {
        return (root, query, criteriaBuilder) -> {
            if (departmentNames == null || departmentNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Join<Decision, Department> deptJoin = root.join("responsibleDepartments", JoinType.LEFT);

            // Create OR conditions for all department names
            List<Predicate> deptPredicates = new ArrayList<>();
            for (String deptName : departmentNames) {
                Predicate namePredicate = criteriaBuilder.equal(
                        criteriaBuilder.lower(deptJoin.get("name")),
                        deptName.toLowerCase()
                );

                Predicate shortNamePredicate = criteriaBuilder.equal(
                        criteriaBuilder.lower(deptJoin.get("shortName")),
                        deptName.toLowerCase()
                );

                deptPredicates.add(criteriaBuilder.or(namePredicate, shortNamePredicate));
            }

            return criteriaBuilder.or(deptPredicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter decisions that have ALL of the specified departments.
     *
     * @param departmentNames list of department names
     * @return specification
     */
    public static Specification<Decision> hasAllDepartments(List<String> departmentNames) {
        return (root, query, criteriaBuilder) -> {
            if (departmentNames == null || departmentNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // This is more complex - requires subquery
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Decision> subRoot = subquery.from(Decision.class);
            Join<Decision, Department> subDeptJoin = subRoot.join("responsibleDepartments");

            subquery.select(criteriaBuilder.count(subDeptJoin))
                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));

            // Decision must have at least as many matching departments as requested
            return criteriaBuilder.greaterThanOrEqualTo(
                    subquery.getSelection(),
                    (long) departmentNames.size()
            );
        };
    }
}