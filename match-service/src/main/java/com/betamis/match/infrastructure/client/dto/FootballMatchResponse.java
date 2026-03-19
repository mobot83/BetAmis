package com.betamis.match.infrastructure.client.dto;

import java.time.Instant;

public record FootballMatchResponse(
        long id,
        String status,
        Instant utcDate,
        FootballTeamDto homeTeam,
        FootballTeamDto awayTeam,
        FootballScoreDto score
) {}
