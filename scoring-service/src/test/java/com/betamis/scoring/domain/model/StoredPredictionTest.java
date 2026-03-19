package com.betamis.scoring.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoredPredictionTest {

    @Test
    @DisplayName("Should create a valid StoredPrediction")
    void createValid() {
        StoredPrediction prediction = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1));

        assertEquals("pred-1", prediction.predictionId());
        assertEquals("match-1", prediction.matchId());
        assertEquals("user-1", prediction.userId());
        assertEquals(new FinalScore(2, 1), prediction.predictedScore());
    }

    @Test
    @DisplayName("Should throw when predictionId is null")
    void nullPredictionId() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoredPrediction(null, "match-1", "user-1", new FinalScore(1, 0)));
    }

    @Test
    @DisplayName("Should throw when predictionId is blank")
    void blankPredictionId() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoredPrediction("   ", "match-1", "user-1", new FinalScore(1, 0)));
    }

    @Test
    @DisplayName("Should throw when matchId is null")
    void nullMatchId() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoredPrediction("pred-1", null, "user-1", new FinalScore(1, 0)));
    }

    @Test
    @DisplayName("Should throw when matchId is blank")
    void blankMatchId() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoredPrediction("pred-1", "   ", "user-1", new FinalScore(1, 0)));
    }

    @Test
    @DisplayName("Should throw when userId is null")
    void nullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoredPrediction("pred-1", "match-1", null, new FinalScore(1, 0)));
    }

    @Test
    @DisplayName("Should throw when userId is blank")
    void blankUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoredPrediction("pred-1", "match-1", "   ", new FinalScore(1, 0)));
    }

    @Test
    @DisplayName("Should throw when predictedScore is null")
    void nullPredictedScore() {
        assertThrows(IllegalArgumentException.class,
                () -> new StoredPrediction("pred-1", "match-1", "user-1", null));
    }

    @Test
    @DisplayName("Should consider two StoredPredictions with same fields as equal (record equality)")
    void equalityByValue() {
        StoredPrediction a = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1));
        StoredPrediction b = new StoredPrediction("pred-1", "match-1", "user-1", new FinalScore(2, 1));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
