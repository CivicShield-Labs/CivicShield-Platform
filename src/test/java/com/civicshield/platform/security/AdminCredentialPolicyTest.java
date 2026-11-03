package com.civicshield.platform.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminCredentialPolicyTest {

    @Test
    void acceptsLongUniquePassword() {
        assertDoesNotThrow(() -> AdminCredentialPolicy.requireStrongPassword(
                "a-unique-admin-password-with-at-least-32-characters"
        ));
    }

    @Test
    void rejectsMissingPassword() {
        assertThrows(IllegalStateException.class,
                () -> AdminCredentialPolicy.requireStrongPassword(null));
    }

    @Test
    void rejectsShortPassword() {
        assertThrows(IllegalStateException.class,
                () -> AdminCredentialPolicy.requireStrongPassword("too-short"));
    }

    @Test
    void rejectsKnownDeploymentPlaceholder() {
        assertThrows(IllegalStateException.class,
                () -> AdminCredentialPolicy.requireStrongPassword("change-this-before-any-deployment"));
    }
}
