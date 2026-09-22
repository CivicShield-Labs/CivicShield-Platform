CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    agency_id VARCHAR(255) NOT NULL,
    incident_type VARCHAR(255) NOT NULL,
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_audit_incident
        FOREIGN KEY (incident_id)
        REFERENCES incidents(id)
);

CREATE INDEX idx_audit_events_incident_id
    ON audit_events(incident_id);

CREATE INDEX idx_audit_events_created_at
    ON audit_events(created_at);
