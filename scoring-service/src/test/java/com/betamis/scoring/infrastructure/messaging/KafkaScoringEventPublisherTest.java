package com.betamis.scoring.infrastructure.messaging;

import com.betamis.ranking.event.RankingUpdated;
import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.event.PointsCalculated;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaScoringEventPublisherTest {

    @Mock Emitter<PointsCalculated> pointsCalculatedEmitter;
    @Mock Emitter<RankingUpdated> rankingUpdatedEmitter;

    KafkaScoringEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaScoringEventPublisher(pointsCalculatedEmitter, rankingUpdatedEmitter);
    }

    @Test
    @DisplayName("publishPointsCalculated should build and send the correct Avro event")
    void shouldPublishPointsCalculatedEvent() {
        when(pointsCalculatedEmitter.send(any(PointsCalculated.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        ScoringResult result = new ScoringResult("pred-1", "match-1", "user-1", 3);

        publisher.publishPointsCalculated(result);

        var captor = ArgumentCaptor.forClass(PointsCalculated.class);
        verify(pointsCalculatedEmitter).send(captor.capture());
        PointsCalculated sent = captor.getValue();
        assertEquals("pred-1", sent.getPredictionId());
        assertEquals("match-1", sent.getMatchId());
        assertEquals("user-1", sent.getUserId());
        assertEquals(3, sent.getPoints());
        assertNotNull(sent.getId());
        assertNotNull(sent.getOccurredAt());
    }

    @Test
    @DisplayName("publishPointsCalculated should not throw and the error callback is reached on emitter failure")
    void shouldHandlePointsCalculatedEmitterFailure() {
        AtomicReference<Throwable> capturedError = new AtomicReference<>();
        CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.whenComplete((v, t) -> capturedError.set(t));
        failed.completeExceptionally(new RuntimeException("Kafka down"));

        when(pointsCalculatedEmitter.send(any(PointsCalculated.class))).thenReturn(failed);

        assertDoesNotThrow(() -> publisher.publishPointsCalculated(
                new ScoringResult("pred-1", "match-1", "user-1", 3)));

        assertNotNull(capturedError.get(), "whenComplete callback should have been invoked");
    }

    @Test
    @DisplayName("publishRankingUpdated should build and send the correct Avro event")
    void shouldPublishRankingUpdatedEvent() {
        when(rankingUpdatedEmitter.send(any(RankingUpdated.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        UserRanking ranking = new UserRanking("user-1", "global", 10L, 2);

        publisher.publishRankingUpdated(ranking);

        var captor = ArgumentCaptor.forClass(RankingUpdated.class);
        verify(rankingUpdatedEmitter).send(captor.capture());
        RankingUpdated sent = captor.getValue();
        assertEquals("user-1", sent.getUserId());
        assertEquals("global", sent.getLeagueId());
        assertEquals(10L, sent.getTotalPoints());
        assertEquals(2, sent.getRank());
        assertNotNull(sent.getId());
        assertNotNull(sent.getOccurredAt());
    }

    @Test
    @DisplayName("publishRankingUpdated should not throw and the error callback is reached on emitter failure")
    void shouldHandleRankingUpdatedEmitterFailure() {
        AtomicReference<Throwable> capturedError = new AtomicReference<>();
        CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.whenComplete((v, t) -> capturedError.set(t));
        failed.completeExceptionally(new RuntimeException("Kafka down"));

        when(rankingUpdatedEmitter.send(any(RankingUpdated.class))).thenReturn(failed);

        assertDoesNotThrow(() -> publisher.publishRankingUpdated(
                new UserRanking("user-1", "global", 10L, 1)));

        assertNotNull(capturedError.get(), "whenComplete callback should have been invoked");
    }
}
