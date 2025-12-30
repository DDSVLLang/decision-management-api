package de.langen.beschlussservice.domain.repository;


import de.langen.beschlussservice.domain.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long>,
        JpaSpecificationExecutor<Decision> {
}

