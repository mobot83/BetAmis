package com.betamis.prediction.infrastructure.messaging;

import com.betamis.prediction.domain.event.PredictionSubmitted;
import com.betamis.prediction.domain.port.out.EventPublisher;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaPredictionEventPublisher implements EventPublisher {

    private final Emitter<com.betamis.prediction.event.PredictionSubmitted> predictionSubmittedEmitter;

    @Inject
    public KafkaPredictionEventPublisher(
            @Channel("prediction-submitted") Emitter<com.betamis.prediction.event.PredictionSubmitted> predictionSubmittedEmitter) {
        this.predictionSubmittedEmitter = predictionSubmittedEmitter;
    }

    @Override
    public void publish(PredictionSubmitted event) {
        var avroScore = com.betamis.prediction.event.Score.newBuilder()
                .setHomeTeamScore(event.score().homeTeamScore())
                .setAwayTeamScore(event.score().awayTeamScore())
                .build();
        var avroEvent = com.betamis.prediction.event.PredictionSubmitted.newBuilder()
                .setId(event.id())
                .setPredictionId(event.predictionId())
                .setMatchId(event.matchId())
                .setUserId(event.userId())
                .setScore(avroScore)
                .setOccurredAt(event.occurredAt())
                .build();
        predictionSubmittedEmitter.send(avroEvent)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish PredictionSubmitted event for prediction %s",
                                event.predictionId());
                    }
                });
    }
}
