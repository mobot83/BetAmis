package com.betamis.match.infrastructure.rest;

import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;

import java.time.Instant;

public record MatchResponse(
        String id,
        String homeTeamId,
        String awayTeamId,
        Integer homeTeamScore,
        Integer awayTeamScore,
        String status,
        Instant kickoffAt
) {
    public static MatchResponse from(Match match) {
        boolean hasScore = match.getStatus() != MatchStatus.PLANNED;
        return new MatchResponse(
                match.getId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                hasScore ? match.getHomeTeamScore() : null,
                hasScore ? match.getAwayTeamScore() : null,
                match.getStatus().name(),
                match.getKickoffAt().orElse(null)
        );
    }
}
