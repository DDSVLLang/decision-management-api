package de.langen.beschlussservice.infrastructure.persistance.specification;


import de.langen.beschlussservice.domain.entity.Decision;
import de.langen.beschlussservice.domain.entity.DecisionStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DecisionSpecification {

    public static Specification<Decision> withFilters(
            String status,
            String topic,
            String department,
            LocalDate dateFrom,
            LocalDate dateTo,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                        DecisionStatus.valueOf(status.toUpperCase().replace("-", "_"))
                ));
            }

            if (topic != null && !topic.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("topic"), topic));
            }

            if (department != null && !department.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("responsibleDepartment"),
                        department
                ));
            }

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
                predicates.add(criteriaBuilder.or(titlePredicate, contentPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

