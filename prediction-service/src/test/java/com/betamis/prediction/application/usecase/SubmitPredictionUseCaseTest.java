package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.event.PredictionSubmitted;
import com.betamis.prediction.domain.exception.PredictionAlreadySubmittedException;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.prediction.PredictionStatus;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.out.EventPublisher;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitPredictionUseCaseTest {

    @Mock
    PredictionRepository repository;

    @Mock
    EventPublisher eventPublisher;

    SimpleMeterRegistry registry;
    SubmitPredictionUseCase useCase;

    static final Instant FUTURE_KICK_OFF = Instant.now().plus(1, ChronoUnit.HOURS);

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        useCase = new SubmitPredictionUseCase(repository, eventPublisher, registry);
    }

    @Test
    @DisplayName("Should save a SUBMITTED prediction to the repository")
    void shouldSaveSubmittedPrediction() {
        when(repository.existsByUserIdAndMatchId("user1", "match1")).thenReturn(false);
        var captor = ArgumentCaptor.forClass(Prediction.class);

        String predictionId = useCase.execute("match1", "user1", new Score(2, 1), FUTURE_KICK_OFF);

        assertNotNull(predictionId);
        assertFalse(predictionId.isBlank());
        verify(eventPublisher).publish(any(PredictionSubmitted.class));
        verify(repository).save(captor.capture());
        var prediction = captor.getValue();
        assertEquals(PredictionStatus.SUBMITTED, prediction.getStatus());
        assertEquals("match1", prediction.getMatchId());
        assertEquals("user1", prediction.getUserId());
        assertEquals(new Score(2, 1), prediction.getScore());
        assertNotNull(prediction.getId());
        assertFalse(prediction.getId().isBlank());
    }

    @Test
    @DisplayName("Should throw when user already submitted a prediction for the same match")
    void shouldThrowWhenPredictionAlreadyExists() {
        when(repository.existsByUserIdAndMatchId("user1", "match1")).thenReturn(true);

        assertThrows(PredictionAlreadySubmittedException.class,
            () -> useCase.execute("match1", "user1", new Score(2, 1), FUTURE_KICK_OFF));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("betamis_predictions_submitted_total increments on success, not on duplicate rejection")
    void shouldIncrementCounterOnlyOnSuccessfulSubmit() {
        assertEquals(0.0, registry.counter("betamis_predictions_submitted_total").count());

        when(repository.existsByUserIdAndMatchId("user1", "match1")).thenReturn(true);
        assertThrows(PredictionAlreadySubmittedException.class,
            () -> useCase.execute("match1", "user1", new Score(1, 0), FUTURE_KICK_OFF));
        assertEquals(0.0, registry.counter("betamis_predictions_submitted_total").count());

        when(repository.existsByUserIdAndMatchId("user2", "match1")).thenReturn(false);
        useCase.execute("match1", "user2", new Score(1, 0), FUTURE_KICK_OFF);
        assertEquals(1.0, registry.counter("betamis_predictions_submitted_total").count());
    }
}
