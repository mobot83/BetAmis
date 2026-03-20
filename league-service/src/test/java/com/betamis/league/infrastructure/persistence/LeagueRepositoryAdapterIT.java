package com.betamis.league.infrastructure.persistence;

import com.betamis.league.domain.model.Invitation;
import com.betamis.league.domain.model.League;
import com.betamis.league.domain.port.out.LeagueRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the repository adapter using a real PostgreSQL database
 * started automatically by Quarkus DevServices (Testcontainers).
 *
 * <p>Each test calls {@code em.flush()} + {@code em.clear()} after writes to evict
 * Hibernate's first-level cache, ensuring that the subsequent {@code findById} reads
 * from the database rather than from the in-memory identity map.
 */
@QuarkusTest
class LeagueRepositoryAdapterIT {

    @Inject
    LeagueRepository leagueRepository;

    @Inject
    EntityManager em;

    @Test
    @Transactional
    @DisplayName("Should persist a league and find it by id")
    void shouldSaveAndFindById() {
        League league = League.create("Champions League 2026", "user-1");
        leagueRepository.save(league);
        em.flush();
        em.clear();

        Optional<League> found = leagueRepository.findById(league.getId());

        assertTrue(found.isPresent());
        assertEquals(league.getId(), found.get().getId());
        assertEquals("Champions League 2026", found.get().getName());
        assertEquals("user-1", found.get().getOwnerId());
        assertEquals(1, found.get().getMemberships().size());
        assertEquals("user-1", found.get().getMemberships().get(0).userId());
        assertEquals(1, found.get().getInvitations().size());
    }

    @Test
    @Transactional
    @DisplayName("Should return empty when league does not exist")
    void shouldReturnEmptyForUnknownId() {
        Optional<League> found = leagueRepository.findById("non-existent-id");
        assertTrue(found.isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should update league memberships when saved again")
    void shouldUpdateLeagueMembershipsOnSave() {
        League league = League.create("My League", "owner-1");
        String code = league.getInvitations().get(0).code();
        String leagueId = league.getId();

        leagueRepository.save(league);
        em.flush();
        em.clear();

        // Reload fresh from DB so the join operates on a clean aggregate
        League reloaded = leagueRepository.findById(leagueId).orElseThrow();
        reloaded.join("member-2", code, Instant.now());
        leagueRepository.save(reloaded);
        em.flush();
        em.clear();

        Optional<League> found = leagueRepository.findById(leagueId);
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getMemberships().size());
        assertTrue(found.get().getMemberships().stream()
                .anyMatch(m -> m.userId().equals("member-2")));
    }

    @Test
    @Transactional
    @DisplayName("Should persist invitations with correct expiry date (7 days from creation)")
    void shouldPersistInvitationsWithCorrectExpiry() {
        League league = League.create("Expiry Test League", "user-99");
        leagueRepository.save(league);
        em.flush();
        em.clear();

        Optional<League> found = leagueRepository.findById(league.getId());
        assertTrue(found.isPresent());

        Invitation inv = found.get().getInvitations().get(0);
        Instant now = Instant.now();
        assertTrue(inv.isValid(now));
        long daysDiff = ChronoUnit.DAYS.between(now, inv.expiresAt());
        assertTrue(daysDiff >= 6 && daysDiff <= 7);
    }
}
