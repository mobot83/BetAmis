package com.betamis.scoring.domain.model;

public record ScoringResult(
        String predictionId,
        String matchId,
        String userId,
        int points
) {
    public ScoringResult {
        if (predictionId == null || predictionId.isBlank()) throw new IllegalArgumentException("predictionId must not be blank");
        if (matchId == null || matchId.isBlank()) throw new IllegalArgumentException("matchId must not be blank");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
        if (points < 0) throw new IllegalArgumentException("points must be >= 0");
    }

    public static ScoringResult calculate(StoredPrediction prediction, FinalScore actual) {
        int pts = computePoints(prediction.predictedScore(), actual);
        return new ScoringResult(prediction.predictionId(), prediction.matchId(), prediction.userId(), pts);
    }

    private static int computePoints(FinalScore predicted, FinalScore actual) {
        if (predicted.homeTeamScore() == actual.homeTeamScore()
                && predicted.awayTeamScore() == actual.awayTeamScore()) {
            return 3;
        }
        boolean correctOutcome = sameOutcome(predicted, actual);
        if (correctOutcome && !actual.isDraw()
                && predicted.goalDifference() == actual.goalDifference()) {
            return 2;
        }
        if (correctOutcome) {
            return 1;
        }
        return 0;
    }

    private static boolean sameOutcome(FinalScore a, FinalScore b) {
        return (a.isHomeWin() && b.isHomeWin())
                || (a.isAwayWin() && b.isAwayWin())
                || (a.isDraw() && b.isDraw());
    }
}
