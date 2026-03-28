package com.betamis.prediction.domain.model.ranking;

public record RankingEntry(int rank, String userId, long totalPoints) {

    public RankingEntry {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
        if (totalPoints < 0) throw new IllegalArgumentException("totalPoints must be >= 0");
        if (rank < 1) throw new IllegalArgumentException("rank must be >= 1");
    }
}
