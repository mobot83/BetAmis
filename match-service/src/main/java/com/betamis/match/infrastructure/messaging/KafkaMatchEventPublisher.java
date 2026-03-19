package com.betamis.match.infrastructure.messaging;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaMatchEventPublisher implements MatchEventPublisher {

    @Inject
    @Channel("match-started")
    Emitter<com.betamis.match.event.MatchStarted> matchStartedEmitter;

    @Inject
    @Channel("match-finished")
    Emitter<com.betamis.match.event.MatchFinished> matchFinishedEmitter;

    @Override
    public void publish(MatchStarted event) {
        var avroEvent = com.betamis.match.event.MatchStarted.newBuilder()
                .setId(event.id())
                .setMatchId(event.matchId())
                .setHomeTeamId(event.homeTeamId())
                .setAwayTeamId(event.awayTeamId())
                .setOccurredAt(event.occurredAt())
                .build();
        matchStartedEmitter.send(avroEvent)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish MatchStarted event for match %s", event.matchId());
                    }
                });
    }

    @Override
    public void publish(MatchFinished event) {
        var score = com.betamis.match.event.Score.newBuilder()
                .setHomeTeamScore(event.homeTeamScore())
                .setAwayTeamScore(event.awayTeamScore())
                .build();
        var avroEvent = com.betamis.match.event.MatchFinished.newBuilder()
                .setId(event.id())
                .setMatchId(event.matchId())
                .setHomeTeamId(event.homeTeamId())
                .setAwayTeamId(event.awayTeamId())
                .setFinalScore(score)
                .setOccurredAt(event.occurredAt())
                .build();
        matchFinishedEmitter.send(avroEvent)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish MatchFinished event for match %s", event.matchId());
                    }
                });
    }
}
