package com.betamis.match.domain.event;

import java.time.Instant;

public record MatchFinished(String id,
                            String matchId,
                            String homeTeamId,
                            String awayTeamId,
                            int homeTeamScore,
                            int awayTeamScore,
                            Instant occurredAt) {

    public static MatchFinished of(String matchId, String homeTeamId, String awayTeamId,
                                   int homeTeamScore, int awayTeamScore) {
        return new MatchFinished(
                java.util.UUID.randomUUID().toString(),
                matchId,
                homeTeamId,
                awayTeamId,
                homeTeamScore,
                awayTeamScore,
                Instant.now()
        );
    }
}
