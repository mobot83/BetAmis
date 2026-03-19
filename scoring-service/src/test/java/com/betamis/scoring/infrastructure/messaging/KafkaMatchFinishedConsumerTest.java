package com.betamis.scoring.infrastructure.messaging;

import com.betamis.match.event.MatchFinished;
import com.betamis.match.event.Score;
import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.port.in.ScoreMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaMatchFinishedConsumerTest {

    @Mock
    ScoreMatch scoreMatch;

    KafkaMatchFinishedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaMatchFinishedConsumer(scoreMatch);
    }

    @Test
    @DisplayName("Should map MatchFinished event and delegate to ScoreMatch use case")
    void shouldMapEventAndDelegate() {
        MatchFinished event = MatchFinished.newBuilder()
                .setId("evt-1")
                .setMatchId("match-1")
                .setHomeTeamId("home-team")
                .setAwayTeamId("away-team")
                .setFinalScore(Score.newBuilder()
                        .setHomeTeamScore(3)
                        .setAwayTeamScore(2)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();

        consumer.consume(event);

        var scoreCaptor = ArgumentCaptor.forClass(FinalScore.class);
        verify(scoreMatch).score(eq("match-1"), scoreCaptor.capture());
        assertEquals(new FinalScore(3, 2), scoreCaptor.getValue());
    }

    @Test
    @DisplayName("Should correctly map a 0-0 draw result")
    void shouldMapZeroZeroResult() {
        MatchFinished event = MatchFinished.newBuilder()
                .setId("evt-2")
                .setMatchId("match-2")
                .setHomeTeamId("home")
                .setAwayTeamId("away")
                .setFinalScore(Score.newBuilder()
                        .setHomeTeamScore(0)
                        .setAwayTeamScore(0)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();

        consumer.consume(event);

        var scoreCaptor = ArgumentCaptor.forClass(FinalScore.class);
        verify(scoreMatch).score(eq("match-2"), scoreCaptor.capture());
        assertEquals(new FinalScore(0, 0), scoreCaptor.getValue());
    }

    @Test
    @DisplayName("Should swallow exception and not propagate to Kafka consumer when use case throws")
    void shouldNotPropagateExceptionOnFailure() {
        doThrow(new RuntimeException("Scoring failed")).when(scoreMatch).score(any(), any());
        MatchFinished event = MatchFinished.newBuilder()
                .setId("evt-3")
                .setMatchId("match-3")
                .setHomeTeamId("home")
                .setAwayTeamId("away")
                .setFinalScore(Score.newBuilder()
                        .setHomeTeamScore(1)
                        .setAwayTeamScore(0)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();

        assertDoesNotThrow(() -> consumer.consume(event));
    }
}
