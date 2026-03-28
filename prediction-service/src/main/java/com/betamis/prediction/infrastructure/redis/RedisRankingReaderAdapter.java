package com.betamis.prediction.infrastructure.redis;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import com.betamis.prediction.domain.port.out.RankingReader;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.ScoredValue;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RedisRankingReaderAdapter implements RankingReader {

    private static final String KEY_PREFIX = "ranking:";

    private final SortedSetCommands<String, String> sortedSet;

    @Inject
    public RedisRankingReaderAdapter(RedisDataSource redis) {
        this.sortedSet = redis.sortedSet(String.class);
    }

    @Override
    public List<RankingEntry> findLeagueRanking(String leagueId) {
        String key = KEY_PREFIX + leagueId;
        List<ScoredValue<String>> entries = sortedSet.zrangebyscoreWithScores(key, ScoreRange.unbounded());
        List<RankingEntry> result = new ArrayList<>(entries.size());
        for (int i = entries.size() - 1; i >= 0; i--) {
            ScoredValue<String> sv = entries.get(i);
            result.add(new RankingEntry(result.size() + 1, sv.value(), (long) sv.score()));
        }
        return result;
    }
}
