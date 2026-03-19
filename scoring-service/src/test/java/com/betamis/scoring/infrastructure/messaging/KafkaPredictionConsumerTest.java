package com.betamis.scoring.infrastructure.messaging;

import com.betamis.prediction.event.PredictionSubmitted;
import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.port.in.StorePrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaPredictionConsumerTest {

    @Mock
    StorePrediction storePrediction;

    KafkaPredictionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaPredictionConsumer(storePrediction);
    }

    @Test
    @DisplayName("Should map PredictionSubmitted event to StoredPrediction and delegate to use case")
    void shouldMapEventAndDelegate() {
        PredictionSubmitted event = PredictionSubmitted.newBuilder()
                .setId("evt-1")
                .setPredictionId("pred-1")
                .setMatchId("match-1")
                .setUserId("user-1")
                .setScore(com.betamis.prediction.event.Score.newBuilder()
                        .setHomeTeamScore(2)
                        .setAwayTeamScore(1)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();

        consumer.consume(event);

        var captor = ArgumentCaptor.forClass(StoredPrediction.class);
        verify(storePrediction).store(captor.capture());
        StoredPrediction stored = captor.getValue();
        assertEquals("pred-1", stored.predictionId());
        assertEquals("match-1", stored.matchId());
        assertEquals("user-1", stored.userId());
        assertEquals(new FinalScore(2, 1), stored.predictedScore());
    }

    @Test
    @DisplayName("Should correctly map a 0-0 draw prediction")
    void shouldMapZeroZeroDraw() {
        PredictionSubmitted event = PredictionSubmitted.newBuilder()
                .setId("evt-2")
                .setPredictionId("pred-2")
                .setMatchId("match-2")
                .setUserId("user-2")
                .setScore(com.betamis.prediction.event.Score.newBuilder()
                        .setHomeTeamScore(0)
                        .setAwayTeamScore(0)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();

        consumer.consume(event);

        var captor = ArgumentCaptor.forClass(StoredPrediction.class);
        verify(storePrediction).store(captor.capture());
        assertEquals(new FinalScore(0, 0), captor.getValue().predictedScore());
    }

    @Test
    @DisplayName("Should swallow exception and not propagate to Kafka consumer when use case throws")
    void shouldNotPropagateExceptionOnFailure() {
        doThrow(new RuntimeException("DB unavailable")).when(storePrediction).store(any());
        PredictionSubmitted event = PredictionSubmitted.newBuilder()
                .setId("evt-3")
                .setPredictionId("pred-3")
                .setMatchId("match-3")
                .setUserId("user-3")
                .setScore(com.betamis.prediction.event.Score.newBuilder()
                        .setHomeTeamScore(1)
                        .setAwayTeamScore(0)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();

        assertDoesNotThrow(() -> consumer.consume(event));
    }
}
