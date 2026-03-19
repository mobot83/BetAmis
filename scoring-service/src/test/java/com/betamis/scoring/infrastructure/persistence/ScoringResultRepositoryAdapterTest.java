package com.betamis.scoring.infrastructure.persistence;

import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.port.out.ScoringResultRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ScoringResultRepositoryAdapterTest {

    @Inject
    ScoringResultRepository repository;

    @Test
    @Transactional
    @DisplayName("Should persist a scoring result")
    void shouldSaveResult() {
        ScoringResult result = new ScoringResult("pred-300", "match-300", "user-1", 3);

        repository.save(result);

        assertTrue(repository.existsByPredictionId("pred-300"));
    }

    @Test
    @DisplayName("Should return false when prediction not yet scored")
    void shouldReturnFalseForUnknownPrediction() {
        assertFalse(repository.existsByPredictionId("unknown-pred"));
    }
}
