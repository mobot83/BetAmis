package com.betamis.prediction.domain.model.prediction;

import com.betamis.prediction.domain.event.PredictionSubmitted;
import com.betamis.prediction.domain.exception.KickOffAlreadyPassedException;
import com.betamis.prediction.domain.exception.PredictionAlreadyClosedException;
import com.betamis.prediction.domain.model.score.Score;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class PredictionTest {

    @Test
    @DisplayName("Should create prediction successfully when all required fields are provided")
    void shouldCreatePredictionSuccessfully() {
        // Given
        String id = "prediction1";
        String matchId = "match1";
        String userId = "user1";
        Score score = new Score(2, 1);
        PredictionStatus status = PredictionStatus.SUBMITTED;
        Instant submittedAt = Instant.now();

        // When
        Prediction prediction = new Prediction(id, matchId, userId, score, status, submittedAt);

        // Then
        assertEquals(id, prediction.getId());
        assertEquals(matchId, prediction.getMatchId());
        assertEquals(userId, prediction.getUserId());
        assertEquals(score, prediction.getScore());
        assertEquals(status, prediction.getStatus());
        assertEquals(submittedAt, prediction.getSubmittedAt());
    }

    @Test
    @DisplayName("Should throw error when no id is provided")
    void shouldThrowErrorWhenNoIdIsProvided() {
        assertThrows(IllegalArgumentException.class, () -> new Prediction(null, null, null, null, null, null));
    }

    @Test
    @DisplayName("Should throw error when no match id is provided")
    void shouldThrowErrorWhenNoMatchIdIsProvided() {
        assertThrows(IllegalArgumentException.class, () -> new Prediction("prediction1", null, null, null, null, null));
    }

    @Test
    @DisplayName("Should throw error when no user id is provided")
    void shouldThrowErrorWhenNoUserIdIsProvided() {
        assertThrows(IllegalArgumentException.class, () -> new Prediction("prediction1", "match1", null, null, null, null));
    }

    @Test
    @DisplayName("Should throw error when no score is provided")
    void shouldThrowErrorWhenNoScoreIsProvided() {
        assertThrows(IllegalArgumentException.class, () -> new Prediction("prediction1", "match1", "user1", null, null, null));
    }

    @Test
    @DisplayName("Should throw error when no status is provided")
    void shouldThrowErrorWhenNoStatusIsProvided() {
        assertThrows(IllegalArgumentException.class, () -> new Prediction("prediction1", "match1", "user1", new Score(0, 0), null, null));
    }

    @Test
    @DisplayName("Should throw error when no submitted at is provided")
    void shouldThrowErrorWhenNoSubmittedAtIsProvided() {
        assertThrows(IllegalArgumentException.class, () -> new Prediction("prediction1", "match1", "user1", new Score(0, 0), PredictionStatus.SUBMITTED, null));
    }

    @Test
    @DisplayName("Should create prediction successfully using submit factory method")
    void shouldCreatePredictionSuccessfullyUsingSubmitFactoryMethod() {
        String matchId = "match1";
        String userId = "user1";
        Score score = new Score(2, 1);
        Instant kickOffTime = Instant.now().plus(1, ChronoUnit.HOURS);

        var prediction = Prediction.submit(matchId, userId, score, kickOffTime);

        assertEquals(matchId, prediction.getMatchId());
        assertEquals(userId, prediction.getUserId());
        assertEquals(score, prediction.getScore());
        assertEquals(PredictionStatus.SUBMITTED, prediction.getStatus());
        assertNotNull(prediction.getSubmittedAt());
        assertNotNull(prediction.getId());
        assertFalse(prediction.getId().isBlank());
    }

    @Test
    @DisplayName("Should throw when submitting after kick-off")
    void shouldThrowWhenSubmittingAfterKickOff() {
        Instant kickOffTime = Instant.now().minus(1, ChronoUnit.SECONDS);

        assertThrows(KickOffAlreadyPassedException.class,
            () -> Prediction.submit("match1", "user1", new Score(2, 1), kickOffTime));
    }

    @Test
    @DisplayName("Should throw when submitting exactly at kick-off")
    void shouldThrowWhenSubmittingAtKickOff() {
        Instant kickOffTime = Instant.now().minus(1, ChronoUnit.MILLIS);

        assertThrows(KickOffAlreadyPassedException.class,
            () -> Prediction.submit("match1", "user1", new Score(2, 1), kickOffTime));
    }

    @Test
    @DisplayName("Should raise a PredictionSubmitted event when submit is called")
    void shouldRaisePredictionSubmittedEvent() {
        Instant kickOffTime = Instant.now().plus(1, ChronoUnit.HOURS);
        var prediction = Prediction.submit("match1", "user1", new Score(2, 1), kickOffTime);

        var events = prediction.pullDomainEvents();

        assertEquals(1, events.size());
        assertInstanceOf(PredictionSubmitted.class, events.getFirst());
        var event = (PredictionSubmitted) events.getFirst();
        assertEquals(prediction.getId(), event.predictionId());
        assertEquals("match1", event.matchId());
        assertEquals("user1", event.userId());
        assertEquals(new Score(2, 1), event.score());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("Should clear events after pulling them")
    void shouldClearEventsAfterPulling() {
        Instant kickOffTime = Instant.now().plus(1, ChronoUnit.HOURS);
        var prediction = Prediction.submit("match1", "user1", new Score(2, 1), kickOffTime);

        prediction.pullDomainEvents();

        assertTrue(prediction.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Should transition status to CLOSED when close() is called")
    void shouldTransitionToClosedOnClose() {
        var prediction = new Prediction("p-1", "m-1", "u-1", new Score(1, 0), PredictionStatus.SUBMITTED, Instant.now());

        prediction.close();

        assertEquals(PredictionStatus.CLOSED, prediction.getStatus());
    }

    @Test
    @DisplayName("Should throw PredictionAlreadyClosedException when closing an already-closed prediction")
    void shouldThrowWhenClosingAlreadyClosedPrediction() {
        var prediction = new Prediction("p-2", "m-1", "u-1", new Score(0, 0), PredictionStatus.CLOSED, Instant.now());

        assertThrows(PredictionAlreadyClosedException.class, prediction::close);
    }

}
