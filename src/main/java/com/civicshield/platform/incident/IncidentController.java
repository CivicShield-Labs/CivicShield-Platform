package com.civicshield.platform.incident;

import com.civicshield.platform.audit.*;
import com.civicshield.platform.security.AgencyAccessPolicy;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final AuditEventRepository auditEventRepository;
    private final AgencyAccessPolicy agencyAccessPolicy;

    public IncidentController(
            IncidentRepository incidentRepository,
            AuditEventRepository auditEventRepository,
            AgencyAccessPolicy agencyAccessPolicy
    ) {
        this.incidentRepository = incidentRepository;
        this.auditEventRepository = auditEventRepository;
        this.agencyAccessPolicy = agencyAccessPolicy;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(Authentication authentication, @Valid @RequestBody CreateIncidentRequest request) {
        String agencyId = agencyAccessPolicy.requireAgency(authentication, request.agencyId());
        Incident incident = incidentRepository.save(
                new Incident(agencyId, request.incidentType())
        );

        auditEventRepository.save(
                new AuditEvent(
                        incident.getId(),
                        "INCIDENT_CREATED",
                        authentication.getName(),
                        "Incident created"
                )
        );

        return IncidentResponse.from(incident);
    }

    @GetMapping
    public List<IncidentResponse> list(Authentication authentication) {
        String agencyId = agencyAccessPolicy.requireAgency(authentication);
        return incidentRepository.findAllByAgencyId(agencyId)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable UUID id, Authentication authentication) {
        return IncidentResponse.from(findIncident(id, agencyAccessPolicy.requireAgency(authentication)));
    }

    @GetMapping("/{id}/events")
    public List<AuditEventResponse> events(@PathVariable UUID id, Authentication authentication) {
        findIncident(id, agencyAccessPolicy.requireAgency(authentication));

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
            Authentication authentication,
            @Valid @RequestBody CreateAuditEventRequest request
    ) {
        findIncident(id, agencyAccessPolicy.requireAgency(authentication));

        AuditEvent event = auditEventRepository.save(
                new AuditEvent(
                        id,
                        request.action(),
                        authentication.getName(),
                        request.details()
                )
        );

        return AuditEventResponse.from(event);
    }

    private Incident findIncident(UUID id, String agencyId) {
        return incidentRepository.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Incident not found"
                ));
    }
}
