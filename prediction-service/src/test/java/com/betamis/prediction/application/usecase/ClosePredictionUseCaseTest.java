package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.event.PredictionClosed;
import com.betamis.prediction.domain.exception.PredictionAlreadyClosedException;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.prediction.PredictionStatus;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.out.EventPublisher;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClosePredictionUseCaseTest {

    @Mock
    PredictionRepository repository;

    @Mock
    EventPublisher eventPublisher;

    SimpleMeterRegistry registry;
    ClosePredictionUseCase useCase;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        useCase = new ClosePredictionUseCase(repository, eventPublisher, registry);
    }

    @Test
    void closes_all_submitted_predictions_for_a_match() {
        Prediction p1 = submittedPrediction("pred-1", "match-1", "user-1");
        Prediction p2 = submittedPrediction("pred-2", "match-1", "user-2");
        when(repository.findByMatchId("match-1")).thenReturn(List.of(p1, p2));

        useCase.close("match-1");

        assertEquals(PredictionStatus.CLOSED, p1.getStatus());
        assertEquals(PredictionStatus.CLOSED, p2.getStatus());
        verify(repository).update(p1);
        verify(repository).update(p2);
        verify(eventPublisher).publish(argThat((PredictionClosed e) -> "match-1".equals(e.matchId())));
    }

    @Test
    void skips_already_closed_predictions() {
        Prediction open = submittedPrediction("pred-1", "match-1", "user-1");
        Prediction closed = closedPrediction("pred-2", "match-1", "user-2");
        when(repository.findByMatchId("match-1")).thenReturn(List.of(open, closed));

        useCase.close("match-1");

        verify(repository).update(open);
        verify(repository, never()).update(closed);
        verify(eventPublisher).publish(any(PredictionClosed.class));
    }

    @Test
    void publishes_event_even_when_no_predictions_exist() {
        when(repository.findByMatchId("match-empty")).thenReturn(List.of());

        useCase.close("match-empty");

        verify(repository, never()).update(any());
        verify(eventPublisher).publish(argThat((PredictionClosed e) -> "match-empty".equals(e.matchId())));
    }

    @Test
    void counter_increments_once_per_close_call_regardless_of_prediction_state() {
        assertEquals(0.0, registry.counter("betamis_predictions_closed_total").count());

        when(repository.findByMatchId("match-1")).thenReturn(List.of());
        useCase.close("match-1");
        assertEquals(1.0, registry.counter("betamis_predictions_closed_total").count());

        when(repository.findByMatchId("match-2")).thenReturn(
                List.of(submittedPrediction("pred-1", "match-2", "user-1")));
        useCase.close("match-2");
        assertEquals(2.0, registry.counter("betamis_predictions_closed_total").count());
    }

    private Prediction submittedPrediction(String id, String matchId, String userId) {
        return new Prediction(id, matchId, userId, new Score(1, 0), PredictionStatus.SUBMITTED, Instant.now());
    }

    private Prediction closedPrediction(String id, String matchId, String userId) {
        Prediction p = new Prediction(id, matchId, userId, new Score(0, 0), PredictionStatus.CLOSED, Instant.now());
        return p;
    }
}
