package com.civicshield.platform.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(
        @NotBlank @Size(max = 255) String agencyId,
        @NotBlank @Size(max = 255) String incidentType
) {
}
