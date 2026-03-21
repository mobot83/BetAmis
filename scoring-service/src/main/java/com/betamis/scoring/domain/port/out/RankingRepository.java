package com.betamis.scoring.domain.port.out;

import com.betamis.scoring.domain.model.UserRanking;

import java.util.List;

public interface RankingRepository {
    /**
     * Increment the user's score in the given league and return the updated ranking.
     */
    UserRanking addPoints(String userId, String leagueId, int points);

    /**
     * Return all entries for the given league, sorted by rank (ascending).
     */
    List<UserRanking> findLeagueRanking(String leagueId);
}
