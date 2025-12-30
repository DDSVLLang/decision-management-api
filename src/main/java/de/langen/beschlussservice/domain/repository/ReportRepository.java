package de.langen.beschlussservice.domain.repository;

import de.langen.beschlussservice.domain.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Optional<Report> findByYear(String year);
}
