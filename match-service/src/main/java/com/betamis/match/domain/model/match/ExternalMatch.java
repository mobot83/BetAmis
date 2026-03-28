package com.betamis.match.domain.model.match;

import java.time.Instant;

/**
 * Value object carrying the raw data fetched from an external match data provider.
 * The {@code status} field holds the provider-specific string (e.g. "IN_PLAY") and
 * is mapped to {@link MatchStatus} by the application layer.
 */
public record ExternalMatch(
        long externalId,
        String status,
        String homeTeamId,
        String awayTeamId,
        int homeScore,
        int awayScore,
        Instant kickoffAt
) {}
