package com.betamis.prediction.infrastructure.messaging;

import com.betamis.prediction.domain.event.PredictionClosed;
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
    private final Emitter<com.betamis.prediction.event.PredictionClosed> predictionClosedEmitter;

    @Inject
    public KafkaPredictionEventPublisher(
            @Channel("prediction-submitted") Emitter<com.betamis.prediction.event.PredictionSubmitted> predictionSubmittedEmitter,
            @Channel("prediction-closed") Emitter<com.betamis.prediction.event.PredictionClosed> predictionClosedEmitter) {
        this.predictionSubmittedEmitter = predictionSubmittedEmitter;
        this.predictionClosedEmitter = predictionClosedEmitter;
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

    @Override
    public void publish(PredictionClosed event) {
        var avroEvent = com.betamis.prediction.event.PredictionClosed.newBuilder()
                .setId(event.id())
                .setMatchId(event.matchId())
                .setOccurredAt(event.occurredAt())
                .build();

        predictionClosedEmitter.send(avroEvent)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish PredictionClosed event for match %s",
                                event.matchId());
                    }
                });
    }
}
