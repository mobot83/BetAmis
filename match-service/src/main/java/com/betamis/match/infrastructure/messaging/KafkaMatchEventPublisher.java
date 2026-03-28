package com.betamis.match.infrastructure.messaging;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchScheduled;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaMatchEventPublisher implements MatchEventPublisher {

    private final Emitter<com.betamis.match.event.MatchScheduled> matchScheduledEmitter;
    private final Emitter<com.betamis.match.event.MatchStarted> matchStartedEmitter;
    private final Emitter<com.betamis.match.event.MatchFinished> matchFinishedEmitter;

    public KafkaMatchEventPublisher(
            @Channel("match-scheduled") Emitter<com.betamis.match.event.MatchScheduled> matchScheduledEmitter,
            @Channel("match-started") Emitter<com.betamis.match.event.MatchStarted> matchStartedEmitter,
            @Channel("match-finished") Emitter<com.betamis.match.event.MatchFinished> matchFinishedEmitter) {
        this.matchScheduledEmitter = matchScheduledEmitter;
        this.matchStartedEmitter = matchStartedEmitter;
        this.matchFinishedEmitter = matchFinishedEmitter;
    }

    @Override
    public void publish(MatchScheduled event) {
        var avroEvent = com.betamis.match.event.MatchScheduled.newBuilder()
                .setId(event.id())
                .setMatchId(event.matchId())
                .setHomeTeamId(event.homeTeamId())
                .setAwayTeamId(event.awayTeamId())
                .setKickoffAt(event.kickoffAt())
                .setOccurredAt(event.occurredAt())
                .build();
        matchScheduledEmitter.send(avroEvent)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish MatchScheduled event for match %s", event.matchId());
                    }
                });
    }

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
