package com.civicshield.platform.incident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findAllByAgencyId(String agencyId);

    Optional<Incident> findByIdAndAgencyId(UUID id, String agencyId);
}
