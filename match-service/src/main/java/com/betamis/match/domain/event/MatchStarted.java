package com.betamis.match.domain.event;

import java.time.Instant;

public record MatchStarted(String id,
                           String matchId,
                           String homeTeamId,
                           String awayTeamId,
                           Instant occurredAt) {

    public static MatchStarted of(String matchId, String homeTeamId, String awayTeamId) {
        return new MatchStarted(
                java.util.UUID.randomUUID().toString(),
                matchId,
                homeTeamId,
                awayTeamId,
                Instant.now()
        );
    }
}
