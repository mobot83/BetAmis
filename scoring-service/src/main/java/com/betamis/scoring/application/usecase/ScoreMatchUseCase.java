package com.betamis.scoring.application.usecase;

import com.betamis.scoring.domain.model.FinalScore;
import com.betamis.scoring.domain.model.ScoringResult;
import com.betamis.scoring.domain.model.StoredPrediction;
import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.in.ScoreMatch;
import com.betamis.scoring.domain.port.out.RankingRepository;
import com.betamis.scoring.domain.port.out.ScoringEventPublisher;
import com.betamis.scoring.domain.port.out.ScoringResultRepository;
import com.betamis.scoring.domain.port.out.StoredPredictionRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ScoreMatchUseCase implements ScoreMatch {

    // All predictions currently belong to a single global league.
    // If per-competition leagues are needed in the future, the league id should
    // be carried by the StoredPrediction and passed here.
    private static final String GLOBAL_LEAGUE = "global";

    private final StoredPredictionRepository predictionRepository;
    private final ScoringResultRepository scoringResultRepository;
    private final RankingRepository rankingRepository;
    private final ScoringEventPublisher eventPublisher;

    @Inject
    public ScoreMatchUseCase(
            StoredPredictionRepository predictionRepository,
            ScoringResultRepository scoringResultRepository,
            RankingRepository rankingRepository,
            ScoringEventPublisher eventPublisher) {
        this.predictionRepository = predictionRepository;
        this.scoringResultRepository = scoringResultRepository;
        this.rankingRepository = rankingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void score(String matchId, FinalScore finalScore) {
        List<StoredPrediction> predictions = predictionRepository.findByMatchId(matchId);

        for (StoredPrediction prediction : predictions) {
            if (scoringResultRepository.existsByPredictionId(prediction.predictionId())) {
                Log.infof("Prediction %s already scored, skipping", prediction.predictionId());
                continue;
            }

            ScoringResult result = ScoringResult.calculate(prediction, finalScore);
            scoringResultRepository.save(result);

            UserRanking ranking = rankingRepository.addPoints(result.userId(), GLOBAL_LEAGUE, result.points());

            eventPublisher.publishPointsCalculated(result);
            eventPublisher.publishRankingUpdated(ranking);
        }
    }
}
