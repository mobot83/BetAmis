package com.betamis.notification.infrastructure.messaging;

import com.betamis.match.event.MatchScheduled;
import com.betamis.notification.domain.port.in.ScheduleMatchNotifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaMatchScheduledConsumerTest {

    @Mock
    ScheduleMatchNotifications scheduleMatchNotifications;

    @Test
    void delegates_to_schedule_use_case() {
        KafkaMatchScheduledConsumer consumer = new KafkaMatchScheduledConsumer(scheduleMatchNotifications);
        Instant kickoff = Instant.parse("2026-05-01T20:00:00Z");
        MatchScheduled event = MatchScheduled.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setMatchId("match-42")
                .setHomeTeamId("team-a")
                .setAwayTeamId("team-b")
                .setKickoffAt(kickoff)
                .setOccurredAt(Instant.now())
                .build();

        consumer.consume(event);

        verify(scheduleMatchNotifications).schedule("match-42", "team-a", "team-b", kickoff);
    }

    @Test
    void propagates_exception_to_caller() {
        KafkaMatchScheduledConsumer consumer = new KafkaMatchScheduledConsumer(scheduleMatchNotifications);
        MatchScheduled event = MatchScheduled.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setMatchId("match-err")
                .setHomeTeamId("team-x")
                .setAwayTeamId("team-y")
                .setKickoffAt(Instant.now())
                .setOccurredAt(Instant.now())
                .build();
        doThrow(new RuntimeException("DB down")).when(scheduleMatchNotifications).schedule(any(), any(), any(), any());

        assertThrows(RuntimeException.class, () -> consumer.consume(event));
    }
}
