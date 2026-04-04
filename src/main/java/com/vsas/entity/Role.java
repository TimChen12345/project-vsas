package com.vsas.entity;

/**
 * Stored on registered users. Anonymous visitors are not stored (no JWT).
 *
 * <p>GUEST exists only for legacy rows from an older schema; it is migrated to USER on startup and
 * treated as USER for Spring Security.
 */
public enum Role {
    /** @deprecated Legacy DB value only; normalized to USER at startup. */
    @Deprecated
    GUEST,
    USER,
    ADMIN
}
