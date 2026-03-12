package com.betamis.prediction.domain.event;

import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.score.Score;

import java.time.Instant;
import java.util.UUID;

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
            UUID.randomUUID().toString(),
            prediction.getId(),
            prediction.getMatchId(),
            prediction.getUserId(),
            prediction.getScore(),
            Instant.now()
        );
    }
}
