package com.civicshield.platform.incident;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        String agencyId,
        String incidentType,
        IncidentStatus status,
        Instant createdAt
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getAgencyId(),
                incident.getIncidentType(),
                incident.getStatus(),
                incident.getCreatedAt()
        );
    }
}
