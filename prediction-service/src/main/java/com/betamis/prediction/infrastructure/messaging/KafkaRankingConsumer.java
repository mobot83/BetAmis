package com.betamis.prediction.infrastructure.messaging;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import com.betamis.prediction.infrastructure.sse.RankingBroadcaster;
import com.betamis.ranking.event.RankingUpdated;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class KafkaRankingConsumer {

    private final RankingBroadcaster broadcaster;

    @Inject
    public KafkaRankingConsumer(RankingBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Incoming("ranking-updated")
    public void consume(RankingUpdated event) {
        Log.debugf("RankingUpdated: league=%s user=%s rank=%d points=%d",
                event.getLeagueId(), event.getUserId(), event.getRank(), event.getTotalPoints());
        try {
            broadcaster.publish(event.getLeagueId(),
                    new RankingEntry(event.getRank(), event.getUserId(), event.getTotalPoints()));
        } catch (Exception e) {
            Log.errorf(e, "Failed to broadcast RankingUpdated for league %s — skipping", event.getLeagueId());
        }
    }
}
