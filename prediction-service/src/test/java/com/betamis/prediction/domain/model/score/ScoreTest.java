package com.betamis.prediction.domain.model.score;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    @Test
    @DisplayName("Should create valid score")
    void shouldCreateValidScore() {
        var score = new Score(2, 1);
        assertEquals(2, score.homeTeamScore());
        assertEquals(1, score.awayTeamScore());
    }

    @Test
    @DisplayName("Should accept 0-0 score")
    void shouldAcceptZeroZeroScore() {
        assertDoesNotThrow(() -> new Score(0, 0));
    }

    @Test
    @DisplayName("Should accept 100-100 score")
    void shouldAcceptHundredHundredScore() {
        assertDoesNotThrow(() -> new Score(100, 100));
    }

    @Test
    @DisplayName("Should throw when home score is negative")
    void shouldThrowWhenHomeScoreIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Score(-1, 0));
    }

    @Test
    @DisplayName("Should throw when away score is negative")
    void shouldThrowWhenAwayScoreIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Score(0, -1));
    }

    @Test
    @DisplayName("Should throw when home score exceeds 100")
    void shouldThrowWhenHomeScoreExceeds100() {
        assertThrows(IllegalArgumentException.class, () -> new Score(101, 0));
    }

    @Test
    @DisplayName("Should throw when away score exceeds 100")
    void shouldThrowWhenAwayScoreExceeds100() {
        assertThrows(IllegalArgumentException.class, () -> new Score(0, 101));
    }
}
