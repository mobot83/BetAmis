package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.exception.PredictionNotFoundException;
import com.betamis.prediction.domain.exception.PredictionNotOwnedException;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.in.UpdatePrediction;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;

@ApplicationScoped
public class UpdatePredictionUseCase implements UpdatePrediction {

    private final PredictionRepository predictionRepository;

    public UpdatePredictionUseCase(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @Override
    @Transactional
    public Prediction execute(String predictionId, String userId, Score newScore, Instant kickoffAt) {
        Prediction prediction = predictionRepository.findById(predictionId);
        if (prediction == null) {
            throw new PredictionNotFoundException(
                    "Prediction %s not found".formatted(predictionId));
        }
        if (!prediction.getUserId().equals(userId)) {
            throw new PredictionNotOwnedException(
                    "User %s does not own prediction %s".formatted(userId, predictionId));
        }
        prediction.update(newScore, kickoffAt, Instant.now());
        predictionRepository.update(prediction);
        return prediction;
    }
}
