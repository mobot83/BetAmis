package com.betamis.prediction.infrastructure.sse;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import com.betamis.prediction.domain.port.in.StreamLeagueRanking;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import org.reactivestreams.Publisher;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory broadcaster that fans out RankingEntry events to all SSE subscribers
 * watching a given league.
 *
 * TODO: processors map grows unboundedly (one entry per distinct leagueId ever streamed).
 *       Consider evicting entries when subscriber count drops to zero if league count becomes large.
 */
@ApplicationScoped
public class RankingBroadcaster implements StreamLeagueRanking {

    private final ConcurrentHashMap<String, BroadcastProcessor<RankingEntry>> processors =
            new ConcurrentHashMap<>();

    public void publish(String leagueId, RankingEntry entry) {
        BroadcastProcessor<RankingEntry> processor = processors.get(leagueId);
        if (processor != null) {
            processor.onNext(entry);
        }
    }

    @Override
    public Publisher<RankingEntry> stream(String leagueId) {
        return processors.computeIfAbsent(leagueId, k -> BroadcastProcessor.create());
    }
}
