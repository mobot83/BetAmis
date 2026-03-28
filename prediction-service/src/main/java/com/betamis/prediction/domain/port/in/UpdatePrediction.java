package com.betamis.prediction.domain.port.in;

import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.score.Score;

import java.time.Instant;

public interface UpdatePrediction {
    Prediction execute(String predictionId, String userId, Score newScore, Instant kickoffAt);
}
