package com.betamis.scoring.application.usecase;

import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.out.LeagueMembershipRepository;
import com.betamis.scoring.domain.port.out.RankingRepository;
import com.betamis.scoring.domain.port.out.ScoringEventPublisher;
import com.betamis.scoring.domain.port.out.ScoringResultRepository;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreMatchUseCaseTest {

    @Mock StoredPredictionRepository predictionRepository;
    @Mock ScoringResultRepository scoringResultRepository;
    @Mock RankingRepository rankingRepository;
    @Mock ScoringEventPublisher eventPublisher;
    @Mock LeagueMembershipRepository leagueMembershipRepository;

    SimpleMeterRegistry registry;
    ScoreMatchUseCase useCase;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        useCase = new ScoreMatchUseCase(predictionRepository, scoringResultRepository, rankingRepository, eventPublisher, leagueMembershipRepository, registry);
    }

    @Test
    @DisplayName("Should score predictions and publish events")
    void shouldScorePredictionsAndPublishEvents() {
        StoredPrediction prediction = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1));
        FinalScore finalScore = new FinalScore(2, 1);
        UserRanking ranking = new UserRanking("user-1", "global", 3, 1);

        when(predictionRepository.findByMatchId("match-1")).thenReturn(List.of(prediction));
        when(scoringResultRepository.existsByPredictionId("pred-1")).thenReturn(false);
        when(rankingRepository.addPoints("user-1", "global", 3)).thenReturn(ranking);

        useCase.score("match-1", finalScore);

        var resultCaptor = ArgumentCaptor.forClass(ScoringResult.class);
        verify(scoringResultRepository).save(resultCaptor.capture());
        assertEquals(3, resultCaptor.getValue().points());
        assertEquals("pred-1", resultCaptor.getValue().predictionId());

        verify(rankingRepository).addPoints("user-1", "global", 3);
        verify(eventPublisher).publishPointsCalculated(any(ScoringResult.class));
        verify(eventPublisher).publishRankingUpdated(ranking);
    }

    @Test
    @DisplayName("Should skip already-scored predictions (idempotency)")
    void shouldSkipAlreadyScoredPredictions() {
        StoredPrediction prediction = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(1, 0));

        when(predictionRepository.findByMatchId("match-1")).thenReturn(List.of(prediction));
        when(scoringResultRepository.existsByPredictionId("pred-1")).thenReturn(true);

        useCase.score("match-1", new FinalScore(1, 0));

        verify(scoringResultRepository, never()).save(any());
        verifyNoInteractions(rankingRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should handle no predictions for a match")
    void shouldHandleNoPredictions() {
        when(predictionRepository.findByMatchId("match-1")).thenReturn(List.of());

        useCase.score("match-1", new FinalScore(1, 0));

        verify(scoringResultRepository, never()).save(any());
        verifyNoInteractions(rankingRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should score multiple predictions for the same match")
    void shouldScoreMultiplePredictions() {
        List<StoredPrediction> predictions = List.of(
                new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1)),
                new StoredPrediction("pred-2", "match-1", "user-2", new FinalScore(0, 0))
        );
        FinalScore finalScore = new FinalScore(2, 1);

        when(predictionRepository.findByMatchId("match-1")).thenReturn(predictions);
        when(scoringResultRepository.existsByPredictionId(anyString())).thenReturn(false);
        when(rankingRepository.addPoints(anyString(), anyString(), anyInt()))
                .thenReturn(new UserRanking("user-1", "global", 3, 1))
                .thenReturn(new UserRanking("user-2", "global", 0, 2));

        useCase.score("match-1", finalScore);

        verify(scoringResultRepository, times(2)).save(any());
        verify(rankingRepository, times(2)).addPoints(anyString(), anyString(), anyInt());
        verify(eventPublisher, times(2)).publishPointsCalculated(any());
        verify(eventPublisher, times(2)).publishRankingUpdated(any());
    }

    @Test
    @DisplayName("betamis_scores_computed_total counts only new scores; betamis_scoring_duration_seconds records every call")
    void shouldRecordMetricsCorrectly() {
        StoredPrediction fresh = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1));
        StoredPrediction alreadyScored = new StoredPrediction("pred-2", "match-1", "user-2", new FinalScore(0, 0));
        when(predictionRepository.findByMatchId("match-1")).thenReturn(List.of(fresh, alreadyScored));
        when(scoringResultRepository.existsByPredictionId("pred-1")).thenReturn(false);
        when(scoringResultRepository.existsByPredictionId("pred-2")).thenReturn(true);
        when(rankingRepository.addPoints(anyString(), anyString(), anyInt()))
                .thenReturn(new UserRanking("user-1", "global", 3, 1));

        assertEquals(0.0, registry.counter("betamis_scores_computed_total").count());
        assertEquals(0, registry.timer("betamis_scoring_duration_seconds").count());

        useCase.score("match-1", new FinalScore(2, 1));

        assertEquals(1.0, registry.counter("betamis_scores_computed_total").count(),
                "only the non-skipped prediction should be counted");
        assertEquals(1, registry.timer("betamis_scoring_duration_seconds").count(),
                "timer records once per score() call");
        assertTrue(registry.timer("betamis_scoring_duration_seconds").totalTime(TimeUnit.NANOSECONDS) > 0,
                "recorded duration should be positive");
    }
}
