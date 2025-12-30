package de.langen.beschlussservice.domain.repository;

import de.langen.beschlussservice.domain.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommitteeRepository extends JpaRepository<Committee, UUID> {
}
