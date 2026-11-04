package com.civicshield.platform.security;

public final class AdminCredentialPolicy {

    private static final int MIN_PASSWORD_LENGTH = 32;
    private static final String KNOWN_PLACEHOLDER = "change-this-before-any-deployment";

    private AdminCredentialPolicy() {
    }

    public static void requireStrongPassword(String password) {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || KNOWN_PLACEHOLDER.equals(password)) {
            throw new IllegalStateException(
                    "Set CIVICSHIELD_ADMIN_PASSWORD to a non-placeholder value of at least 32 characters."
            );
        }
    }
}
