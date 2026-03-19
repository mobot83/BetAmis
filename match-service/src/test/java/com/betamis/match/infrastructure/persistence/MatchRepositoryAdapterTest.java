package com.betamis.match.infrastructure.persistence;

import com.betamis.match.domain.model.match.Match;
import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.domain.port.out.MatchRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MatchRepositoryAdapterTest {

    @Inject
    MatchRepository matchRepository;

    @Test
    @Transactional
    @DisplayName("Should persist a match and find it by external id")
    void shouldSaveAndFindByExternalId() {
        Match match = Match.fromExternal(1001L, "10", "20", 0, 0, MatchStatus.PLANNED);

        matchRepository.save(match);

        Optional<Match> found = matchRepository.findByExternalId(1001L);
        assertTrue(found.isPresent());
        assertEquals(match.getId(), found.get().getId());
        assertEquals(Optional.of(1001L), found.get().getExternalId());
        assertEquals("10", found.get().getHomeTeamId());
        assertEquals("20", found.get().getAwayTeamId());
        assertEquals(MatchStatus.PLANNED, found.get().getStatus());
    }

    @Test
    @Transactional
    @DisplayName("Should update existing match when saved again with same matchId")
    void shouldUpdateExistingMatchOnSave() {
        Match original = Match.fromExternal(1002L, "10", "20", 0, 0, MatchStatus.PLANNED);
        matchRepository.save(original);

        Match updated = original.withUpdate(3, 1, MatchStatus.FINISHED);
        matchRepository.save(updated);

        Optional<Match> found = matchRepository.findByExternalId(1002L);
        assertTrue(found.isPresent());
        assertEquals(MatchStatus.FINISHED, found.get().getStatus());
        assertEquals(3, found.get().getHomeTeamScore());
        assertEquals(1, found.get().getAwayTeamScore());
    }

    @Test
    @Transactional
    @DisplayName("Should return empty when external id does not exist")
    void shouldReturnEmptyForUnknownExternalId() {
        Optional<Match> found = matchRepository.findByExternalId(999999L);
        assertTrue(found.isEmpty());
    }
}
