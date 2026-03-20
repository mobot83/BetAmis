package com.betamis.scoring.infrastructure.messaging;

import com.betamis.league.event.MemberJoined;
import com.betamis.scoring.domain.port.in.TrackLeagueMembership;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class KafkaLeagueMemberConsumer {

    private final TrackLeagueMembership trackLeagueMembership;

    @Inject
    public KafkaLeagueMemberConsumer(TrackLeagueMembership trackLeagueMembership) {
        this.trackLeagueMembership = trackLeagueMembership;
    }

    @Incoming("league-member-joined")
    public void consume(MemberJoined event) {
        Log.infof("Received MemberJoined: user %s joined league %s", event.getUserId(), event.getLeagueId());
        try {
            trackLeagueMembership.track(event.getUserId(), event.getLeagueId());
        } catch (Exception e) {
            Log.errorf(e, "Failed to track membership for user %s in league %s — skipping",
                    event.getUserId(), event.getLeagueId());
        }
    }
}
