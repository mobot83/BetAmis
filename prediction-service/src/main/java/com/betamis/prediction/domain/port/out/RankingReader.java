package com.betamis.prediction.domain.port.out;

import com.betamis.prediction.domain.model.ranking.RankingEntry;

import java.util.List;

public interface RankingReader {
    List<RankingEntry> findLeagueRanking(String leagueId);
}
