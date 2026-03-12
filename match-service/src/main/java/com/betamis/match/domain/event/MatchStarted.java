package com.betamis.match.domain.event;

import java.time.Instant;

public record MatchStarted(String id, String matchId, Instant occurredAt) {
    public static MatchStarted of(String matchId) {
        return new MatchStarted(java.util.UUID.randomUUID().toString(), matchId, Instant.now());
    }
}
