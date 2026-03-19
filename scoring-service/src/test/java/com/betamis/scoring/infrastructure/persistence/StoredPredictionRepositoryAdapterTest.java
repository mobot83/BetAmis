package com.betamis.scoring.infrastructure.persistence;

import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoredPredictionRepositoryAdapterTest {

    @Inject
    StoredPredictionRepository repository;

    @Test
    @Transactional
    @DisplayName("Should persist and find a prediction by matchId")
    void shouldSaveAndFindByMatchId() {
        StoredPrediction prediction = new StoredPrediction("pred-100", "match-100", "user-1", new FinalScore(2, 1));

        repository.save(prediction);

        List<StoredPrediction> found = repository.findByMatchId("match-100");
        assertEquals(1, found.size());
        assertEquals("pred-100", found.get(0).predictionId());
        assertEquals("user-1", found.get(0).userId());
        assertEquals(2, found.get(0).predictedScore().homeTeamScore());
        assertEquals(1, found.get(0).predictedScore().awayTeamScore());
    }

    @Test
    @Transactional
    @DisplayName("Should return multiple predictions for same match")
    void shouldReturnMultiplePredictionsForSameMatch() {
        repository.save(new StoredPrediction("pred-200", "match-200", "user-1", new FinalScore(1, 0)));
        repository.save(new StoredPrediction("pred-201", "match-200", "user-2", new FinalScore(0, 0)));

        List<StoredPrediction> found = repository.findByMatchId("match-200");
        assertEquals(2, found.size());
    }

    @Test
    @Transactional
    @DisplayName("Should return empty list when match has no predictions")
    void shouldReturnEmptyForUnknownMatch() {
        List<StoredPrediction> found = repository.findByMatchId("unknown-match");
        assertTrue(found.isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should be idempotent — saving the same predictionId twice must not throw or duplicate")
    void shouldIgnoreDuplicatePredictionId() {
        StoredPrediction prediction = new StoredPrediction("pred-300", "match-300", "user-1", new FinalScore(1, 0));

        repository.save(prediction);
        repository.save(prediction); // second call must be a no-op

        List<StoredPrediction> found = repository.findByMatchId("match-300");
        assertEquals(1, found.size());
    }
}
