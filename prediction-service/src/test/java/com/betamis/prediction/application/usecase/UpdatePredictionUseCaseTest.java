package com.betamis.prediction.application.usecase;

import com.betamis.prediction.domain.exception.MatchAlreadyStartedException;
import com.betamis.prediction.domain.exception.PredictionNotFoundException;
import com.betamis.prediction.domain.exception.PredictionNotOwnedException;
import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.prediction.PredictionStatus;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.out.PredictionRepository;
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
class UpdatePredictionUseCaseTest {

    @Mock
    PredictionRepository repository;

    UpdatePredictionUseCase useCase;

    static final Instant FUTURE_KICK_OFF = Instant.now().plus(1, ChronoUnit.HOURS);
    static final Instant PAST_KICK_OFF = Instant.now().minus(1, ChronoUnit.HOURS);

    @BeforeEach
    void setUp() {
        useCase = new UpdatePredictionUseCase(repository);
    }

    private Prediction submittedPrediction(String id, String userId) {
        return new Prediction(id, "match-1", userId, new Score(1, 0),
                PredictionStatus.SUBMITTED, Instant.now().minus(1, ChronoUnit.DAYS));
    }

    private Prediction closedPrediction(String id, String userId) {
        return new Prediction(id, "match-1", userId, new Score(1, 0),
                PredictionStatus.CLOSED, Instant.now().minus(2, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("Should update score when prediction is owned and kick-off is in the future")
    void shouldUpdateScore() {
        Prediction prediction = submittedPrediction("pred-1", "user-1");
        when(repository.findById("pred-1")).thenReturn(prediction);

        Prediction result = useCase.execute("pred-1", "user-1", new Score(3, 1), FUTURE_KICK_OFF);

        assertEquals(3, result.getScore().homeTeamScore());
        assertEquals(1, result.getScore().awayTeamScore());
        ArgumentCaptor<Prediction> captor = ArgumentCaptor.forClass(Prediction.class);
        verify(repository).update(captor.capture());
        assertEquals(new Score(3, 1), captor.getValue().getScore());
    }

    @Test
    @DisplayName("Should throw PredictionNotFoundException when prediction does not exist")
    void shouldThrowWhenNotFound() {
        when(repository.findById("missing")).thenReturn(null);

        assertThrows(PredictionNotFoundException.class,
                () -> useCase.execute("missing", "user-1", new Score(1, 0), FUTURE_KICK_OFF));
        verify(repository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw PredictionNotOwnedException when user is not the owner")
    void shouldThrowWhenNotOwner() {
        when(repository.findById("pred-2")).thenReturn(submittedPrediction("pred-2", "user-A"));

        assertThrows(PredictionNotOwnedException.class,
                () -> useCase.execute("pred-2", "user-B", new Score(1, 0), FUTURE_KICK_OFF));
        verify(repository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw MatchAlreadyStartedException when prediction is CLOSED")
    void shouldThrowWhenClosed() {
        when(repository.findById("pred-3")).thenReturn(closedPrediction("pred-3", "user-1"));

        assertThrows(MatchAlreadyStartedException.class,
                () -> useCase.execute("pred-3", "user-1", new Score(2, 0), FUTURE_KICK_OFF));
        verify(repository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw MatchAlreadyStartedException when kick-off time has passed")
    void shouldThrowWhenKickoffPassed() {
        when(repository.findById("pred-4")).thenReturn(submittedPrediction("pred-4", "user-1"));

        assertThrows(MatchAlreadyStartedException.class,
                () -> useCase.execute("pred-4", "user-1", new Score(2, 0), PAST_KICK_OFF));
        verify(repository, never()).update(any());
    }
}
