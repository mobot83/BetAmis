package com.betamis.scoring.infrastructure.messaging;

import com.betamis.match.event.MatchFinished;
import com.betamis.match.event.Score;
import com.betamis.prediction.event.PredictionSubmitted;
import com.betamis.scoring.application.usecase.ScoreMatchUseCase;
import com.betamis.scoring.application.usecase.StorePredictionUseCase;
import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.out.RankingRepository;
import com.betamis.scoring.domain.port.out.ScoringEventPublisher;
import com.betamis.scoring.domain.port.out.ScoringResultRepository;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * End-to-end flow tests using pure Mockito — no Quarkus container, no database, no Redis.
 */
@ExtendWith(MockitoExtension.class)
class ScoringIntegrationTest {

    @Mock StoredPredictionRepository predictionRepository;
    @Mock ScoringResultRepository scoringResultRepository;
    @Mock RankingRepository rankingRepository;
    @Mock ScoringEventPublisher eventPublisher;

    KafkaPredictionConsumer predictionConsumer;
    KafkaMatchFinishedConsumer matchConsumer;

    @BeforeEach
    void setUp() {
        StorePredictionUseCase storePrediction = new StorePredictionUseCase(predictionRepository);
        ScoreMatchUseCase scoreMatch = new ScoreMatchUseCase(predictionRepository, scoringResultRepository, rankingRepository, eventPublisher);
        predictionConsumer = new KafkaPredictionConsumer(storePrediction);
        matchConsumer = new KafkaMatchFinishedConsumer(scoreMatch);
    }

    @Test
    @DisplayName("Exact score prediction should award 3 points")
    void exactScoreYieldsThreePoints() {
        StoredPrediction stored = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1));
        when(predictionRepository.findByMatchId("match-1")).thenReturn(List.of(stored));
        when(scoringResultRepository.existsByPredictionId("pred-1")).thenReturn(false);
        when(rankingRepository.addPoints("user-1", "global", 3))
                .thenReturn(new UserRanking("user-1", "global", 3, 1));

        predictionConsumer.consume(predictionEvent("pred-1", "match-1", "user-1", 2, 1));
        verify(predictionRepository).save(stored);

        matchConsumer.consume(matchEvent("match-1", 2, 1));

        var captor = ArgumentCaptor.forClass(ScoringResult.class);
        verify(scoringResultRepository).save(captor.capture());
        assertEquals(3, captor.getValue().points());
        verify(eventPublisher).publishPointsCalculated(any());
        verify(eventPublisher).publishRankingUpdated(any());
    }

    @Test
    @DisplayName("Wrong prediction should award 0 points but still update ranking")
    void wrongPredictionYieldsZeroPoints() {
        StoredPrediction stored = new StoredPrediction("pred-2", "match-2", "user-1", new FinalScore(2, 0));
        when(predictionRepository.findByMatchId("match-2")).thenReturn(List.of(stored));
        when(scoringResultRepository.existsByPredictionId("pred-2")).thenReturn(false);
        when(rankingRepository.addPoints("user-1", "global", 0))
                .thenReturn(new UserRanking("user-1", "global", 0, 1));

        matchConsumer.consume(matchEvent("match-2", 0, 3));

        var captor = ArgumentCaptor.forClass(ScoringResult.class);
        verify(scoringResultRepository).save(captor.capture());
        assertEquals(0, captor.getValue().points());
        verify(rankingRepository).addPoints("user-1", "global", 0);
    }

    @Test
    @DisplayName("MatchFinished consumed twice should only score each prediction once (idempotency)")
    void scoringSameMatchTwiceIsIdempotent() {
        StoredPrediction stored = new StoredPrediction("pred-3", "match-3", "user-1", new FinalScore(1, 1));
        when(predictionRepository.findByMatchId("match-3")).thenReturn(List.of(stored));
        // First call: not yet scored; second call: already scored
        when(scoringResultRepository.existsByPredictionId("pred-3"))
                .thenReturn(false)
                .thenReturn(true);
        when(rankingRepository.addPoints(anyString(), anyString(), anyInt()))
                .thenReturn(new UserRanking("user-1", "global", 3, 1));

        matchConsumer.consume(matchEvent("match-3", 1, 1));
        matchConsumer.consume(matchEvent("match-3", 1, 1));

        verify(scoringResultRepository, times(1)).save(any());
        verify(rankingRepository, times(1)).addPoints(anyString(), anyString(), anyInt());
    }

    // --- helpers ---

    private static PredictionSubmitted predictionEvent(
            String predictionId, String matchId, String userId, int home, int away) {
        return PredictionSubmitted.newBuilder()
                .setId("evt-" + predictionId)
                .setPredictionId(predictionId)
                .setMatchId(matchId)
                .setUserId(userId)
                .setScore(com.betamis.prediction.event.Score.newBuilder()
                        .setHomeTeamScore(home)
                        .setAwayTeamScore(away)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();
    }

    private static MatchFinished matchEvent(String matchId, int home, int away) {
        return MatchFinished.newBuilder()
                .setId("evt-" + matchId)
                .setMatchId(matchId)
                .setHomeTeamId("home")
                .setAwayTeamId("away")
                .setFinalScore(Score.newBuilder()
                        .setHomeTeamScore(home)
                        .setAwayTeamScore(away)
                        .build())
                .setOccurredAt(java.time.Instant.now())
                .build();
    }
}
