package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.exception.PredictionAlreadySubmittedException;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.in.SubmitPrediction;
import com.betamis.prediction.domain.port.out.PredictionRepository;

import java.time.Instant;

public class SubmitPredictionUseCase implements SubmitPrediction {
    private final PredictionRepository predictionRepository;

    public SubmitPredictionUseCase(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @Override
    public void execute(String matchId, String userId, Score score, Instant kickOffTime) {
        if (predictionRepository.existsByUserIdAndMatchId(userId, matchId)) {
            throw new PredictionAlreadySubmittedException(
                "User %s already submitted a prediction for match %s".formatted(userId, matchId)
            );
        }
        var prediction = Prediction.submit(matchId, userId, score, kickOffTime);
        predictionRepository.save(prediction);
        // Right now events are not published but should be later on
        prediction.pullDomainEvents();
    }
}
