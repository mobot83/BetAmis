package com.betamis.scoring.application.usecase;

import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorePredictionUseCaseTest {

    @Mock
    StoredPredictionRepository predictionRepository;

    StorePredictionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new StorePredictionUseCase(predictionRepository);
    }

    @Test
    @DisplayName("Should delegate to repository")
    void shouldSavePrediction() {
        StoredPrediction prediction = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1));

        useCase.store(prediction);

        verify(predictionRepository).save(prediction);
    }
}
