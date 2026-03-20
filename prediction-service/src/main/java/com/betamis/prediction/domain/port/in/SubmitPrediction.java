package com.betamis.prediction.domain.port.in;

import com.betamis.prediction.domain.model.score.Score;

import java.time.Instant;

public interface SubmitPrediction {
    String execute(String matchId, String userId, Score score, Instant kickOffTime);
}
