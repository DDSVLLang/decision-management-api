package de.langen.decision_service.domain.repository;

import de.langen.decision_service.domain.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Committee entity operations.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Repository
public interface CommitteeRepository extends JpaRepository<Committee, UUID> {

    /**
     * Find committee by name
     * @param name committee name
     * @return optional committee
     */
    Optional<Committee> findByName(String name);

    /**
     * Find committee by short name
     * @param shortName short name
     * @return optional committee
     */
    Optional<Committee> findByShortName(String shortName);

    /**
     * Find all active committees
     * @return list of active committees
     */
    List<Committee> findByActiveTrue();

    /**
     * Check if committee exists by name
     * @param name committee name
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Check if committee exists by short name
     * @param shortName short name
     * @return true if exists
     */
    boolean existsByShortName(String shortName);

    /**
     * Find committees by name containing (case-insensitive search)
     * @param name search term
     * @return list of matching committees
     */
    @Query("SELECT c FROM Committee c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Committee> findByNameContainingIgnoreCase(String name);

    /**
     * Count active committees
     * @return count of active committees
     */
    long countByActiveTrue();
}