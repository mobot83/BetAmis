package com.betamis.prediction.infrastructure.persistence;

import com.betamis.prediction.domain.model.prediction.Prediction;
import com.betamis.prediction.domain.model.prediction.PredictionStatus;
import com.betamis.prediction.domain.model.score.Score;
import com.betamis.prediction.domain.port.out.PredictionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PredictionRepositoryAdapter using a real PostgreSQL database (Quarkus DevServices).
 * Each test is @Transactional so saves and reads share the same transaction.
 * em.flush() + em.clear() after writes ensures reads bypass the identity-map cache.
 */
@QuarkusTest
class PredictionRepositoryAdapterIT {

    @Inject
    PredictionRepository repository;

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void save_and_findById_roundtrip() {
        Prediction prediction = prediction("match-db-1", "user-db-1");
        repository.save(prediction);
        em.flush();
        em.clear();

        Prediction found = repository.findById(prediction.getId());

        assertNotNull(found);
        assertEquals(prediction.getId(), found.getId());
        assertEquals("match-db-1", found.getMatchId());
        assertEquals("user-db-1", found.getUserId());
        assertEquals(new Score(2, 1), found.getScore());
        assertEquals(PredictionStatus.SUBMITTED, found.getStatus());
    }

    @Test
    @Transactional
    void findById_returnsNull_whenNotFound() {
        assertNull(repository.findById("non-existent-id"));
    }

    @Test
    @Transactional
    void findByMatchId_returnsAllPredictionsForMatch() {
        String matchId = UUID.randomUUID().toString();
        Prediction p1 = prediction(matchId, "user-db-2");
        Prediction p2 = prediction(matchId, "user-db-3");
        repository.save(p1);
        repository.save(p2);
        em.flush();
        em.clear();

        List<Prediction> found = repository.findByMatchId(matchId);

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(p -> p.getUserId().equals("user-db-2")));
        assertTrue(found.stream().anyMatch(p -> p.getUserId().equals("user-db-3")));
    }

    @Test
    @Transactional
    void findByMatchId_returnsEmpty_whenNoMatchFound() {
        assertTrue(repository.findByMatchId("match-no-predictions").isEmpty());
    }

    @Test
    @Transactional
    void update_persistsStatusChange() {
        Prediction prediction = prediction("match-db-upd", "user-db-upd");
        repository.save(prediction);
        em.flush();
        em.clear();

        Prediction loaded = repository.findById(prediction.getId());
        loaded.close();
        repository.update(loaded);
        em.flush();
        em.clear();

        Prediction updated = repository.findById(prediction.getId());
        assertEquals(PredictionStatus.CLOSED, updated.getStatus());
    }

    @Test
    @Transactional
    void existsByUserIdAndMatchId_returnsTrue_whenExists() {
        Prediction prediction = prediction("match-db-ex", "user-db-ex");
        repository.save(prediction);
        em.flush();
        em.clear();

        assertTrue(repository.existsByUserIdAndMatchId("user-db-ex", "match-db-ex"));
    }

    @Test
    @Transactional
    void existsByUserIdAndMatchId_returnsFalse_whenNotExists() {
        assertFalse(repository.existsByUserIdAndMatchId("ghost-user", "ghost-match"));
    }

    private Prediction prediction(String matchId, String userId) {
        return new Prediction(
                UUID.randomUUID().toString(),
                matchId,
                userId,
                new Score(2, 1),
                PredictionStatus.SUBMITTED,
                Instant.now()
        );
    }
}
