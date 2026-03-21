package com.betamis.league.domain.port.out;

import com.betamis.league.domain.event.LeagueCreated;
import com.betamis.league.domain.event.MemberJoined;

public interface LeagueEventPublisher {
    void publish(LeagueCreated event);
    void publish(MemberJoined event);
}

