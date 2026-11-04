package com.civicshield.platform.incident;

import com.civicshield.platform.audit.AuditEvent;
import com.civicshield.platform.audit.AuditEventRepository;
import com.civicshield.platform.security.SecurityConfiguration;
import com.civicshield.platform.security.AgencyAccessPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import({SecurityConfiguration.class, AgencyAccessPolicy.class})
@TestPropertySource(properties = {
        "CIVICSHIELD_ADMIN_USER=agency-admin",
        "CIVICSHIELD_ADMIN_PASSWORD=test-only-admin-password-longer-than-32-characters",
        "CIVICSHIELD_ADMIN_AGENCY_ID=agency-a"
})
class IncidentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentRepository incidentRepository;

    @MockitoBean
    private AuditEventRepository auditEventRepository;

    @Test
    void rejectsAnonymousApiRequests() throws Exception {
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsConfiguredAdministratorCredentials() throws Exception {
        when(incidentRepository.findAllByAgencyId("agency-a")).thenReturn(List.of());
        mockMvc.perform(get("/api/incidents")
                        .with(httpBasic("agency-admin", "test-only-admin-password-longer-than-32-characters")))
                .andExpect(status().isOk());
        org.mockito.Mockito.verify(incidentRepository).findAllByAgencyId("agency-a");
    }

    @Test
    @WithMockUser(username = "agency-admin")
    void rejectsIncidentCreationForAnAgencyOutsideTheAuthenticatedScope() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"agencyId":"agency-b","incidentType":"FIRE"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentRepository, auditEventRepository);
    }

    @Test
    @WithMockUser(username = "agency-admin")
    void createsWithinTheAuthenticatedAgencyAndRecordsTheAuthenticatedActor() throws Exception {
        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(auditEventRepository.save(any(AuditEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"agencyId":"agency-a","incidentType":"FIRE"}
                                """))
                .andExpect(status().isCreated());

        verify(incidentRepository).save(argThat(incident -> "agency-a".equals(incident.getAgencyId())));
        verify(auditEventRepository).save(argThat(event -> "agency-admin".equals(event.getActor())));
    }

    @Test
    @WithMockUser(username = "agency-admin")
    void hidesIncidentsOutsideTheAuthenticatedAgencyAtTheRepositoryLookup() throws Exception {
        UUID incidentId = UUID.randomUUID();
        when(incidentRepository.findByIdAndAgencyId(incidentId, "agency-a")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/incidents/{id}", incidentId))
                .andExpect(status().isNotFound());

        org.mockito.Mockito.verify(incidentRepository).findByIdAndAgencyId(incidentId, "agency-a");
        verifyNoInteractions(auditEventRepository);
    }

    @Test
    @WithMockUser(username = "other-authenticated-user")
    void authenticatedAccountWithoutAnAgencyAssignmentIsRejected() throws Exception {
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentRepository, auditEventRepository);
    }

    @Test
    @WithMockUser(username = "agency-admin")
    void recordsAuditActorFromAuthenticatedPrincipal() throws Exception {
        UUID incidentId = UUID.randomUUID();
        when(incidentRepository.findByIdAndAgencyId(incidentId, "agency-a")).thenReturn(Optional.of(mock(Incident.class)));
        when(auditEventRepository.save(any(AuditEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/incidents/{id}/events", incidentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"STATUS_CHANGED","details":"reviewed","actor":"forged-actor"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.actor").value("agency-admin"))
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"));
    }

    @Test
    @WithMockUser(username = "agency-admin")
    void rejectsOversizedIncidentFieldsBeforePersistence() throws Exception {
        String oversized = "x".repeat(256);

        mockMvc.perform(post("/api/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"agencyId":"%s","incidentType":"FIRE"}
                                """.formatted(oversized)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentRepository, auditEventRepository);
    }

    @Test
    @WithMockUser(username = "agency-admin")
    void rejectsOversizedAuditDetailsBeforeLookupOrPersistence() throws Exception {
        String oversized = "x".repeat(10001);

        mockMvc.perform(post("/api/incidents/{id}/events", UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"STATUS_CHANGED","details":"%s"}
                                """.formatted(oversized)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentRepository, auditEventRepository);
    }

    @Test
    @WithMockUser(username = "agency-admin")
    void rejectsOversizedDeclaredRequestBody() throws Exception {
        String body = oversizedIncidentBody();

        mockMvc.perform(post("/api/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isPayloadTooLarge());

        verifyNoInteractions(incidentRepository, auditEventRepository);
    }

    private static String oversizedIncidentBody() {
        return "{\"agencyId\":\"agency\",\"incidentType\":\"FIRE\",\"padding\":\""
                + "x".repeat(70_000)
                + "\"}";
    }
}
