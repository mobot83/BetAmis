package com.betamis.notification.infrastructure.messaging;

import com.betamis.match.event.MatchScheduled;
import com.betamis.notification.domain.port.in.ScheduleMatchNotifications;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class KafkaMatchScheduledConsumer {

    private final ScheduleMatchNotifications scheduleMatchNotifications;

    public KafkaMatchScheduledConsumer(ScheduleMatchNotifications scheduleMatchNotifications) {
        this.scheduleMatchNotifications = scheduleMatchNotifications;
    }

    @Incoming("match-scheduled")
    public void consume(MatchScheduled event) {
        Log.debugf("MatchScheduled received: matchId=%s kickoffAt=%s", event.getMatchId(), event.getKickoffAt());
        scheduleMatchNotifications.schedule(
                event.getMatchId(),
                event.getHomeTeamId(),
                event.getAwayTeamId(),
                event.getKickoffAt()
        );
    }
}
