package com.betamis.league.domain.model;

import java.time.Instant;

/**
 * Value object representing a league member.
 */
public record Membership(String userId, Instant joinedAt) {

    public Membership {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
        if (joinedAt == null) {
            throw new IllegalArgumentException("joinedAt cannot be null");
        }
    }
}

