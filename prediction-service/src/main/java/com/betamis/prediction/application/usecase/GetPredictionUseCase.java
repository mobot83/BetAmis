package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.exception.PredictionNotFoundException;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.port.in.GetPrediction;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GetPredictionUseCase implements GetPrediction {

    private final PredictionRepository predictionRepository;

    public GetPredictionUseCase(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @Override
    public Prediction execute(String matchId, String userId) {
        return predictionRepository.findByUserIdAndMatchId(userId, matchId)
                .orElseThrow(() -> new PredictionNotFoundException(
                        "No prediction found for match %s".formatted(matchId)));
    }
}
