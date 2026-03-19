package com.betamis.scoring.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinalScoreTest {

    @Test
    @DisplayName("Should detect home win")
    void homeWin() {
        FinalScore score = new FinalScore(2, 0);
        assertTrue(score.isHomeWin());
        assertFalse(score.isAwayWin());
        assertFalse(score.isDraw());
    }

    @Test
    @DisplayName("Should detect away win")
    void awayWin() {
        FinalScore score = new FinalScore(0, 1);
        assertFalse(score.isHomeWin());
        assertTrue(score.isAwayWin());
        assertFalse(score.isDraw());
    }

    @Test
    @DisplayName("Should detect draw")
    void draw() {
        FinalScore score = new FinalScore(1, 1);
        assertFalse(score.isHomeWin());
        assertFalse(score.isAwayWin());
        assertTrue(score.isDraw());
    }

    @Test
    @DisplayName("Should compute goal difference")
    void goalDifference() {
        assertEquals(2, new FinalScore(3, 1).goalDifference());
        assertEquals(-1, new FinalScore(0, 1).goalDifference());
        assertEquals(0, new FinalScore(1, 1).goalDifference());
    }

    @Test
    @DisplayName("Should throw on negative homeTeamScore")
    void negativeHomeScore() {
        assertThrows(IllegalArgumentException.class, () -> new FinalScore(-1, 0));
    }

    @Test
    @DisplayName("Should throw on negative awayTeamScore")
    void negativeAwayScore() {
        assertThrows(IllegalArgumentException.class, () -> new FinalScore(0, -1));
    }
}
