package com.betamis.scoring.infrastructure.messaging;

import com.betamis.ranking.event.RankingUpdated;
import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.out.ScoringEventPublisher;
import com.betamis.scoring.event.PointsCalculated;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class KafkaScoringEventPublisher implements ScoringEventPublisher {

    private final Emitter<PointsCalculated> pointsCalculatedEmitter;
    private final Emitter<RankingUpdated> rankingUpdatedEmitter;

    @Inject
    public KafkaScoringEventPublisher(
            @Channel("points-calculated") Emitter<PointsCalculated> pointsCalculatedEmitter,
            @Channel("ranking-updated") Emitter<RankingUpdated> rankingUpdatedEmitter) {
        this.pointsCalculatedEmitter = pointsCalculatedEmitter;
        this.rankingUpdatedEmitter = rankingUpdatedEmitter;
    }

    @Override
    public void publishPointsCalculated(ScoringResult result) {
        PointsCalculated event = PointsCalculated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setPredictionId(result.predictionId())
                .setUserId(result.userId())
                .setMatchId(result.matchId())
                .setPoints(result.points())
                .setOccurredAt(Instant.now())
                .build();

        pointsCalculatedEmitter.send(event)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish PointsCalculated for prediction %s", result.predictionId());
                    }
                });
    }

    @Override
    public void publishRankingUpdated(UserRanking ranking) {
        RankingUpdated event = RankingUpdated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setUserId(ranking.userId())
                .setLeagueId(ranking.leagueId())
                .setTotalPoints(ranking.totalPoints())
                .setRank(ranking.rank())
                .setOccurredAt(Instant.now())
                .build();

        rankingUpdatedEmitter.send(event)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.errorf(throwable, "Failed to publish RankingUpdated for user %s", ranking.userId());
                    }
                });
    }
}
