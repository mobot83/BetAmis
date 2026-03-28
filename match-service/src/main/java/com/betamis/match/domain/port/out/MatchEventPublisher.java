package com.betamis.match.domain.port.out;

import com.betamis.match.domain.event.MatchFinished;
import com.betamis.match.domain.event.MatchScheduled;
import com.betamis.match.domain.event.MatchStarted;

public interface MatchEventPublisher {

    void publish(MatchScheduled event);

    void publish(MatchStarted event);

    void publish(MatchFinished event);
}
