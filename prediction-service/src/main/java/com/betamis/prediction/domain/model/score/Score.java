package com.betamis.prediction.domain.model.score;

public record Score(int homeTeamScore, int awayTeamScore) {
    // validation
    public Score {
        if (homeTeamScore < 0 || awayTeamScore < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
        if (homeTeamScore > 100 || awayTeamScore > 100) {
            throw new IllegalArgumentException("Score cannot be greater than 100");
        }
    }
}
