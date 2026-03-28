package com.betamis.prediction.domain.port.in;

import com.betamis.prediction.domain.model.prediction.Prediction;

public interface GetPrediction {
    Prediction execute(String matchId, String userId);
}
