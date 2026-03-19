package com.betamis.scoring.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoringResultValidationTest {

    @Test
    @DisplayName("Should throw when predictionId is null")
    void nullPredictionId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringResult(null, "match-1", "user-1", 3));
    }

    @Test
    @DisplayName("Should throw when predictionId is blank")
    void blankPredictionId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringResult("   ", "match-1", "user-1", 3));
    }

    @Test
    @DisplayName("Should throw when matchId is null")
    void nullMatchId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringResult("pred-1", null, "user-1", 3));
    }

    @Test
    @DisplayName("Should throw when matchId is blank")
    void blankMatchId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringResult("pred-1", "   ", "user-1", 3));
    }

    @Test
    @DisplayName("Should throw when userId is null")
    void nullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringResult("pred-1", "match-1", null, 3));
    }

    @Test
    @DisplayName("Should throw when userId is blank")
    void blankUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringResult("pred-1", "match-1", "   ", 3));
    }

    @Test
    @DisplayName("Should throw when points is negative")
    void negativePoints() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringResult("pred-1", "match-1", "user-1", -1));
    }

    @Test
    @DisplayName("Should allow zero points")
    void zeroPoints() {
        ScoringResult result = new ScoringResult("pred-1", "match-1", "user-1", 0);
        assertEquals(0, result.points());
    }

    @Test
    @DisplayName("Should consider two ScoringResults with same fields as equal (record equality)")
    void equalityByValue() {
        ScoringResult a = new ScoringResult("pred-1", "match-1", "user-1", 3);
        ScoringResult b = new ScoringResult("pred-1", "match-1", "user-1", 3);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
