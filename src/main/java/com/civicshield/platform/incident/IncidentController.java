package com.civicshield.platform.incident;

import com.civicshield.platform.audit.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final AuditEventRepository auditEventRepository;

    public IncidentController(
            IncidentRepository incidentRepository,
            AuditEventRepository auditEventRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request) {
        Incident incident = incidentRepository.save(
                new Incident(request.agencyId(), request.incidentType())
        );

        auditEventRepository.save(
                new AuditEvent(
                        incident.getId(),
                        "INCIDENT_CREATED",
                        "system",
                        "Incident created"
                )
        );

        return IncidentResponse.from(incident);
    }

    @GetMapping
    public List<IncidentResponse> list() {
        return incidentRepository.findAll()
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable UUID id) {
        return IncidentResponse.from(findIncident(id));
    }

    @GetMapping("/{id}/events")
    public List<AuditEventResponse> events(@PathVariable UUID id) {
        findIncident(id);

        return auditEventRepository
                .findByIncidentIdOrderByCreatedAtAsc(id)
                .stream()
                .map(AuditEventResponse::from)
                .toList();
    }

    @PostMapping("/{id}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public AuditEventResponse event(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAuditEventRequest request
    ) {
        findIncident(id);

        AuditEvent event = auditEventRepository.save(
                new AuditEvent(
                        id,
                        request.action(),
                        request.actor(),
                        request.details()
                )
        );

        return AuditEventResponse.from(event);
    }

    private Incident findIncident(UUID id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Incident not found"
                ));
    }
}
