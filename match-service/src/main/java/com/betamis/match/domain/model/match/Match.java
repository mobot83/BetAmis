package com.betamis.match.domain.model.match;

import java.util.Objects;
import java.util.UUID;

public class Match {
    private final String id;
    private final long externalId;
    private final String homeTeamId;
    private final String awayTeamId;
    private final int homeTeamScore;
    private final int awayTeamScore;
    private final MatchStatus status;

    public Match(String id, long externalId, String homeTeamId, String awayTeamId, int homeTeamScore, int awayTeamScore, MatchStatus status) {
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
    }

    public String getId() {
        return id;
    }

    public long getExternalId() {
        return externalId;
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

    public Match withUpdate(int homeTeamScore, int awayTeamScore, MatchStatus status) {
        return new Match(this.id, this.externalId, this.homeTeamId, this.awayTeamId, homeTeamScore, awayTeamScore, status);
    }

    public static Match create(String homeTeamId, String awayTeamId) {
        return new Match(UUID.randomUUID().toString(), 0L, homeTeamId, awayTeamId, 0, 0, MatchStatus.PLANNED);
    }

    public static Match fromExternal(long externalId, String homeTeamId, String awayTeamId, int homeScore, int awayScore, MatchStatus status) {
        return new Match(UUID.randomUUID().toString(), externalId, homeTeamId, awayTeamId, homeScore, awayScore, status);
    }
}
