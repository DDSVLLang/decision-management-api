package de.langen.decision_service.domain.repository;


import de.langen.decision_service.domain.entity.Decision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, UUID>, JpaSpecificationExecutor<Decision> {
    Optional<Decision> findFirstById(UUID id);
    Optional<Decision> findByIdAndDeletedFalse(UUID id);
}

