package com.betamis.scoring.application.usecase;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreMatchUseCaseMixedTest {

    @Mock StoredPredictionRepository predictionRepository;
    @Mock ScoringResultRepository scoringResultRepository;
    @Mock RankingRepository rankingRepository;
    @Mock ScoringEventPublisher eventPublisher;

    ScoreMatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ScoreMatchUseCase(predictionRepository, scoringResultRepository, rankingRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should score only unscored predictions when list contains both scored and unscored")
    void shouldScoreOnlyUnscoredInMixedList() {
        List<StoredPrediction> predictions = List.of(
                new StoredPrediction("pred-already-scored", "match-1", "user-A", new FinalScore(1, 0)),
                new StoredPrediction("pred-new",            "match-1", "user-B", new FinalScore(2, 1))
        );

        when(predictionRepository.findByMatchId("match-1")).thenReturn(predictions);
        when(scoringResultRepository.existsByPredictionId("pred-already-scored")).thenReturn(true);
        when(scoringResultRepository.existsByPredictionId("pred-new")).thenReturn(false);
        when(rankingRepository.addPoints("user-B", "global", 3))
                .thenReturn(new UserRanking("user-B", "global", 3, 1));

        useCase.score("match-1", new FinalScore(2, 1));

        var captor = ArgumentCaptor.forClass(ScoringResult.class);
        verify(scoringResultRepository, times(1)).save(captor.capture());
        assertEquals("pred-new", captor.getValue().predictionId());
        assertEquals(3, captor.getValue().points());

        verify(rankingRepository, times(1)).addPoints("user-B", "global", 3);
        verify(eventPublisher, times(1)).publishPointsCalculated(any());
        verify(eventPublisher, times(1)).publishRankingUpdated(any());
    }

    @Test
    @DisplayName("Points are 0 when prediction is completely wrong — ranking still updated")
    void shouldStillUpdateRankingWithZeroPoints() {
        StoredPrediction prediction = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 0));

        when(predictionRepository.findByMatchId("match-1")).thenReturn(List.of(prediction));
        when(scoringResultRepository.existsByPredictionId("pred-1")).thenReturn(false);
        when(rankingRepository.addPoints("user-1", "global", 0))
                .thenReturn(new UserRanking("user-1", "global", 0, 1));

        useCase.score("match-1", new FinalScore(0, 3));

        var captor = ArgumentCaptor.forClass(ScoringResult.class);
        verify(scoringResultRepository).save(captor.capture());
        assertEquals(0, captor.getValue().points());
        verify(rankingRepository).addPoints("user-1", "global", 0);
    }
}
