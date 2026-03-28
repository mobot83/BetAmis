package com.betamis.prediction.domain.port.in;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import java.util.concurrent.Flow;

public interface StreamLeagueRanking {
    Flow.Publisher<RankingEntry> stream(String leagueId);
}
