package com.betamis.scoring.domain.model;

public record FinalScore(int homeTeamScore, int awayTeamScore) {

    public FinalScore {
        if (homeTeamScore < 0) throw new IllegalArgumentException("homeTeamScore must be >= 0");
        if (awayTeamScore < 0) throw new IllegalArgumentException("awayTeamScore must be >= 0");
    }

    public boolean isHomeWin() {
        return homeTeamScore > awayTeamScore;
    }

    public boolean isAwayWin() {
        return awayTeamScore > homeTeamScore;
    }

    public boolean isDraw() {
        return homeTeamScore == awayTeamScore;
    }

    public int goalDifference() {
        return homeTeamScore - awayTeamScore;
    }
}
