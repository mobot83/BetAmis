package com.betamis.match.domain.model.match;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Match {
    private final String id;
    private final Long externalId;
    private final String homeTeamId;
    private final String awayTeamId;
    private final int homeTeamScore;
    private final int awayTeamScore;
    private final MatchStatus status;
    private final Instant kickoffAt;

    public Match(String id, Long externalId, String homeTeamId, String awayTeamId,
                 int homeTeamScore, int awayTeamScore, MatchStatus status, Instant kickoffAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Match id cannot be null or blank");
        }
        if (homeTeamId == null || homeTeamId.isBlank()) {
            throw new IllegalArgumentException("Home team id cannot be null or blank");
        }
        if (awayTeamId == null || awayTeamId.isBlank()) {
            throw new IllegalArgumentException("Away team id cannot be null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("Match status cannot be null");
        }
        if (homeTeamScore < 0) {
            throw new IllegalArgumentException("Home team score cannot be negative");
        }
        if (awayTeamScore < 0) {
            throw new IllegalArgumentException("Away team score cannot be negative");
        }
        this.id = id;
        this.externalId = externalId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamScore = awayTeamScore;
        this.status = status;
        this.kickoffAt = kickoffAt;
    }

    public String getId() {
        return id;
    }

    public Optional<Long> getExternalId() {
        return Optional.ofNullable(externalId);
    }

    public String getHomeTeamId() {
        return homeTeamId;
    }

    public String getAwayTeamId() {
        return awayTeamId;
    }

    public int getHomeTeamScore() {
        return homeTeamScore;
    }

    public int getAwayTeamScore() {
        return awayTeamScore;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public Optional<Instant> getKickoffAt() {
        return Optional.ofNullable(kickoffAt);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(id, match.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Match withUpdate(int homeTeamScore, int awayTeamScore, MatchStatus status, Instant kickoffAt) {
        return new Match(this.id, this.externalId, this.homeTeamId, this.awayTeamId,
                homeTeamScore, awayTeamScore, status, kickoffAt);
    }

    public static Match fromExternal(long externalId, String homeTeamId, String awayTeamId,
                                     int homeScore, int awayScore, MatchStatus status, Instant kickoffAt) {
        return new Match(UUID.randomUUID().toString(), externalId, homeTeamId, awayTeamId,
                homeScore, awayScore, status, kickoffAt);
    }
}
