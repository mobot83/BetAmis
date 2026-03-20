package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.event.PredictionSubmitted;
import com.betamis.prediction.domain.exception.PredictionAlreadySubmittedException;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.in.SubmitPrediction;
import com.betamis.prediction.domain.port.out.EventPublisher;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class SubmitPredictionUseCase implements SubmitPrediction {

    private final PredictionRepository predictionRepository;
    private final EventPublisher eventPublisher;

    @Inject
    public SubmitPredictionUseCase(PredictionRepository predictionRepository, EventPublisher eventPublisher) {
        this.predictionRepository = predictionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String execute(String matchId, String userId, Score score, Instant kickOffTime) {
        if (predictionRepository.existsByUserIdAndMatchId(userId, matchId)) {
            throw new PredictionAlreadySubmittedException(
                "User %s already submitted a prediction for match %s".formatted(userId, matchId)
            );
        }
        var prediction = Prediction.submit(matchId, userId, score, kickOffTime);
        predictionRepository.save(prediction);
        prediction.pullDomainEvents().forEach(e -> {
            if (e instanceof PredictionSubmitted event) {
                eventPublisher.publish(event);
            }
        });
        return prediction.getId();
    }
}
