package de.langen.decision_service.domain.repository;

import de.langen.decision_service.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Department entity operations.
 *
 * @author Yacine Sghairi
 * @version 2.0
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /**
     * Find department by name
     * @param name department name
     * @return optional department
     */
    Optional<Department> findByName(String name);

    /**
     * Find department by short name
     * @param shortName short name
     * @return optional department
     */
    Optional<Department> findByShortName(String shortName);

    /**
     * Find all active departments
     * @return list of active departments
     */
    List<Department> findByActiveTrue();

    /**
     * Check if department exists by name
     * @param name department name
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Check if department exists by short name
     * @param shortName short name
     * @return true if exists
     */
    boolean existsByShortName(String shortName);

    /**
     * Find departments by name containing (case-insensitive search)
     * @param name search term
     * @return list of matching departments
     */
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Department> findByNameContainingIgnoreCase(String name);

    /**
     * Find departments by head user
     * @param headUserId head user ID
     * @return list of departments
     */
    @Query("SELECT d FROM Department d WHERE d.headUser.id = :headUserId")
    List<Department> findByHeadUserId(UUID headUserId);

    /**
     * Count active departments
     * @return count of active departments
     */
    long countByActiveTrue();
}