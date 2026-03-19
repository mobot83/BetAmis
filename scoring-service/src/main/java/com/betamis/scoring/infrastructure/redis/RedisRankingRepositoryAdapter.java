package com.betamis.scoring.infrastructure.redis;

import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.out.RankingRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.ScoredValue;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

@ApplicationScoped
public class RedisRankingRepositoryAdapter implements RankingRepository {

    private static final String KEY_PREFIX = "ranking:";

    private final SortedSetCommands<String, String> sortedSet;

    @Inject
    public RedisRankingRepositoryAdapter(RedisDataSource redis) {
        this.sortedSet = redis.sortedSet(String.class);
    }

    @Override
    public UserRanking addPoints(String userId, String leagueId, int points) {
        String key = KEY_PREFIX + leagueId;
        double newScore = sortedSet.zincrby(key, points, userId);
        OptionalLong zeroBasedRank = sortedSet.zrevrank(key, userId);
        int rank = zeroBasedRank.isEmpty() ? 1 : (int) (zeroBasedRank.getAsLong() + 1);
        return new UserRanking(userId, leagueId, (long) newScore, rank);
    }

    @Override
    public List<UserRanking> findLeagueRanking(String leagueId) {
        String key = KEY_PREFIX + leagueId;
        // zrangebyscoreWithScores returns ascending; iterate in reverse for rank 1 = highest score
        List<ScoredValue<String>> entries = sortedSet.zrangebyscoreWithScores(key, ScoreRange.unbounded());
        List<UserRanking> result = new ArrayList<>(entries.size());
        for (int i = entries.size() - 1; i >= 0; i--) {
            ScoredValue<String> sv = entries.get(i);
            result.add(new UserRanking(sv.value(), leagueId, (long) sv.score(), result.size() + 1));
        }
        return result;
    }
}
