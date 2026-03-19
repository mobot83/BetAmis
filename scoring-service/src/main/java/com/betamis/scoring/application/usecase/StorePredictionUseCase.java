package com.betamis.scoring.application.usecase;

import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.port.in.StorePrediction;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StorePredictionUseCase implements StorePrediction {

    private final StoredPredictionRepository predictionRepository;

    @Inject
    public StorePredictionUseCase(StoredPredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @Override
    public void store(StoredPrediction prediction) {
        predictionRepository.save(prediction);
    }
}
