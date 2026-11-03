package com.civicshield.platform.audit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuditEventRequest(
        @NotBlank @Size(max = 255) String action,
        @Size(max = 10000) String details
) {
}
