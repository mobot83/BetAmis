package com.betamis.match.domain.event;

import java.time.Instant;
import java.util.UUID;

public record MatchScheduled(String id,
                             String matchId,
                             String homeTeamId,
                             String awayTeamId,
                             Instant kickoffAt,
                             Instant occurredAt) {

    public static MatchScheduled of(String matchId, String homeTeamId, String awayTeamId,
                                    Instant kickoffAt, Instant occurredAt) {
        return new MatchScheduled(
                UUID.randomUUID().toString(),
                matchId,
                homeTeamId,
                awayTeamId,
                kickoffAt,
                occurredAt
        );
    }
}
