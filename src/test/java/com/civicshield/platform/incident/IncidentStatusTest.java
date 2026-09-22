package com.civicshield.platform.incident;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncidentStatusTest {

    @Test
    void supportsExpectedLifecycleStates() {
        assertEquals(4, IncidentStatus.values().length);
        assertEquals(IncidentStatus.OPEN, IncidentStatus.valueOf("OPEN"));
        assertEquals(IncidentStatus.ACTIVE, IncidentStatus.valueOf("ACTIVE"));
        assertEquals(IncidentStatus.RESOLVED, IncidentStatus.valueOf("RESOLVED"));
        assertEquals(IncidentStatus.CANCELLED, IncidentStatus.valueOf("CANCELLED"));
    }
}
