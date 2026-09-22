package com.civicshield.platform.incident;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agency_id", nullable = false)
    private String agencyId;

    @Column(name = "incident_type", nullable = false)
    private String incidentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Incident() {
    }

    public Incident(String agencyId, String incidentType) {
        this.agencyId = agencyId;
        this.incidentType = incidentType;
        this.status = IncidentStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}
