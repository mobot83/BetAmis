package com.betamis.match.infrastructure.messaging;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchStarted;
import com.betamis.match.domain.port.out.MatchEventPublisher;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaMatchEventPublisher implements MatchEventPublisher {

    @Inject
    @Channel("match-started")
    Emitter<MatchStarted> matchStartedEmitter;

    @Inject
    @Channel("match-finished")
    Emitter<MatchFinished> matchFinishedEmitter;

    @Override
    public void publish(MatchStarted event) {
        matchStartedEmitter.send(event);
    }

    @Override
    public void publish(MatchFinished event) {
        matchFinishedEmitter.send(event);
    }
}
