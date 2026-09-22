package com.civicshield.platform.incident;

import jakarta.validation.constraints.NotBlank;

public record CreateIncidentRequest(
        @NotBlank String agencyId,
        @NotBlank String incidentType
) {
}
