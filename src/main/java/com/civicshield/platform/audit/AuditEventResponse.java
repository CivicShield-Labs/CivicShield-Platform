package com.civicshield.platform.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        UUID incidentId,
        String action,
        String actor,
        String details,
        Instant createdAt
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getIncidentId(),
                event.getAction(),
                event.getActor(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}
