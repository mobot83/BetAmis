package com.betamis.prediction.infrastructure.redis;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class RedisRankingReaderAdapterIT {

    @Inject
    RedisRankingReaderAdapter adapter;

    @Inject
    RedisDataSource redisDataSource;

    private SortedSetCommands<String, String> sortedSet;
    private KeyCommands<String> keyCommands;

    private static final String LEAGUE = "test-reader:league";

    @BeforeEach
    void setUp() {
        sortedSet = redisDataSource.sortedSet(String.class);
        keyCommands = redisDataSource.key(String.class);
        keyCommands.del("ranking:" + LEAGUE);
    }

    @Test
    @DisplayName("findLeagueRanking returns entries sorted by descending score (rank 1 = most points)")
    void findLeagueRanking_sortedByPointsDesc() {
        sortedSet.zadd("ranking:" + LEAGUE, 5.0, "user-b");
        sortedSet.zadd("ranking:" + LEAGUE, 10.0, "user-a");
        sortedSet.zadd("ranking:" + LEAGUE, 1.0, "user-c");

        List<RankingEntry> ranking = adapter.findLeagueRanking(LEAGUE);

        assertEquals(3, ranking.size());
        assertEquals("user-a", ranking.get(0).userId());
        assertEquals(10L, ranking.get(0).totalPoints());
        assertEquals(1, ranking.get(0).rank());
        assertEquals("user-b", ranking.get(1).userId());
        assertEquals(2, ranking.get(1).rank());
        assertEquals("user-c", ranking.get(2).userId());
        assertEquals(3, ranking.get(2).rank());
    }

    @Test
    @DisplayName("findLeagueRanking returns empty list for a league with no entries")
    void findLeagueRanking_emptyForUnknownLeague() {
        assertTrue(adapter.findLeagueRanking("unknown-league").isEmpty());
    }
}
