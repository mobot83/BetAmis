package com.betamis.scoring.domain.model;

public record UserRanking(String userId, String leagueId, long totalPoints, int rank) {

    public UserRanking {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
        if (leagueId == null || leagueId.isBlank()) throw new IllegalArgumentException("leagueId must not be blank");
        if (totalPoints < 0) throw new IllegalArgumentException("totalPoints must be >= 0");
        if (rank < 1) throw new IllegalArgumentException("rank must be >= 1");
    }
}
