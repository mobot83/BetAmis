package com.betamis.scoring.infrastructure.messaging;

import com.betamis.prediction.event.PredictionSubmitted;
import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.port.in.StorePrediction;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class KafkaPredictionConsumer {

    private final StorePrediction storePrediction;

    @Inject
    public KafkaPredictionConsumer(StorePrediction storePrediction) {
        this.storePrediction = storePrediction;
    }

    @Incoming("prediction-submitted")
    public void consume(PredictionSubmitted event) {
        Log.infof("Received PredictionSubmitted for prediction %s, match %s", event.getPredictionId(), event.getMatchId());
        try {
            StoredPrediction prediction = new StoredPrediction(
                    event.getPredictionId(),
                    event.getMatchId(),
                    event.getUserId(),
                    new FinalScore(event.getScore().getHomeTeamScore(), event.getScore().getAwayTeamScore())
            );
            storePrediction.store(prediction);
        } catch (Exception e) {
            Log.errorf(e, "Failed to store prediction %s for match %s — skipping",
                    event.getPredictionId(), event.getMatchId());
        }
    }
}
