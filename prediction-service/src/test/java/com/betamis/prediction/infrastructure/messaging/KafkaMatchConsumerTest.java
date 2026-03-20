package com.betamis.prediction.infrastructure.messaging;

import com.betamis.match.event.MatchStarted;
import com.betamis.prediction.domain.port.in.ClosePrediction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaMatchConsumerTest {

    @Mock
    ClosePrediction closePrediction;

    @Test
    void delegates_to_close_prediction_use_case() {
        KafkaMatchConsumer consumer = new KafkaMatchConsumer(closePrediction);
        MatchStarted event = MatchStarted.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setMatchId("match-42")
                .setHomeTeamId("team-a")
                .setAwayTeamId("team-b")
                .setOccurredAt(Instant.now())
                .build();

        consumer.consume(event);

        verify(closePrediction).close("match-42");
    }

    @Test
    void swallows_exception_without_propagating() {
        KafkaMatchConsumer consumer = new KafkaMatchConsumer(closePrediction);
        MatchStarted event = MatchStarted.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setMatchId("match-err")
                .setHomeTeamId("team-a")
                .setAwayTeamId("team-b")
                .setOccurredAt(Instant.now())
                .build();
        doThrow(new RuntimeException("DB down")).when(closePrediction).close("match-err");

        assertDoesNotThrow(() -> consumer.consume(event));
    }

    private void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new AssertionError("Expected no exception but got: " + e, e);
        }
    }
}
