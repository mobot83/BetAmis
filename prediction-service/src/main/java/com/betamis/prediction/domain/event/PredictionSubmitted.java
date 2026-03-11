package com.betamis.prediction.domain.event;

import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.score.Score;

import java.time.Instant;

public record PredictionSubmitted(
    String id,
    String predictionId,
    String matchId,
    String userId,
    Score score,
    Instant occurredAt
) {
    public static PredictionSubmitted of(Prediction prediction) {
        return new PredictionSubmitted(
            prediction.getId(),
            prediction.getId(),
            prediction.getMatchId(),
            prediction.getUserId(),
            prediction.getScore(),
            Instant.now()
        );
    }
}
