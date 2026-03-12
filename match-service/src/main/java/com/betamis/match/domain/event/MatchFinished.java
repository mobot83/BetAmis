package com.betamis.match.domain.event;

import java.time.Instant;

public record MatchFinished(String id, String matchId, Instant occurredAt) {
    public static MatchFinished of(String matchId) {
        return new MatchFinished(java.util.UUID.randomUUID().toString(), matchId, Instant.now());
    }
}
