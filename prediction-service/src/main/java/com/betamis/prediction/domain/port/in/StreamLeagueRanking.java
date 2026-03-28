package com.betamis.prediction.domain.port.in;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import org.reactivestreams.Publisher;

public interface StreamLeagueRanking {
    Publisher<RankingEntry> stream(String leagueId);
}
