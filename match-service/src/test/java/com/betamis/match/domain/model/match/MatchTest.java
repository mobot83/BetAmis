package com.betamis.match.domain.model.match;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    @Test
    @DisplayName("Should create a valid Match")
    void createMatch() {
        Match match = new Match("match-1", 99L, "team-1", "team-2", 2, 1, MatchStatus.PLANNED);

        assertEquals("match-1", match.getId());
        assertEquals(Optional.of(99L), match.getExternalId());
        assertEquals("team-1", match.getHomeTeamId());
        assertEquals("team-2", match.getAwayTeamId());
        assertEquals(2, match.getHomeTeamScore());
        assertEquals(1, match.getAwayTeamScore());
        assertEquals(MatchStatus.PLANNED, match.getStatus());
    }

    @Test
    @DisplayName("Should return empty Optional when externalId is null")
    void createMatchWithNullExternalId() {
        Match match = new Match("match-1", null, "team-1", "team-2", 0, 0, MatchStatus.PLANNED);
        assertTrue(match.getExternalId().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when creating Match with null id")
    void createMatchWithNullId() {
        assertThrows(IllegalArgumentException.class, () -> new Match(null, null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with blank id")
    void createMatchWithBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new Match("   ", null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with null homeTeamId")
    void createMatchWithNoHomeTeamId() {
        assertThrows(IllegalArgumentException.class, () -> new Match("match-1", null, null, "team-2", 2, 1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with blank homeTeamId")
    void createMatchWithBlankHomeTeamId() {
        assertThrows(IllegalArgumentException.class, () -> new Match("match-1", null, "   ", "team-2", 2, 1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with null awayTeamId")
    void createMatchWithNoAwayTeamId() {
        assertThrows(IllegalArgumentException.class, () -> new Match("match-1", null, "team-1", null, 2, 1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with blank awayTeamId")
    void createMatchWithBlankAwayTeamId() {
        assertThrows(IllegalArgumentException.class, () -> new Match("match-1", null, "team-1", "   ", 2, 1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with negative homeTeamScore")
    void createMatchWithNegativeHomeTeamScore() {
        assertThrows(IllegalArgumentException.class, () -> new Match("match-1", null, "team-1", "team-2", -1, 1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with negative awayTeamScore")
    void createMatchWithNegativeAwayTeamScore() {
        assertThrows(IllegalArgumentException.class, () -> new Match("match-1", null, "team-1", "team-2", 2, -1, MatchStatus.PLANNED));
    }

    @Test
    @DisplayName("Should throw exception when creating Match with null status")
    void createMatchWithNullStatus() {
        assertThrows(IllegalArgumentException.class, () -> new Match("match-1", null, "team-1", "team-2", 2, 1, null));
    }

    @Test
    @DisplayName("Should consider two Matches with same id as equal")
    void twoMatchesWithSameIdAreEqual() {
        Match match1 = new Match("match-1", null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED);
        Match match2 = new Match("match-1", null, "team-1", "team-2", 4, 0, MatchStatus.STARTED);
        assertEquals(match1, match2);
    }

    @Test
    @DisplayName("Should consider two Matches with different id as not equal")
    void twoMatchesWithDifferentIdAreNotEqual() {
        Match match1 = new Match("match-1", null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED);
        Match match2 = new Match("match-2", null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED);
        assertNotEquals(match1, match2);
    }

    @Test
    @DisplayName("Should not be equal for different classes")
    void matchNotEqualToDifferentClass() {
        Match match = new Match("match-1", null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED);
        assertNotEquals(match, "I am not a match");
    }

    @Test
    @DisplayName("Should not be equal against null")
    void matchNotEqualToNull() {
        Match match = new Match("match-1", null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED);
        assertNotEquals(match, null);
    }

    @Test
    @DisplayName("Should have same hash code for two Matches with same id")
    void twoMatchesWithSameIdHaveSameHashCode() {
        Match match1 = new Match("match-1", null, "team-1", "team-2", 2, 1, MatchStatus.PLANNED);
        Match match2 = new Match("match-1", null, "team-1", "team-2", 4, 0, MatchStatus.STARTED);
        assertEquals(match1.hashCode(), match2.hashCode());
    }

    @Test
    @DisplayName("fromExternal() factory method should carry external id and all fields")
    void fromExternalFactoryMethod() {
        Match match = Match.fromExternal(42L, "1", "2", 1, 0, MatchStatus.STARTED);

        assertNotNull(match.getId());
        assertEquals(Optional.of(42L), match.getExternalId());
        assertEquals("1", match.getHomeTeamId());
        assertEquals("2", match.getAwayTeamId());
        assertEquals(1, match.getHomeTeamScore());
        assertEquals(0, match.getAwayTeamScore());
        assertEquals(MatchStatus.STARTED, match.getStatus());
    }

    @Test
    @DisplayName("withUpdate() should return a new instance with updated score and status, same id")
    void withUpdateProducesUpdatedMatch() {
        Match original = new Match("match-1", 10L, "team-1", "team-2", 0, 0, MatchStatus.PLANNED);

        Match updated = original.withUpdate(2, 1, MatchStatus.FINISHED);

        assertEquals("match-1", updated.getId());
        assertEquals(Optional.of(10L), updated.getExternalId());
        assertEquals("team-1", updated.getHomeTeamId());
        assertEquals("team-2", updated.getAwayTeamId());
        assertEquals(2, updated.getHomeTeamScore());
        assertEquals(1, updated.getAwayTeamScore());
        assertEquals(MatchStatus.FINISHED, updated.getStatus());
    }
}
