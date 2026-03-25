package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.event.PredictionClosed;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.prediction.PredictionStatus;
import com.betamis.prediction.domain.port.in.ClosePrediction;
import com.betamis.prediction.domain.port.out.EventPublisher;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ClosePredictionUseCase implements ClosePrediction {

    private final PredictionRepository predictionRepository;
    private final EventPublisher eventPublisher;
    private final Counter predictionsClosedCounter;

    @Inject
    public ClosePredictionUseCase(PredictionRepository predictionRepository,
                                  EventPublisher eventPublisher,
                                  MeterRegistry registry) {
        this.predictionRepository = predictionRepository;
        this.eventPublisher = eventPublisher;
        this.predictionsClosedCounter = registry.counter("betamis_predictions_closed_total");
    }

    @Override
    public void close(String matchId) {
        List<Prediction> predictions = predictionRepository.findByMatchId(matchId);
        for (Prediction prediction : predictions) {
            if (prediction.getStatus() == PredictionStatus.CLOSED) {
                continue;
            }
            prediction.close();
            predictionRepository.update(prediction);
        }
        eventPublisher.publish(PredictionClosed.of(matchId));
        predictionsClosedCounter.increment();
    }
}
