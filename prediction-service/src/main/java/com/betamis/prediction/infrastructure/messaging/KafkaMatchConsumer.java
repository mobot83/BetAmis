package com.betamis.prediction.infrastructure.messaging;

import com.betamis.match.event.MatchStarted;
import com.betamis.prediction.domain.port.in.ClosePrediction;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class KafkaMatchConsumer {

    private final ClosePrediction closePrediction;

    @Inject
    public KafkaMatchConsumer(ClosePrediction closePrediction) {
        this.closePrediction = closePrediction;
    }

    @Incoming("match-started")
    public void consume(MatchStarted event) {
        Log.infof("Received MatchStarted for match %s — closing predictions", event.getMatchId());
        try {
            closePrediction.close(event.getMatchId());
        } catch (Exception e) {
            Log.errorf(e, "Failed to close predictions for match %s — skipping", event.getMatchId());
        }
    }
}
