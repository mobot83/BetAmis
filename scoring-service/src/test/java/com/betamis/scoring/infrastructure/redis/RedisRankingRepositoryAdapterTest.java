package com.betamis.scoring.infrastructure.redis;

import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.out.RankingRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RedisRankingRepositoryAdapterTest {

    // Inject the concrete adapter directly so the real implementation is exercised,
    // not a mock that might be registered for the RankingRepository interface.
    @Inject
    RedisRankingRepositoryAdapter adapter;

    @Inject
    RedisDataSource redisDataSource;

    private KeyCommands<String> keyCommands;

    // Unique per-method league names avoid state bleed between tests
    private static final String LEAGUE_SINGLE   = "redis-test:single";
    private static final String LEAGUE_MULTI    = "redis-test:multi";
    private static final String LEAGUE_CUMUL    = "redis-test:cumulative";
    private static final String LEAGUE_FIND     = "redis-test:find";
    private static final String LEAGUE_EMPTY    = "redis-test:empty";

    @BeforeEach
    void cleanRedis() {
        keyCommands = redisDataSource.key(String.class);
        keyCommands.del(
                "ranking:" + LEAGUE_SINGLE,
                "ranking:" + LEAGUE_MULTI,
                "ranking:" + LEAGUE_CUMUL,
                "ranking:" + LEAGUE_FIND,
                "ranking:" + LEAGUE_EMPTY
        );
    }

    @Test
    @DisplayName("addPoints for a single user should return rank 1 with the correct total")
    void addPointsSingleUser() {
        UserRanking ranking = adapter.addPoints("user-1", LEAGUE_SINGLE, 3);

        assertEquals("user-1", ranking.userId());
        assertEquals(LEAGUE_SINGLE, ranking.leagueId());
        assertEquals(3L, ranking.totalPoints());
        assertEquals(1, ranking.rank());
    }

    @Test
    @DisplayName("addPoints is cumulative — subsequent calls accumulate points")
    void addPointsIsCumulative() {
        adapter.addPoints("user-1", LEAGUE_CUMUL, 3);
        UserRanking afterSecond = adapter.addPoints("user-1", LEAGUE_CUMUL, 1);

        assertEquals(4L, afterSecond.totalPoints());
        assertEquals(1, afterSecond.rank());
    }

    @Test
    @DisplayName("addPoints assigns rank 1 to the user with the most points")
    void addPointsRankOrderForMultipleUsers() {
        adapter.addPoints("user-low",  LEAGUE_MULTI, 1);
        adapter.addPoints("user-mid",  LEAGUE_MULTI, 3);
        UserRanking top = adapter.addPoints("user-high", LEAGUE_MULTI, 6);

        assertEquals("user-high", top.userId());
        assertEquals(1, top.rank()); // highest score → rank 1
    }

    @Test
    @DisplayName("addPoints assigns increasing ranks by descending score")
    void addPointsRankReflectsPosition() {
        adapter.addPoints("user-a", LEAGUE_MULTI, 6);
        adapter.addPoints("user-b", LEAGUE_MULTI, 3);
        UserRanking last = adapter.addPoints("user-c", LEAGUE_MULTI, 1);

        assertEquals(3, last.rank()); // user-c has the lowest score → rank 3
    }

    @Test
    @DisplayName("addPoints with 0 points still records the user and returns a valid rank")
    void addZeroPoints() {
        UserRanking ranking = adapter.addPoints("user-1", LEAGUE_SINGLE, 0);

        assertEquals(0L, ranking.totalPoints());
        assertEquals(1, ranking.rank());
    }

    @Test
    @DisplayName("findLeagueRanking returns entries sorted by descending score (rank 1 = most points)")
    void findLeagueRankingDescendingOrder() {
        adapter.addPoints("user-bronze", LEAGUE_FIND, 1);
        adapter.addPoints("user-silver", LEAGUE_FIND, 3);
        adapter.addPoints("user-gold",   LEAGUE_FIND, 9);

        List<UserRanking> ranking = adapter.findLeagueRanking(LEAGUE_FIND);

        assertEquals(3, ranking.size());
        assertEquals("user-gold",   ranking.get(0).userId());
        assertEquals(9L,             ranking.get(0).totalPoints());
        assertEquals(1,              ranking.get(0).rank());

        assertEquals("user-silver", ranking.get(1).userId());
        assertEquals(3L,             ranking.get(1).totalPoints());
        assertEquals(2,              ranking.get(1).rank());

        assertEquals("user-bronze", ranking.get(2).userId());
        assertEquals(1L,             ranking.get(2).totalPoints());
        assertEquals(3,              ranking.get(2).rank());
    }

    @Test
    @DisplayName("findLeagueRanking returns all entries with correct leagueId")
    void findLeagueRankingContainsCorrectLeagueId() {
        adapter.addPoints("user-1", LEAGUE_FIND, 5);

        List<UserRanking> ranking = adapter.findLeagueRanking(LEAGUE_FIND);

        assertFalse(ranking.isEmpty());
        ranking.forEach(r -> assertEquals(LEAGUE_FIND, r.leagueId()));
    }

    @Test
    @DisplayName("findLeagueRanking returns empty list for a league with no entries")
    void findLeagueRankingEmptyForUnknownLeague() {
        List<UserRanking> ranking = adapter.findLeagueRanking(LEAGUE_EMPTY);

        assertTrue(ranking.isEmpty());
    }

    @Test
    @DisplayName("findLeagueRanking result is consistent with addPoints rank")
    void findLeagueRankingConsistentWithAddPoints() {
        UserRanking fromAdd = adapter.addPoints("user-1", LEAGUE_FIND, 7);
        adapter.addPoints("user-2", LEAGUE_FIND, 3);

        List<UserRanking> listing = adapter.findLeagueRanking(LEAGUE_FIND);
        UserRanking fromFind = listing.stream()
                .filter(r -> r.userId().equals("user-1"))
                .findFirst()
                .orElseThrow();

        assertEquals(fromAdd.rank(), fromFind.rank());
        assertEquals(fromAdd.totalPoints(), fromFind.totalPoints());
    }
}
