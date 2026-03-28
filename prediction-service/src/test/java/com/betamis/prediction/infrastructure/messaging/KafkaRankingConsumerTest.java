package com.betamis.prediction.infrastructure.messaging;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import com.betamis.prediction.infrastructure.sse.RankingBroadcaster;
import com.betamis.ranking.event.RankingUpdated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaRankingConsumerTest {

    @Mock
    RankingBroadcaster broadcaster;

    @Test
    void delegates_ranking_updated_to_broadcaster() {
        KafkaRankingConsumer consumer = new KafkaRankingConsumer(broadcaster);
        RankingUpdated event = RankingUpdated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setLeagueId("league-42")
                .setUserId("user-a")
                .setRank(1)
                .setTotalPoints(100L)
                .setOccurredAt(Instant.now())
                .build();

        consumer.consume(event);

        ArgumentCaptor<RankingEntry> captor = ArgumentCaptor.forClass(RankingEntry.class);
        verify(broadcaster).publish(eq("league-42"), captor.capture());
        assertEquals("user-a", captor.getValue().userId());
        assertEquals(1, captor.getValue().rank());
        assertEquals(100L, captor.getValue().totalPoints());
    }

    @Test
    void swallows_exception_without_propagating() {
        KafkaRankingConsumer consumer = new KafkaRankingConsumer(broadcaster);
        RankingUpdated event = RankingUpdated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setLeagueId("league-err")
                .setUserId("user-x")
                .setRank(1)
                .setTotalPoints(5L)
                .setOccurredAt(Instant.now())
                .build();
        doThrow(new RuntimeException("broadcaster down")).when(broadcaster).publish(any(), any());

        try {
            consumer.consume(event);
        } catch (Exception e) {
            throw new AssertionError("Expected no exception but got: " + e, e);
        }
    }
}
