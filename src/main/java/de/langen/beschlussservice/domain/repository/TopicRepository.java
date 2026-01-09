package de.langen.beschlussservice.domain.repository;

import de.langen.beschlussservice.domain.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Topic entity operations.
 *
 * @author Backend Team
 * @version 2.0
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    /**
     * Find topic by name
     * @param name topic name
     * @return optional topic
     */
    Optional<Topic> findByName(String name);

    /**
     * Find all active topics
     * @return list of active topics
     */
    List<Topic> findByActiveTrue();

    /**
     * Check if topic exists by name
     * @param name topic name
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Find topics by name containing (case-insensitive search)
     * @param name search term
     * @return list of matching topics
     */
    @Query("SELECT t FROM Topic t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Topic> findByNameContainingIgnoreCase(String name);

    /**
     * Count active topics
     * @return count of active topics
     */
    long countByActiveTrue();
}