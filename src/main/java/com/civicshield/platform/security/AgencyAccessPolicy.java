package com.civicshield.platform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public final class AgencyAccessPolicy {

    private final String administrator;
    private final String agencyId;

    public AgencyAccessPolicy(
            @Value("${spring.security.user.name}") String administrator,
            @Value("${civicshield.security.admin-agency-id:}") String agencyId
    ) {
        this.administrator = administrator;
        this.agencyId = agencyId;
    }

    public String requireAgency(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        if (!administrator.equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No agency is assigned to this account");
        }
        if (agencyId == null || agencyId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Administrator agency scope is not configured");
        }
        return agencyId;
    }

    public String requireAgency(Authentication authentication, String requestedAgencyId) {
        String authorizedAgencyId = requireAgency(authentication);
        if (!authorizedAgencyId.equals(requestedAgencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requested agency is outside the authenticated account scope");
        }
        return authorizedAgencyId;
    }
}
