package com.betamis.league.domain.event;

import java.time.Instant;
import java.util.UUID;

public record MemberJoined(
        String id,
        String leagueId,
        String userId,
        Instant occurredAt) {

    public static MemberJoined of(String leagueId, String userId) {
        return new MemberJoined(
                UUID.randomUUID().toString(),
                leagueId,
                userId,
                Instant.now()
        );
    }
}

