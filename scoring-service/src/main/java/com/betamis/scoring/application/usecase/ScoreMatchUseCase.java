package com.betamis.scoring.application.usecase;

import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.in.ScoreMatch;
import com.betamis.scoring.domain.port.out.LeagueMembershipRepository;
import com.betamis.scoring.domain.port.out.RankingRepository;
import com.betamis.scoring.domain.port.out.ScoringEventPublisher;
import com.betamis.scoring.domain.port.out.ScoringResultRepository;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ScoreMatchUseCase implements ScoreMatch {

    // Points are always added to the global league in addition to each user's own leagues.
    private static final String GLOBAL_LEAGUE = "global";

    private final StoredPredictionRepository predictionRepository;
    private final ScoringResultRepository scoringResultRepository;
    private final RankingRepository rankingRepository;
    private final ScoringEventPublisher eventPublisher;
    private final LeagueMembershipRepository leagueMembershipRepository;
    private final Counter scoresComputedCounter;
    private final Timer scoringTimer;

    @Inject
    public ScoreMatchUseCase(
            StoredPredictionRepository predictionRepository,
            ScoringResultRepository scoringResultRepository,
            RankingRepository rankingRepository,
            ScoringEventPublisher eventPublisher,
            LeagueMembershipRepository leagueMembershipRepository,
            MeterRegistry registry) {
        this.predictionRepository = predictionRepository;
        this.scoringResultRepository = scoringResultRepository;
        this.rankingRepository = rankingRepository;
        this.eventPublisher = eventPublisher;
        this.leagueMembershipRepository = leagueMembershipRepository;
        this.scoresComputedCounter = registry.counter("betamis_scores_computed_total");
        this.scoringTimer = registry.timer("betamis_scoring_duration_seconds");
    }

    @Override
    public void score(String matchId, FinalScore finalScore) {
        long start = System.nanoTime();
        try {
            List<StoredPrediction> predictions = predictionRepository.findByMatchId(matchId);

            for (StoredPrediction prediction : predictions) {
                if (scoringResultRepository.existsByPredictionId(prediction.predictionId())) {
                    Log.infof("Prediction %s already scored, skipping", prediction.predictionId());
                    continue;
                }

                ScoringResult result = ScoringResult.calculate(prediction, finalScore);
                scoringResultRepository.save(result);

                // Update the global league plus every private league the user belongs to.
                List<String> leagues = new ArrayList<>();
                leagues.add(GLOBAL_LEAGUE);
                leagues.addAll(leagueMembershipRepository.findLeagueIdsByUserId(result.userId()));

                for (String leagueId : leagues) {
                    UserRanking ranking = rankingRepository.addPoints(result.userId(), leagueId, result.points());
                    eventPublisher.publishRankingUpdated(ranking);
                }

                eventPublisher.publishPointsCalculated(result);
                scoresComputedCounter.increment();
            }
        } finally {
            scoringTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }
}
