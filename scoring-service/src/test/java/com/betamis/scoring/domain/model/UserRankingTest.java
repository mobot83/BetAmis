package com.betamis.scoring.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRankingTest {

    @Test
    @DisplayName("Should create a valid UserRanking")
    void createValid() {
        UserRanking ranking = new UserRanking("user-1", "global", 9L, 1);
        assertEquals("user-1", ranking.userId());
        assertEquals("global", ranking.leagueId());
        assertEquals(9L, ranking.totalPoints());
        assertEquals(1, ranking.rank());
    }

    @Test
    @DisplayName("Should allow zero totalPoints")
    void zeroTotalPointsIsValid() {
        assertDoesNotThrow(() -> new UserRanking("user-1", "global", 0L, 1));
    }

    @Test
    @DisplayName("Should throw when userId is null")
    void nullUserId() {
        assertThrows(IllegalArgumentException.class, () -> new UserRanking(null, "global", 0L, 1));
    }

    @Test
    @DisplayName("Should throw when userId is blank")
    void blankUserId() {
        assertThrows(IllegalArgumentException.class, () -> new UserRanking("   ", "global", 0L, 1));
    }

    @Test
    @DisplayName("Should throw when leagueId is null")
    void nullLeagueId() {
        assertThrows(IllegalArgumentException.class, () -> new UserRanking("user-1", null, 0L, 1));
    }

    @Test
    @DisplayName("Should throw when leagueId is blank")
    void blankLeagueId() {
        assertThrows(IllegalArgumentException.class, () -> new UserRanking("user-1", "   ", 0L, 1));
    }

    @Test
    @DisplayName("Should throw when totalPoints is negative")
    void negativeTotalPoints() {
        assertThrows(IllegalArgumentException.class, () -> new UserRanking("user-1", "global", -1L, 1));
    }

    @Test
    @DisplayName("Should throw when rank is zero")
    void zeroRank() {
        assertThrows(IllegalArgumentException.class, () -> new UserRanking("user-1", "global", 0L, 0));
    }

    @Test
    @DisplayName("Should throw when rank is negative")
    void negativeRank() {
        assertThrows(IllegalArgumentException.class, () -> new UserRanking("user-1", "global", 0L, -1));
    }
}
