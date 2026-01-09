package de.langen.beschlussservice.domain.repository;


import de.langen.beschlussservice.domain.entity.Decision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, UUID>, JpaSpecificationExecutor<Decision> {
    Optional<Decision> findById(UUID id);
    Page<Decision> findByAssigneeId(UUID id, Pageable pageable);
}

