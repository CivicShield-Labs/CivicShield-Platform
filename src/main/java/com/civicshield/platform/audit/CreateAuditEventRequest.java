package com.civicshield.platform.audit;

import jakarta.validation.constraints.NotBlank;

public record CreateAuditEventRequest(
        @NotBlank String action,
        @NotBlank String actor,
        String details
) {
}
