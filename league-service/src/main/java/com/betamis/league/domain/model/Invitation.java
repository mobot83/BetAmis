package com.betamis.league.domain.model;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Value object representing an invitation to a league.
 * An invitation code is 6 alphanumeric characters and expires after 7 days.
 */
public record Invitation(String code, Instant createdAt, Instant expiresAt) {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_DAYS = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static Invitation generate(Instant now) {
        String code = generateCode();
        return new Invitation(code, now, now.plus(EXPIRY_DAYS, ChronoUnit.DAYS));
    }

    /**
     * Create an invitation from persisted data (no code generation).
     */
    public static Invitation of(String code, Instant createdAt, Instant expiresAt) {
        return new Invitation(code, createdAt, expiresAt);
    }

    public boolean isValid(Instant now) {
        return now.isBefore(expiresAt);
    }

    private static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}

