package com.betamis.league.infrastructure.messaging;

import com.betamis.league.domain.event.LeagueCreated;
import com.betamis.league.domain.event.MemberJoined;
import com.betamis.league.domain.port.out.LeagueEventPublisher;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaLeagueEventPublisher implements LeagueEventPublisher {

    private final Emitter<com.betamis.league.event.LeagueCreated> leagueCreatedEmitter;
    private final Emitter<com.betamis.league.event.MemberJoined> memberJoinedEmitter;

    @Inject
    public KafkaLeagueEventPublisher(
            @Channel("league-created") Emitter<com.betamis.league.event.LeagueCreated> leagueCreatedEmitter,
            @Channel("league-member-joined") Emitter<com.betamis.league.event.MemberJoined> memberJoinedEmitter) {
        this.leagueCreatedEmitter = leagueCreatedEmitter;
        this.memberJoinedEmitter = memberJoinedEmitter;
    }

    @Override
    public void publish(LeagueCreated event) {
        var avroEvent = com.betamis.league.event.LeagueCreated.newBuilder()
                .setId(event.id())
                .setLeagueId(event.leagueId())
                .setLeagueName(event.leagueName())
                .setOwnerId(event.ownerId())
                .setInvitationCode(event.invitationCode())
                .setOccurredAt(event.occurredAt())
                .build();
        leagueCreatedEmitter.send(avroEvent)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish LeagueCreated event for league %s", event.leagueId());
                    }
                });
    }

    @Override
    public void publish(MemberJoined event) {
        var avroEvent = com.betamis.league.event.MemberJoined.newBuilder()
                .setId(event.id())
                .setLeagueId(event.leagueId())
                .setUserId(event.userId())
                .setOccurredAt(event.occurredAt())
                .build();
        memberJoinedEmitter.send(avroEvent)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish MemberJoined event for league %s", event.leagueId());
                    }
                });
    }
}
