package com.betamis.league.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LeagueCreated(
        String id,
        String leagueId,
        String leagueName,
        String ownerId,
        String invitationCode,
        Instant occurredAt) {

    public static LeagueCreated of(String leagueId, String leagueName, String ownerId,
                                   String invitationCode, Instant occurredAt) {
        return new LeagueCreated(
                UUID.randomUUID().toString(),
                leagueId,
                leagueName,
                ownerId,
                invitationCode,
                occurredAt
        );
    }
}

