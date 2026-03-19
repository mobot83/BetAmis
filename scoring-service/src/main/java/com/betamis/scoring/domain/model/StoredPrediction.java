package com.betamis.scoring.domain.model;

public record StoredPrediction(
        String predictionId,
        String matchId,
        String userId,
        FinalScore predictedScore
) {
    public StoredPrediction {
        if (predictionId == null || predictionId.isBlank()) throw new IllegalArgumentException("predictionId must not be blank");
        if (matchId == null || matchId.isBlank()) throw new IllegalArgumentException("matchId must not be blank");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
        if (predictedScore == null) throw new IllegalArgumentException("predictedScore must not be null");
    }
}
